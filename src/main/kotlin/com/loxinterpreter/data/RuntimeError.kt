package com.loxinterpreter.data.RuntimeError

import com.loxinterpreter.data.Token
import java.lang.RuntimeException

class RuntimeError(val token : Token, message : String) : RuntimeException(message)