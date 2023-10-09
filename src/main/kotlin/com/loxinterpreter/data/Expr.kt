package com.loxinterpreter.data

abstract class Expr {
	interface Visitor<R> {
		fun visitAssign(expr: Assign) : R
		fun visitBinary(expr: Binary) : R
		fun visitLogical(expr: Logical) : R
		fun visitGrouping(expr: Grouping) : R
		fun visitLiteral(expr: Literal) : R
		fun visitUnary(expr: Unary) : R
		fun visitTernary(expr: Ternary) : R
		fun visitVariable(expr: Variable) : R
		fun visitFunction(expr: Function) : R
		fun visitGet(expr: Get) : R
		fun visitThis(expr: This) : R
		fun visitSet(expr: Set) : R
		fun visitCall(expr: Call) : R
	}
	abstract fun <R> accept(visitor : Visitor<R>) : R

	class Assign(val name : Token, val value : Expr, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitAssign(this)
	}

	class Binary(val left : Expr, val operator : Token, val right : Expr, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitBinary(this)
	}

	class Logical(val left : Expr, val operator : Token, val right : Expr, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitLogical(this)
	}

	class Grouping(val expression : Expr, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitGrouping(this)
	}

	class Literal(val value : Any?, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitLiteral(this)
	}

	class Unary(val operator : Token, val right : Expr, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitUnary(this)
	}

	class Ternary(val condition : Expr, val thenBranch : Expr, val elseBranch : Expr, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitTernary(this)
	}

	class Variable(val name : Token, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitVariable(this)
	}

	class Function(val params : List<Token>, val body : List<Stmt>, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitFunction(this)
	}

	class Get(val obj : Expr, val name : Token, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitGet(this)
	}

	class This(val keyword : Token, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitThis(this)
	}

	class Set(val obj : Expr, val name : Token, val value : Expr, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitSet(this)
	}

	class Call(val callee : Expr, val paren : Token, val arguments : List<Expr>, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitCall(this)
	}

}
