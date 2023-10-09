package com.loxinterpreter.data

import com.loxinterpreter.Interpreter
import com.loxinterpreter.LoxCallable

class LoxClass(val name : String, private val methods : HashMap<String,LoxFunction>) : LoxCallable{
    override val arity: Int = 0
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any =
        LoxInstance(this)

    override fun toString(): String = name
    fun findMethod(name: String): LoxFunction? = methods[name]

}