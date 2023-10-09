package com.loxinterpreter.data

class LoxInstance(private val klass : LoxClass) {

    private val fields : MutableMap<String, Any?> = HashMap()
    fun get(name : Token) : Any? {
        if(fields.containsKey(name.lexeme)) return fields[name.lexeme]

        val method = klass.findMethod(name.lexeme)

        if(method != null) return method.bind(this)

        throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }


    override fun toString(): String = klass.name + " instance"
    fun set(name: Token, value: Any?){
        fields[name.lexeme] = value;
    }

}
