class Environment {
    private val variables = HashMap<String, Any?>()
    fun define(name : String, value : Any?){
        variables[name] = value
    }
}