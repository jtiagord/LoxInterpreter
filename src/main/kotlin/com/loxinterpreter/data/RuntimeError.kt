package com.loxinterpreter.data

import java.lang.RuntimeException

class RuntimeError(val token : Token?, message : String) : RuntimeException(message)