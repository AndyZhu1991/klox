class LoxClass(
    val name: String,
    val methods: Map<String, LoxFunction>,
): LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return LoxInstance(this).apply {
            findMethod("init")
                ?.bind(this)
                ?.call(interpreter, arguments)
        }
    }

    override fun arity(): Int {
        return findMethod("init")?.arity() ?: 0
    }

    override fun toString(): String {
        return name
    }

    fun findMethod(name: String): LoxFunction? {
        return methods[name]
    }
}