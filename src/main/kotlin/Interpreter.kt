import TokenType.*

class Interpreter : ExprVisitor<Any?>, StmtVisitor<Unit> {

    private var environment = Environment()

    fun interpret(stmts: List<Stmt>) {
        try {
            stmts.forEach(::execute)
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    private fun execute(stmt: Stmt) {
        visitStmt(stmt, this)
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

    override fun visitAssignExpr(expr: Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    override fun visitVariable(expr: Variable): Any? {
        return environment.get(expr.name)
    }

    override fun visitLogicalExpr(expr: Logical): Any? {
        return when (expr.operator.type) {
            OR  -> return isTruthy(evaluate(expr.left)) || isTruthy(evaluate(expr.right))
            AND -> return isTruthy(evaluate(expr.left)) && isTruthy(evaluate(expr.right))
            else -> null
        }
    }

    override fun visitEmptyStmt(stmt: Stmt.Empty) = Unit

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val result = evaluate(stmt.expr)
        println(stringify(result))
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expr)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = stmt.initializer?.let { evaluate(it) }
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        pushEnviroment()
        try {
            stmt.stmts.forEach(::execute)
        } finally {
            popEnviroment()
        }
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else {
            stmt.elseBranch?.let { execute(it) }
        }
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
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

    private fun pushEnviroment() {
        this.environment = Environment(this.environment)
    }

    private fun popEnviroment() {
        this.environment = this.environment.enclosing!!
    }
}