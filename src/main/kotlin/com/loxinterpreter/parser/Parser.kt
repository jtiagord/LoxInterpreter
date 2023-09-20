package com.loxinterpreter.parser

import com.loxinterpreter.data.Expr
import com.loxinterpreter.data.Stmt
import com.loxinterpreter.data.Token
import com.loxinterpreter.data.TokenType
import com.loxinterpreter.data.TokenType.*
import com.loxinterpreter.error


class ParseError : RuntimeException()

class Parser(private val tokens : List<Token>){
    private var curr = 0;

    fun parse() : List<Stmt>{
        val stmts = ArrayList<Stmt>()
        while(!isAtEnd()){
            val stmt = declaration()?:continue
            stmts.add(stmt)
        }

        return stmts
    }

    private fun declaration(): Stmt? {
        try {
            if (match(VAR)) return varDeclaration();

            return statement();
        } catch (error : ParseError) {
            synchronize();
            return null;
        }
    }

    private fun varDeclaration() : Stmt{
        val name = consume(IDENTIFIER, "Expected variable name")
        val value = if(match(EQUAL)) expression() else null
        consume(SEMICOLON , "Expected ';'")
        return Stmt.Var(name,value)
    }

    private fun statement(): Stmt =
        if(match(PRINT)) printStatement()
        else expressionStatement()

    private fun expressionStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON , "Expected ';'")
        return Stmt.Expression(value)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON , "Expected ';'")
        return Stmt.Print(value)
    }


    private fun expression() : Expr = comma();

    private fun comma() : Expr {
        var expr = ternary()

        while (match(COMMA)) {
            val operator = previous()
            val right = ternary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun ternary() : Expr {
        var expr = equality()

        if((match(QUESTION))){
            val second = equality()
            consume(COLON, "Expected \":\" for ternary operator")
            val third = equality()
            expr = Expr.Ternary(expr, second, third)
        }

        return expr
    }

    private fun equality() : Expr {
        var expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (match(GREATER, GREATER_EQUAL,LESS,LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(STAR, SLASH)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if(match(BANG, MINUS)){
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return primary();
    }

    private fun consume(token : TokenType, errorMessage : String) : Token {
        if(match(token)) return peek()

        error(peek(), errorMessage)
        throw ParseError()
    }

    private fun primary(): Expr {
        if(match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Missing closing \")\"")
            return Expr.Grouping(expr)
        }

        if(match(FALSE)){ return Expr.Literal(false) }
        if(match(TRUE)){ return Expr.Literal(true) }
        if(match(NIL)){ return Expr.Literal(null) }
        if(match(NUMBER, STRING)){ return Expr.Literal(previous().literal) }
        if(match(IDENTIFIER)) { return Expr.Variable(previous()) }

        error(peek(), "Invalid value")
        throw ParseError()
    }

    private fun previous(): Token {
        return tokens[curr - 1]
    }

    private fun match(vararg types : TokenType) : Boolean{
        if(isAtEnd()) return false
        val nextToken = peek();
        for (type in types){
            if(nextToken.type == type){
                advance()
                return true
            }
        }
        return false
    }

    private fun advance() : Token {
        if(!isAtEnd()) curr ++;
        return previous()
    }

    private fun peek(): Token {
        return tokens[curr]
    }

    private fun isAtEnd(): Boolean {
        return peek().type == EOF
    }

    private fun synchronize(){
        while(!isAtEnd()){
            val curr = peek()
            if(curr.type == SEMICOLON){
                advance()
                return
            }

            when (curr.type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> {}
            }

            advance()
        }
    }
}

