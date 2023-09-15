package com.loxinterpreter.parser


import com.loxinterpreter.data.Expr
import com.loxinterpreter.data.Token
import com.loxinterpreter.data.TokenType


class RPNPrinter : Expr.Visitor<String> {
    override fun visitBinary(expr: Expr.Binary): String
        = "${expr.left.accept(this)} ${expr.right.accept(this)} ${expr.operator.lexeme}"


    override fun visitGrouping(expr: Expr.Grouping): String = expr.expression.accept(this)


    override fun visitLiteral(expr: Expr.Literal): String
            = expr.value?.toString() ?: "nil"


    override fun visitUnary(expr: Expr.Unary): String {
        var operator = expr.operator.lexeme
        if (expr.operator.type === TokenType.MINUS) {
            // Can't use same symbol for unary and binary.
            operator = "~"
        }
        return expr.right.accept(this) + " " + operator
    }


    fun print(expr : Expr) : String = expr.accept(this)
}

fun main(){
    val expr = Expr.Binary(
        Expr.Binary(Expr.Literal(1), Token(TokenType.PLUS, "+", null, 0),Expr.Literal(2)),
        Token(TokenType.STAR, "*", null, 0),
        Expr.Binary(Expr.Literal(4), Token(TokenType.MINUS, "-", null, 0),Expr.Literal(3))
    )

    val printer = RPNPrinter()
    println(printer.print(expr))

}