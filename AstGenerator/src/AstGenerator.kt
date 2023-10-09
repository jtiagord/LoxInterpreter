import java.io.PrintWriter
import kotlin.system.exitProcess

fun main(args : Array<String>) {
    if(args.size != 1){
        println("Usage generate_ast <output_directory>")
        exitProcess(64)
    }

    val outputDir = args[0]

    defineAst(
        outputDir, "Expr", listOf(
            "Assign   : Token name , Expr value",
            "Binary   : Expr left, Token operator, Expr right",
            "Logical  : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Any? value",
            "Unary    : Token operator, Expr right",
            "Ternary  : Expr condition, Expr thenBranch , Expr elseBranch",
            "Variable : Token name",
            "Function : List<Token> params, List<Stmt> body",
            "Get      : Expr obj, Token name",
            "This     : Token keyword",
            "Set      : Expr obj, Token name, Expr value",
            "Call     : Expr callee, Token paren, List<Expr> arguments",
        )
    )

    defineAst(outputDir, "Stmt", listOf(
            "Block      : List<Stmt> statements",
            "If         : Expr condition, Stmt thenBranch, Stmt? elseBranch",
            "Function   : Token name, Expr.Function expr",
            "Return     : Token keyword, Expr? value",
            "Break      :",
            "While      : Expr condition, Stmt loop",
            "Expression : Expr expression",
            "Print      : Expr expression",
            "PrintLn    : Expr? expression",
            "Class      : Token name, List<Stmt.Function> methods",
            "Var        : Token name, Expr? initializer"
        )
    )
}

fun defineAst(outputDir: String, baseName: String, types : List<String>) {
    val writer = PrintWriter("$outputDir/$baseName.kt")

    writer.println("package com.loxinterpreter.data")
    writer.println()
    writer.println("abstract class $baseName {")

    defineVisitor(writer, baseName, types)

    for( type in types ) {
        val (className, fields) = type.split(":").map(String::trim)
        writer.print("\t")
        defineType(writer, baseName, className, fields)
        writer.println()
    }
    writer.println("}")
    writer.close()
}

fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
    writer.println("\tinterface Visitor<R> {")

    for(type in types){
        val typeName = type.split(":")[0].trim()

        writer.println("\t\tfun visit$typeName(${baseName.lowercase()}: $typeName) : R")
    }
    writer.println("\t}")

    writer.println("\tabstract fun <R> accept(visitor : Visitor<R>) : R")

    writer.println()

}

fun defineType(writer: PrintWriter, baseClass : String, className: String, fields: String) {
    writer.print("class $className(")
    /*{
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitBinary(this)
	}*/
    if(fields.isNotEmpty()) fields.split(",").forEach { field ->
        val trimmedField = field.trim()
        val (type ,name) = trimmedField.split("\\s+".toRegex())
        writer.print("val $name : $type, ")
    }
    writer.println(") : $baseClass() {")
    writer.println("\t\t${acceptFunction(className)}")
    writer.println("\t}")
}

fun acceptFunction(className : String) = "override fun <R> accept(visitor : Visitor<R>) : R = visitor.visit$className(this)"
