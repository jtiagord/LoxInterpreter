package com.loxinterpreter

import com.loxinterpreter.data.*


interface LoxCallable {
    val arity : Int
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}
class BreakException : Exception()

class Return(val value : Any?) : RuntimeException(null, null, false, false)

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private var global = Environment()
    private val locals: MutableMap<Expr, Int> = HashMap()
    private var currEnv = global

    init {
        val function = object  : LoxCallable{
            override val arity = 0
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any
                = (System.currentTimeMillis() / 1000.0)

            override fun toString(): String = "<native fn>";
        }
        global.define("clock", function)
    }

    fun interpret(stmts : List<Stmt>){
        try {
            for (stmt in stmts) {
                execute(stmt)
            }
        }catch (ex : RuntimeError){
            runtimeError(ex)
        }
    }

    fun executeBlock(stmts: List<Stmt>, env: Environment){
        val previous: Environment = this.currEnv
        try {
            this.currEnv = env
            for (statement in stmts) {
                execute(statement)
            }
        } finally {
            this.currEnv = previous
        }
    }

    private fun execute(stmt : Stmt) = stmt.accept(this)

    private fun stringify(value: Any?): String {
        if(value == null) return "nil"

        if (value is Double) {
            var text: String = value.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }

        return value.toString()
    }

    override fun visitBlock(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(currEnv))
    }

    override fun visitIf(stmt: Stmt.If) {
        val condition = evaluate(stmt.condition)

        if(isTruthy(condition)) execute(stmt.thenBranch)
        else if(stmt.elseBranch != null) execute(stmt.elseBranch)
    }

    override fun visitFunction(stmt: Stmt.Function) {
        val function = LoxFunction(stmt.expr, currEnv, stmt.name)
        currEnv.define(stmt.name.lexeme, function)
    }

    override fun visitReturn(stmt: Stmt.Return) {
        val value: Any? = if (stmt.value != null) evaluate(stmt.value) else null

        throw Return(value)
    }

    override fun visitBreak(stmt: Stmt.Break) {
        throw BreakException()
    }

    override fun visitWhile(stmt: Stmt.While) {
        try {
            while (isTruthy(evaluate(stmt.condition))) execute(stmt.loop)
        }catch (_: BreakException){}
    }

    override fun visitAssign(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        currEnv.assign(expr.name, value)
        return value
    }


    override fun visitBinary(expr: Expr.Binary): Any? {
        val right = evaluate(expr.right)
        val left = evaluate(expr.left)

        return when(expr.operator.type){
            TokenType.MINUS ->{
                checkNumberOperands(expr.operator, left, right)
                return left as Double - right as Double
            }
            TokenType.SLASH ->{
                checkNumberOperands(expr.operator, left, right)
                return left as Double / right as Double
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double * right as Double
            }
            TokenType.PLUS -> {
                if(left is Double && right is Double){
                    return left + right
                }
                if(left is String || right is String){
                    return stringify(left) + stringify(right)
                }else throw
                    RuntimeError(expr.operator,"Operands must be two numbers or two strings.")
            }

            TokenType.GREATER ->{
                checkNumberOperands(expr.operator, left, right)
                return left as Double > right as Double
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double >= right as Double
            }
            TokenType.LESS ->
            {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) < right as Double
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double <= right as Double
            }
            TokenType.BANG_EQUAL -> return !isEqual(left, right);
            TokenType.EQUAL_EQUAL -> return isEqual(left, right);
            TokenType.COMMA -> return right
            else -> {
                null
            }
        }
    }

    override fun visitLogical(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type === TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    private fun isEqual(left: Any?, right: Any?): Boolean {
        if(left == null && right == null) return true
        return left?.equals(right) ?: return false
    }

    override fun visitGrouping(expr: Expr.Grouping): Any? = evaluate(expr.expression)

    private fun evaluate(expr: Expr): Any? = expr.accept(this)

    override fun visitLiteral(expr: Expr.Literal): Any? = expr.value

    override fun visitUnary(expr: Expr.Unary): Any? {
        val value = evaluate(expr.right)

        return when(expr.operator.type){
            TokenType.MINUS ->
            {
                checkNumberOperands(expr.operator, value)
                - (value as Double)
            }
            TokenType.BANG -> !isTruthy(value)
            else -> {
                null
            }
        }
    }

    private fun checkNumberOperands(operator : Token, vararg numbers : Any?){
        for(number in numbers){
            if(number !is Double) throw RuntimeError(operator, "$number is not a number")
        }
    }

    private fun isTruthy(value: Any?): Boolean {
        if(value == null) return false
        if(value is Boolean) return value

        return true
    }

    override fun visitTernary(expr: Expr.Ternary): Any? {
        val condition = evaluate(expr.condition)
        return if(isTruthy(condition)) evaluate(expr.thenBranch) else evaluate(expr.elseBranch)
    }

    override fun visitVariable(expr: Expr.Variable): Any?
        = lookUpVariable(expr.name, expr);

    private fun lookUpVariable(name: Token, expr: Expr.Variable): Any? {
        val distance = locals[expr]

        return if(distance != null) currEnv.getAt(distance ,name)
                else global.get(name)

    }

    override fun visitFunction(expr: Expr.Function): Any {
        return LoxFunction(expr, currEnv)
    }

    override fun visitGet(expr: Expr.Get): Any? {
        val obj = evaluate(expr.obj)

        if(obj is LoxInstance){
            return obj.get(expr.name)
        }

        throw RuntimeError(expr.name, "Only instances have properties.")
    }

    override fun visitSet(expr: Expr.Set): Any? {
        val obj = evaluate(expr.obj)

        if(obj is LoxInstance){
            return obj.set(expr.name, evaluate(expr.value))
        }

        throw RuntimeError(expr.name, "Only instances have properties.")
    }

    override fun visitCall(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)
        val arguments: MutableList<Any?> = ArrayList()

        for (argument in expr.arguments) {
            arguments.add(evaluate(argument))
        }

        if(callee !is LoxCallable)
            throw RuntimeError(expr.paren,"Can only functions call functions and classes")

        if(arguments.size != callee.arity){
            throw RuntimeError(expr.paren,
                "Expected ${callee.arity} arguments but got ${arguments.size}")
        }

        return callee.call(this, arguments)
    }

    override fun visitExpression(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitPrint(stmt : Stmt.Print) {
        val value = evaluate(stmt.expression)
        print(stringify(value))
    }

    override fun visitPrintLn(stmt: Stmt.PrintLn) {
        if(stmt.expression == null) println()
        else println(stringify(evaluate(stmt.expression)))
    }

    override fun visitClass(stmt: Stmt.Class) {
        val klass = LoxClass(stmt.name.lexeme)
        currEnv.define(stmt.name.lexeme, klass)
    }

    override fun visitVar(stmt: Stmt.Var) {
        val value = if(stmt.initializer == null) null else evaluate(stmt.initializer)
        currEnv.define(stmt.name.lexeme, value)
    }

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }
}