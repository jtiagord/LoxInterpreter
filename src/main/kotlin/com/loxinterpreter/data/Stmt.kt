package com.loxinterpreter.data

abstract class Stmt {
	interface Visitor<R> {
		fun visitBlock(stmt: Block) : R
		fun visitIf(stmt: If) : R
		fun visitFunction(stmt: Function) : R
		fun visitReturn(stmt: Return) : R
		fun visitBreak(stmt: Break) : R
		fun visitWhile(stmt: While) : R
		fun visitExpression(stmt: Expression) : R
		fun visitPrint(stmt: Print) : R
		fun visitPrintLn(stmt: PrintLn) : R
		fun visitClass(stmt: Class) : R
		fun visitVar(stmt: Var) : R
	}
	abstract fun <R> accept(visitor : Visitor<R>) : R

	class Block(val statements : List<Stmt>, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitBlock(this)
	}

	class If(val condition : Expr, val thenBranch : Stmt, val elseBranch : Stmt?, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitIf(this)
	}

	class Function(val name : Token, val expr : Expr.Function, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitFunction(this)
	}

	class Return(val keyword : Token, val value : Expr?, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitReturn(this)
	}

	class Break() : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitBreak(this)
	}

	class While(val condition : Expr, val loop : Stmt, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitWhile(this)
	}

	class Expression(val expression : Expr, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitExpression(this)
	}

	class Print(val expression : Expr, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitPrint(this)
	}

	class PrintLn(val expression : Expr?, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitPrintLn(this)
	}

	class Class(val name : Token, val methods : List<Stmt.Function>, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitClass(this)
	}

	class Var(val name : Token, val initializer : Expr?, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitVar(this)
	}

}
