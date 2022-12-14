class LoxInstance(
    val klass: LoxClass,
) {
    private val fields: MutableMap<String, Any?> = mutableMapOf()

    fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        }

        klass.findMethod(name.lexeme)?.let { return it }

        throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }

    fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }

    override fun toString(): String {
        return "${klass.name} instance"
    }
}