package com.loxinterpreter.parser

import com.loxinterpreter.data.Expr
import com.loxinterpreter.data.Stmt
import com.loxinterpreter.data.Token
import com.loxinterpreter.data.TokenType
import com.loxinterpreter.data.TokenType.*
import com.loxinterpreter.error


class ParseError : RuntimeException()

class Parser(private val tokens : List<Token>, private val allowExpression : Boolean){
    private var curr = 0
    private var loopDepth = 0
    fun parse() : List<Stmt>{
        val stmts = ArrayList<Stmt>()
        while(!isAtEnd()){
            val stmt = declaration()?:continue
            stmts.add(stmt)
        }

        return stmts
    }

    private fun declaration(): Stmt? {
        return try {
            if(match(CLASS)) classDeclaration()
            else if (match(VAR)) varDeclaration()
            else if (match(FUN)) function("function")
            else statement();
        } catch (error : ParseError) {
            synchronize();
            null;
        }
    }

    private fun classDeclaration() : Stmt {
        val className = consume(IDENTIFIER, "Expected identifier")
        consume(LEFT_BRACE, "Expected '{' before class body")

        val functions = ArrayList<Stmt.Function>()
        while(!isAtEnd() && peek().type != RIGHT_BRACE){
            functions.add(function("method") as Stmt.Function)
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return Stmt.Class(className, functions)
    }

    private fun varDeclaration() : Stmt{
        val name = consume(IDENTIFIER, "Expected variable name")
        val value = if(match(EQUAL)) expression() else null
        if(value !is Expr.Function) consume(SEMICOLON , "Expected ';'")
        return Stmt.Var(name,value)
    }

    private fun statement(): Stmt =
        if(match(IF)) ifStatement()
        else if(match(FOR)) forStatement()
        else if(match(WHILE)) whileStatement()
        else if(match(PRINT)) printStatement()
        else if(match(PRINTLN)) printLnStatement()
        else if(match(LEFT_BRACE)) block()
        else if(match(BREAK)) breakStatement()
        else if(match(RETURN)) returnStatement()
        else expressionStatement()

    private fun returnStatement(): Stmt {
        val returnToken = previous()
        val value = if(peek().type != SEMICOLON) expression() else null
        consume(SEMICOLON, "Expected ';' after statement")
        return Stmt.Return(returnToken, value)
    }

    private fun function(kind : String): Stmt {
        val name = consume(IDENTIFIER, "Expect $kind name.")
        consume(LEFT_PAREN, "Expect '(' after $kind name.")
        val params = ArrayList<Token>()

        if(peek().type != RIGHT_PAREN){
            do{
                if (params.size >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                params.add(consume(IDENTIFIER, "Expect parameter name."));
            }while (match(COMMA))
        }

        consume(RIGHT_PAREN, "Expect ')' after parameters.")
        consume(LEFT_BRACE, "Expected '{' before $kind body")
        val block = block() as Stmt.Block

        return Stmt.Function(name, Expr.Function(params, block.statements))
    }

    private fun breakStatement(): Stmt {
        if(loopDepth==0){
            error(previous(), "Using break outside of loop")
            throw ParseError()
        }
        consume(SEMICOLON, "Missing ';'")
        return Stmt.Break()
    }

    private fun forStatement(): Stmt {
        loopDepth++
        consume(LEFT_PAREN, "Missing '('")

        val initializer : Stmt? = if(match(VAR)) varDeclaration()
                                    else if(match(SEMICOLON)) null
                                    else expressionStatement()

        var condition : Expr? = null

        if(!match(SEMICOLON)){
            condition = expression()
            consume(SEMICOLON, "Expected ; after loop condition")
        }

        val increment = if(peek().type == RIGHT_PAREN) null else expression()

        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()

        if(increment != null) body = Stmt.Block(listOf(body, Stmt.Expression(increment)))

        if (condition == null) condition = Expr.Literal(true)

        body = Stmt.While(condition, body)

        if(initializer != null) body = Stmt.Block(listOf(initializer, body))
        loopDepth--
        return body
    }

    private fun whileStatement(): Stmt {
        loopDepth++
        consume(LEFT_PAREN, "Missing '('")
        val cond = expression()
        consume(RIGHT_PAREN, "Missing ')'")
        val loop = statement()
        loopDepth--
        return Stmt.While(cond, loop)
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Missing '('")
        val cond = expression()
        consume(RIGHT_PAREN, "Missing ')'")
        val then = statement()
        var elseStmt : Stmt? = null
        if(match(ELSE)) elseStmt = statement();
        return Stmt.If(cond, then, elseStmt)
    }

    private fun block(): Stmt {
        val stmts = ArrayList<Stmt>()
        while(!isAtEnd() && peek().type != RIGHT_BRACE){
            val stmt = declaration()?:continue
            stmts.add(stmt)
        }

        consume(RIGHT_BRACE, "Expected '}' after block")
        return Stmt.Block(stmts)
    }


    private fun expressionStatement(): Stmt {
        val value = expression()

        if (allowExpression && isAtEnd()) {
            return Stmt.Print(value)
        }

        consume(SEMICOLON , "Expected ';'")
        return Stmt.Expression(value)
    }
    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON , "Expected ';'")
        return Stmt.Print(value)
    }

