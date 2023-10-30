package com.loxinterpreter

import com.loxinterpreter.data.Expr
import com.loxinterpreter.data.Stmt
import com.loxinterpreter.data.Token
import java.util.*
import kotlin.collections.HashMap

enum class FunctionType{
    FUNCTION, METHOD , NONE
}

enum class ClassType{
    CLASS, SUBCLASS, NONE
}

class Resolver(private val interpreter : Interpreter) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {

    private val scopes = Stack<HashMap<String, Boolean>>()
    private var currClass = ClassType.NONE

    override fun visitBlock(stmt: Stmt.Block) {
        beginScope()
        resolveBlock(stmt.statements)
        endScope()
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun beginScope() {
        scopes.push(HashMap())
    }

    fun resolve(statements: List<Stmt>) {
        for(stmt in statements){
            resolve(stmt)
        }
    }

    private fun resolveBlock(statements : List<Stmt>){
        for(stmt in statements){
            resolve(stmt)
        }
    }


    private fun resolve(statement : Stmt){
        statement.accept(this)
    }

    private fun resolve(expr : Expr){
        expr.accept(this)
    }

    override fun visitIf(stmt: Stmt.If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if(stmt.elseBranch != null) resolve(stmt.elseBranch)
    }

    override fun visitFunction(stmt: Stmt.Function) {
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt.expr, FunctionType.FUNCTION)
    }

    private fun resolveFunction(expr: Expr.Function, type : FunctionType) {
        beginScope()
        for(param in expr.params){
            declare(param)
            define(param)
        }
        resolveBlock(expr.body)
        endScope()
    }

    override fun visitReturn(stmt: Stmt.Return) {
        if(stmt.value != null) resolve(stmt.value)
    }

    override fun visitBreak(stmt: Stmt.Break) {}

    override fun visitWhile(stmt: Stmt.While) {
        resolve(stmt.condition)
        resolve(stmt.loop)
    }

    override fun visitExpression(stmt: Stmt.Expression) {
        resolve(stmt.expression)
    }

    override fun visitPrint(stmt: Stmt.Print) {
        resolve(stmt.expression)
    }

    override fun visitPrintLn(stmt: Stmt.PrintLn) {
        if(stmt.expression != null) resolve(stmt.expression)
    }

    override fun visitClass(stmt: Stmt.Class) {
        val enclosingClass = currClass
        currClass = ClassType.CLASS

        declare(stmt.name)

        if(stmt.superClass != null){
            currClass = ClassType.SUBCLASS
            beginScope()
            scopes.peek()["super"] = true
        }

        beginScope()
        scopes.peek()["this"] = true
        for(method in stmt.methods){
            resolveFunction(method.expr, FunctionType.METHOD)
        }

        define(stmt.name)

        if(stmt.superClass?.name?.lexeme == stmt.name.lexeme){
            error(stmt.superClass.name, "A class can't inherit itself")
        }
        endScope()

        if (stmt.superClass != null) endScope()

        currClass = enclosingClass
    }

    override fun visitVar(stmt: Stmt.Var) {
        declare(stmt.name)
        if(stmt.initializer != null) resolve(stmt.initializer)
        define(stmt.name)
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return

        scopes.peek()[name.lexeme] = true
    }

    private fun declare(name: Token) {
        if (!scopes.isEmpty()){
            scopes.peek()[name.lexeme] = false
        }
    }

    override fun visitAssign(expr: Expr.Assign) {
        resolve(expr.value)
        resolveLocal(expr.value, expr.name)
    }

    override fun visitBinary(expr: Expr.Binary) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitLogical(expr: Expr.Logical) {
        resolve(expr.right)
        resolve(expr.left)
    }

    override fun visitGrouping(expr: Expr.Grouping) {
        resolve(expr.expression)
    }

    override fun visitLiteral(expr: Expr.Literal) {}

    override fun visitUnary(expr: Expr.Unary) {
        resolve(expr.right)
    }

    override fun visitTernary(expr: Expr.Ternary) {
        resolve(expr.condition)
        resolve(expr.thenBranch)
        resolve(expr.elseBranch)
    }

    override fun visitVariable(expr: Expr.Variable) {
        if(!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false)
            error(expr.name, "Cannot read variable in its own initializer")

        resolveLocal(expr, expr.name)
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        for(i in (0 until scopes.size).reversed()){
            if(scopes[i].containsKey(name.lexeme)){
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    override fun visitFunction(expr: Expr.Function) {
        resolveFunction(expr,FunctionType.FUNCTION)
    }

    override fun visitGet(expr: Expr.Get) {
        resolve(expr.obj)
    }

    override fun visitThis(expr: Expr.This) =
        if (currClass == ClassType.NONE) {
            error(expr.keyword, "Can't use 'this' outside of a class.")
        }else resolveLocal(expr, expr.keyword)

    override fun visitSuper(expr: Expr.Super) =
        if (currClass == ClassType.NONE) {
            error(expr.keyword, "Can't use 'super' outside of a class.")
        }
        else if (currClass != ClassType.SUBCLASS) {
            error(expr.keyword,
            "Can't use 'super' in a class with no superclass.")
        }
        else{
            resolveLocal(expr, expr.keyword)
        }


    override fun visitSet(expr: Expr.Set) {
        resolve(expr.obj)
        resolve(expr.value)
    }

    override fun visitCall(expr: Expr.Call) {
        resolve(expr.callee)
        for(arg in expr.arguments){
            resolve(arg)
        }
    }
}