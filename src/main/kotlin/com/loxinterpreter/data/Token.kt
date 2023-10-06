package com.loxinterpreter.data

enum class TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, QUESTION,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, COLON,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL, ARROW,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    // Keywords.
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT,PRINTLN, RETURN, SUPER, THIS, TRUE, VAR, WHILE, BREAK,

    EOF
}
data class Token (
    val type : TokenType,
    val lexeme : String,
    val literal : Any?,
    val line : Int
)


