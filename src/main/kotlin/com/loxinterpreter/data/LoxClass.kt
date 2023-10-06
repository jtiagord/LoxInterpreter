package com.loxinterpreter.data

import com.loxinterpreter.Interpreter
import com.loxinterpreter.LoxCallable

class LoxClass(val name : String) : LoxCallable{
    override val arity: Int = 0
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? =
        LoxInstance(this)

    override fun toString(): String = name
}