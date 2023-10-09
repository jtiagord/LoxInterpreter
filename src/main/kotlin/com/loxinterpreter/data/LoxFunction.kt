package com.loxinterpreter.data

import com.loxinterpreter.Environment
import com.loxinterpreter.Interpreter
import com.loxinterpreter.LoxCallable
import com.loxinterpreter.Return

class LoxFunction(private val declaration : Expr.Function, private val closure : Environment, val name : Token? = null)
    : LoxCallable {

    override val arity: Int
        get() = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for((i,params) in declaration.params.withIndex()){
            environment.define(params.lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        }catch (returnVal : Return){
            return returnVal.value
        }
        return null
    }

    override fun toString(): String = "<fn ${name?.lexeme?: "anonymous"}>"
    fun bind(instance : LoxInstance) : LoxFunction{
        val environment = Environment(closure);
        environment.define("this", instance);
        return LoxFunction(declaration, environment, name);
    }

}