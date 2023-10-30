package com.loxinterpreter.data

import com.loxinterpreter.Interpreter
import com.loxinterpreter.LoxCallable

class LoxClass(val name : String, private val methods : HashMap<String,LoxFunction>, val superClass : LoxClass?)
                                                                                                        : LoxCallable{
    override val arity: Int = findMethod("init")?.arity ?: 0

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
        val initializer = findMethod("init")

        val instance = LoxInstance(this)

        initializer?.bind(instance)?.call(interpreter,arguments)

        return instance
    }


    override fun toString(): String = name
    fun findMethod(name: String): LoxFunction? = methods[name] ?: superClass?.findMethod(name)

}