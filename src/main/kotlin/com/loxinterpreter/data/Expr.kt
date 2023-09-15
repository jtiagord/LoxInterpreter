package com.loxinterpreter.data

import java.util.List

abstract class Expr {
	interface Visitor<R> {
		fun visitBinary(expr: Binary) : R
		fun visitGrouping(expr: Grouping) : R
		fun visitLiteral(expr: Literal) : R
		fun visitUnary(expr: Unary) : R
		fun visitTernary(expr: Ternary) : R
	}
	abstract fun <R> accept(visitor : Visitor<R>) : R

	class Binary(val left : Expr, val operator : Token, val right : Expr, ) : Expr() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitBinary(this)
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

}
