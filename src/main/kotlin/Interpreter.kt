import TokenType.*

class Interpreter : ExprVisitor<Any?> {

    fun interpret(expr: Expr) {
        try {
            val result = evaluate(expr)
            println(stringify(result))
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    private fun evaluate(expr: Expr): Any? {
        return visitExpr(expr, this)
    }

    override fun visitLiteral(expr: Literal) = expr.value

    override fun visitGrouping(expr: Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitUnary(expr: Unary): Any? {
        val right = evaluate(expr.right)
        return when(expr.operator.type) {
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }
            BANG -> {
                checkNumberOperand(expr.operator, right)
                !isTruthy(right)
            }
            else -> null  // Unreachable
        }
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        return true
    }

    override fun visitBinary(expr: Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when(expr.operator.type) {
            MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }
            PLUS  -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw RuntimeError(expr.operator, "Operands must be numbers or two strings.")
                }
            }
            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) / (right as Double)
            }
            STAR  -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }

            GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }
            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }
            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }
            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }
            BANG_EQUAL -> left != right
            EQUAL      -> left == right

            else -> null  // Unreachable
        }
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"

        if (obj is Double) {
            val text = obj.toString()
            return if (text.endsWith(".0")) {
                text.substring(0, text.length-2)
            } else {
                text
            }
        }

        return obj.toString()
    }
}