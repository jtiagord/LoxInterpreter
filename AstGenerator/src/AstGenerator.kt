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
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Any? value",
            "Unary    : Token operator, Expr right",
            "Ternary  : Expr condition, Expr thenBranch , Expr elseBranch"
        )
    )

    defineAst(outputDir, "Stmt", listOf(
            "Expression : Expr expression",
            "Print      : Expr expression"
        )
    )
}

fun defineAst(outputDir: String, baseName: String, types : List<String>) {
    val writer = PrintWriter("$outputDir/$baseName.kt")

    writer.println("package com.loxinterpreter.data")
    writer.println()
    writer.println("import java.util.List")
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

        writer.println("\t\tfun visit$typeName(expr: $typeName) : R")
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
    fields.split(",").forEach { field ->
        val trimmedField = field.trim()
        val (type ,name) = trimmedField.split("\\s+".toRegex())
        writer.print("val $name : $type, ")
    }
    writer.println(") : $baseClass() {")
    writer.println("\t\t${acceptFunction(className)}")
    writer.println("\t}")
}

fun acceptFunction(className : String) = "override fun <R> accept(visitor : Visitor<R>) : R = visitor.visit$className(this)"
