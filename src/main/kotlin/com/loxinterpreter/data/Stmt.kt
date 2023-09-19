package com.loxinterpreter.data

import java.util.List

abstract class Stmt {
	interface Visitor<R> {
		fun visitExpression(expr: Expression) : R
		fun visitPrint(expr: Print) : R
	}
	abstract fun <R> accept(visitor : Visitor<R>) : R

	class Expression(val expression : Expr, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitExpression(this)
	}

	class Print(val expression : Expr, ) : Stmt() {
		override fun <R> accept(visitor : Visitor<R>) : R = visitor.visitPrint(this)
	}
}
