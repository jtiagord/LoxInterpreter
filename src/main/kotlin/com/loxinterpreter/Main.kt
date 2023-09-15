package com.loxinterpreter

import com.loxinterpreter.data.Token
import com.loxinterpreter.data.TokenType
import com.loxinterpreter.parser.AstPrinter
import com.loxinterpreter.parser.Parser
import com.loxinterpreter.scanner.Scanner
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.system.exitProcess

var hadError = false

fun main(args : Array<String>){
    if(args.size > 1) println("Usage : jlox {file}")

    if(args.size == 1) runFile(args[0])
    else runPrompt()
}

fun runPrompt() {
    val input = BufferedReader(InputStreamReader(System.`in`))

    while(true) {
        print("> ")
        val input = input.readLine() ?: return

        run(input)
        hadError = false
    }
}

fun runFile(file : String) {
    val input = Files.readAllBytes(Path(file))
    run(String(input, Charset.defaultCharset()))
    if(hadError) exitProcess(65);
}

fun run(input : String){
    val scanner = Scanner(input)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val expression = parser.parse()

    if(hadError) return

    println(AstPrinter().print(expression!!))
}




fun error(line: Int, message: String) {
    report(line, "", message)
}

fun error(token : Token, message : String){
    if (token.type == TokenType.EOF) {
        report(token.line, " at end", message);
    } else {
        report(token.line, " at '" + token.lexeme + "'", message);
    }
}

private fun report(line: Int, where: String, message: String) {
    System.err.println("[Line $line] Error$where: $message")
    hadError = true
}
