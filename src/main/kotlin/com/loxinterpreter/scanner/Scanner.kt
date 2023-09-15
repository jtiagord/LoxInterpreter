package com.loxinterpreter.scanner

import com.loxinterpreter.data.Token
import com.loxinterpreter.data.TokenType
import com.loxinterpreter.data.TokenType.*
import com.loxinterpreter.error


class Scanner(private val source: String) {
    private val tokens : MutableList<Token> = mutableListOf()
    private var curr = 0
    private var start = 0
    private var line = 1

    companion object {
        val keywords = HashMap<String, TokenType>().apply {
            put("and", AND);
            put("class", CLASS);
            put("else", ELSE);
            put("false", FALSE);
            put("for", FOR);
            put("fun", FUN);
            put("if", IF);
            put("nil", NIL);
            put("or", OR);
            put("print", PRINT);
            put("return", RETURN);
            put("super", SUPER);
            put("this", THIS);
            put("true", TRUE);
            put("var", VAR);
            put("while", WHILE);
        }
    }

    fun scanTokens() : List<Token>{
        while(!isAtEnd()) {
            start = curr
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }


    private fun peek(): Char = if(isAtEnd()) '\u0000' else source[curr]


    private fun scanToken() {
        val currChar = advance()
        when (currChar){
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '?' -> addToken(QUESTION)
            ':' -> addToken(COLON)
            '!' -> if(match('=')) addToken(BANG_EQUAL) else addToken(BANG)
            '=' -> if(match('=')) addToken(EQUAL_EQUAL) else addToken(EQUAL)
            '>' -> if(match('=')) addToken(GREATER_EQUAL) else addToken(GREATER)
            '<' -> if(match('=')) addToken(LESS_EQUAL) else addToken(LESS)
            '/' -> if(match('/')) {
                    while(peek() != '\n' && !isAtEnd()) advance()
                }else if(match('*')){
                    blockComment();
                }else addToken(SLASH)

            ' ','\r','\t' -> {}
            '\n' -> line++
            '"' -> string()
            else -> {
                if(isDigit(currChar)) number()
                else if (isAlpha(currChar)) identifier()
                else error(line, "Unexpected Character")
            }
        }
    }

    private fun blockComment() {
        while(!(peek() == '*' && peekNext() == '/') && !isAtEnd())
        {
            if(advance() == '\n') line++
        }

        if(isAtEnd()){
            error(line, "Block comment not ended")
            return
        }

        repeat(2){ advance() }
    }

    private fun isAlphaNumeric(ch : Char): Boolean = isAlpha(ch) || isDigit(ch)

    private fun isAlpha(ch : Char): Boolean = ch in 'a' .. 'z' || ch in 'A' .. 'Z' || ch == '_'

    private fun identifier() {
        while(isAlphaNumeric(peek())) advance()

        val text = source.substring(start until curr)
        val type = keywords[text] ?: IDENTIFIER

        addToken(type)
    }


    private fun number() {
        while(isDigit(peek())) advance()

        if(peek() == '.' && isDigit(peekNext())){
            advance()

            while(isDigit(peek()) && !isAtEnd()){
                advance()
            }
        }

        addToken(NUMBER, source.substring(start until curr).toDouble())
    }

    private fun peekNext(): Char =if(curr + 1 >= source.length)  '\u0000' else source[curr + 1]

    private fun isDigit(ch : Char): Boolean = ch in '0' .. '9'

    private fun string() {
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++
            advance()
        }

        if(isAtEnd()){
            error(line, "Unterminated String");
        }

        advance()

        val str = source.substring(start + 1 until curr - 1)
        addToken(STRING,str)
    }



    private fun match(expected : Char): Boolean {
        if(isAtEnd()) return false

        if(source[curr] == expected){
            curr++
            return true
        }

        return false
    }

    private fun addToken(tokenType : TokenType, literal : Any?){
        tokens.add(Token(tokenType, source.substring(start until curr), literal, line))
    }

    private fun addToken(tokenType: TokenType) {
        addToken(tokenType, null)
    }

    private fun isAtEnd() : Boolean {
        return curr >= source.length
    }

    private fun advance() : Char{
        return source[curr++]
    }
}