    private fun printLnStatement(): Stmt {
        val value = if(peek().type != SEMICOLON) expression() else null
        consume(SEMICOLON , "Expected ';'")
        return Stmt.PrintLn(value)
    }


    private fun expression() : Expr = assignment();

    private fun assignment(): Expr {
        val expr = comma()

        if(match(EQUAL)){
            val equals = previous()
            val value = assignment()

            if(expr is Expr.Variable){
                return Expr.Assign(expr.name, value)
            }else if(expr is Expr.Get){
                return Expr.Set(expr.obj, expr.name, value)
            }

            error(equals, "Invalid assignment target.");
        }

        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr {
        var expr = ternary()

        while (match(AND)) {
            val operator = previous()
            val right = ternary()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun comma() : Expr {
        var expr = or()

        while (match(COMMA)) {
            val operator = previous()
            val right = or()
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
        return call();
    }

    private fun call(): Expr {
        var expr = primary()
        while(true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                expr = Expr.Get(
                    obj = expr,
                    name = consume(IDENTIFIER, "Expect property name after '.'.")
                )
            } else {
                break
            }
        }
        return expr
    }

    private fun finishCall(expr: Expr): Expr {
        val arguments = ArrayList<Expr>()
        if (peek().type != RIGHT_PAREN){
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments.")
                    throw ParseError()
                }
                arguments.add(or())
            } while (match(COMMA))
        }
        val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")
        return Expr.Call(expr, paren, arguments)
    }

    private fun consume(token : TokenType, errorMessage : String) : Token {
        if(match(token)) return previous()

        error(peek(), errorMessage)
        throw ParseError()
    }

    private fun primary(): Expr {
        if(match(LEFT_PAREN)) {
            if(match(RIGHT_PAREN) && match(ARROW)){
                consume(LEFT_BRACE,  "Expected '{' after '->'")
                return Expr.Function(ArrayList(), (block() as Stmt.Block).statements)
            }

            val expr = expression()
            consume(RIGHT_PAREN, "Missing closing \")\"")

            if(peek().type == ARROW){
                advance()
                val tokens = parseExpressionToArgs(expr)
                consume(LEFT_BRACE, "Missing '{'")
                return Expr.Function(tokens, (block() as Stmt.Block).statements)
            }
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

    private fun parseExpressionToArgs(expr: Expr): List<Token> {
        val tokens = ArrayList<Token>()
        parseExpressionToArgsHelper(expr, tokens)
        return tokens
    }

    private fun parseExpressionToArgsHelper(expr:Expr, tokens : MutableList<Token>){
        if(expr is Expr.Variable){
            tokens.add(expr.name)
            return
        }

        if(expr is Expr.Binary){
            if(expr.left is Expr.Variable && expr.operator.type == COMMA){
                tokens.add(expr.left.name)
                parseExpressionToArgsHelper(expr.right, tokens)
                return
            }
            if(expr.operator.type == COMMA){
                error(expr.operator , "Missing ',' separating identifiers")
            }else {
                error(expr.operator, "Only identifiers allowed in function arguments")
            }
            throw ParseError()
        }

        error(peek(), "Invalid syntax for arrow function correct usage : ({args?})->{}")
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

