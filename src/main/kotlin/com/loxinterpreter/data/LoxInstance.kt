package com.loxinterpreter.data

class LoxInstance(private val klass : LoxClass) {

    val fields : MutableMap<String, Any?> = HashMap()
    fun get(name : Token) : Any? =
        if(fields.containsKey(name.lexeme)) fields[name.lexeme]
        else throw RuntimeError(name, "Undefined property '${name.lexeme}'.")

    override fun toString(): String = klass.name + " instance"
    fun set(name: Token, value: Any?){
        fields[name.lexeme] = value;
    }

}
