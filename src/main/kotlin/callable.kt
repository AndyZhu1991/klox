interface LoxCallable {
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?

    fun arity(): Int
}


class LoxFunction(
    private val declaration: Stmt.Function,
) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(interpreter.globals)
        declaration.parameters.forEachIndexed { index, token ->
            environment.define(token.lexeme, arguments[index])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun arity() = declaration.parameters.size

    override fun toString() = "<fn ${declaration.name.lexeme}>"
}