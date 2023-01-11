interface LoxCallable {
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?

    fun arity(): Int
}


class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment,
    private val isInitializer: Boolean,
) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        declaration.parameters.forEachIndexed { index, token ->
            environment.define(token.lexeme, arguments[index])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            if (isInitializer) return closure.getAt(0, "this")
            return returnValue.value
        }

        if (isInitializer) {
            return closure.getAt(0, "this")
        }

        return null
    }

    override fun arity() = declaration.parameters.size

    override fun toString() = "<fn ${declaration.name.lexeme}>"

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }
}