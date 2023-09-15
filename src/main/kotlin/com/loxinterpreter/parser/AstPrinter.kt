package com.loxinterpreter.parser

import com.loxinterpreter.data.Expr
import com.loxinterpreter.data.Token
import com.loxinterpreter.data.TokenType

class AstPrinter : Expr.Visitor<String> {
    override fun visitBinary(expr: Expr.Binary): String =
        parenthesize(expr.operator.lexeme, expr.left, expr.right)


    override fun visitGrouping(expr: Expr.Grouping): String =
        parenthesize("group", expr.expression)


    override fun visitLiteral(expr: Expr.Literal): String
        = expr.value?.toString() ?: "nil"


    override fun visitUnary(expr: Expr.Unary): String
        = parenthesize(expr.operator.lexeme, expr.right)

    private fun parenthesize(name : String, vararg exprs : Expr) : String{
        val builder = StringBuilder()

        builder.append("(").append(name)

        for(expr in exprs){
            builder.append(" ${expr.accept(this)}")
        }

        builder.append(")")

        return builder.toString()
    }

    fun print(expr : Expr) : String = expr.accept(this)
}

fun main(){
    val expr = Expr.Binary(Expr.Literal(1), Token(TokenType.PLUS, "+", null, 0),Expr.Literal(2))
    val printer = AstPrinter()

    val unary = Expr.Unary(Token(TokenType.MINUS, "-", null, 0), expr)
    val grouping = Expr.Grouping(unary)
    println(printer.print(grouping))

}