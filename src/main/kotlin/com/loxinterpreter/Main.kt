package com.loxinterpreter

import com.loxinterpreter.data.RuntimeError
import com.loxinterpreter.data.Token
import com.loxinterpreter.data.TokenType
import com.loxinterpreter.parser.Parser
import com.loxinterpreter.scanner.Scanner
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.system.exitProcess

var hadError = false
var hadRuntimeError = false



fun main(args : Array<String>){
    if(args.size > 1) println("Usage : jlox {file}")

    if(args.size == 1) runFile(args[0])
    else runPrompt()
}



fun runtimeError(error: RuntimeError) {
    System.err.println(error.message +
            "\n[line " + error.token.line + "]");
    hadRuntimeError = true
}
fun runPrompt() {
    val input = BufferedReader(InputStreamReader(System.`in`))

    while(true) {
        print("> ")
        val input = input.readLine() ?: return

        run(input, true)
        hadError = false
    }
}

fun runFile(file : String) {
    val input = Files.readAllBytes(Path(file))
    run(String(input, Charset.defaultCharset()), false)
    if (hadError) exitProcess(65)
    if (hadRuntimeError) exitProcess(70);
}

fun run(input : String, allowExpression : Boolean){
    val scanner = Scanner(input)
    val tokens = scanner.scanTokens()

    if(hadError) return

    val parser = Parser(tokens, allowExpression)
    val statements = parser.parse()

    if(hadError) return

    val interpreter = Interpreter()

    val resolver = Resolver(interpreter)
    resolver.resolve(statements)

    interpreter.interpret(statements)
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

fun warn(token : Token, message : String){
    if (token.type == TokenType.EOF) {
        warning(token.line, " at end", message);
    } else {
        warning(token.line, " at '" + token.lexeme + "'", message);
    }
}

private fun report(line: Int, where: String, message: String) {
    System.err.println("[Line $line] Error$where: $message")
    hadError = true
}

private fun warning(line: Int, where: String, message: String) {
    println("[Line $line] WARNING $where: $message")
}
