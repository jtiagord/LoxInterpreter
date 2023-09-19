package com.loxinterpreter

import com.loxinterpreter.data.Expr
import com.loxinterpreter.data.RuntimeError.RuntimeError
import com.loxinterpreter.data.Token
import com.loxinterpreter.data.TokenType

class Interpreter : Expr.Visitor<Any?> {

    fun interpret(expression : Expr){
        try{
            val value = evaluate(expression)
            println(stringify(value))
        }catch (ex : RuntimeError){
            runtimeError(ex)
        }
    }



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
            else -> {
                null
            }
        }
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
        val then = evaluate(expr.thenBranch)
        val other = evaluate(expr.elseBranch)
        return if(isTruthy(condition)) then else other
    }
}