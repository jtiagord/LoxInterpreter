package com.loxinterpreter

import com.loxinterpreter.data.RuntimeError
import com.loxinterpreter.data.Token


class Environment(var enclosing : Environment? = null){
    private val variables = HashMap<String, Any?>()
    fun define(name : String, value : Any?){
        variables[name] = value
    }
    fun get(name : Token) : Any?{
        if(variables.containsKey(name.lexeme)) {
            return variables[name.lexeme]
        }

        return if(enclosing != null) enclosing!!.get(name)
        else
            throw RuntimeError(
                name,
                "Undefined variable '" + name.lexeme + "'."
            )
    }

    fun assign(name : Token, newValue : Any?){
        if(variables.containsKey(name.lexeme)) {
            variables[name.lexeme] = newValue
            return
        }

        if(enclosing != null){
            return enclosing!!.assign(name, newValue)
        }

        throw RuntimeError(
            name,
            "Undefined variable '" + name.lexeme + "'." )

    }

    fun getAt(distance: Int, name : Token) : Any?
        = ancestor(distance).get(name)


    private fun ancestor(distance: Int) : Environment{
        var env = this
        for(i in 0 until distance){
            env = env.enclosing?: throw IllegalStateException("Ancestor at $distance doesn't exist")
        }
        return env
    }
}