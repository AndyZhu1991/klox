import TokenType.*

class Interpreter : ExprVisitor<Any?>, StmtVisitor<Unit> {

    public val globals = Environment()
    private var environment = globals
    private val locals = mutableMapOf<Expr, Int>()

    init {
        globals.define("clock", object : LoxCallable {
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return System.currentTimeMillis() / 1000.0
            }

            override fun arity(): Int = 0

            override fun toString() = "<native fn>"
        })
    }

    fun interpret(stmts: List<Stmt>) {
        try {
            stmts.forEach(::execute)
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    fun executeBlock(block: List<Stmt>, environment: Environment) {
        val prevEnv = this.environment
        try {
            this.environment = environment
            block.forEach(::execute)
        } finally {
            this.environment = prevEnv
        }
    }

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    private fun execute(stmt: Stmt) {
        visitStmt(stmt, this)
    }

    private fun evaluate(expr: Expr): Any? {
        return visitExpr(expr, this)
    }

    override fun visitLiteral(expr: Expr.Literal) = expr.value

    override fun visitGrouping(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitUnary(expr: Expr.Unary): Any? {
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

    override fun visitBinary(expr: Expr.Binary): Any? {
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

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }
        return value
    }

    override fun visitVariable(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }

    private fun lookupVariable(name: Token, expr: Expr): Any? {
        val distance = locals[expr]
        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals.get(name)
        }
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        return when (expr.operator.type) {
            OR  -> return isTruthy(evaluate(expr.left)) || isTruthy(evaluate(expr.right))
            AND -> return isTruthy(evaluate(expr.left)) && isTruthy(evaluate(expr.right))
            else -> null
        }
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)
        val arguments = expr.arguments.map(::evaluate)
        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        } else {
            if (arguments.size != callee.arity()) {
                throw RuntimeError(expr.paren, "Expected ${callee.arity()} arguments but got ${arguments.size}.")
            }
            return callee.call(this, arguments)
        }
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val obj = evaluate(expr.obj)
        if (obj is LoxInstance) {
            return obj.get(expr.name)
        }
        throw RuntimeError(expr.name, "Only instance have properties.")
    }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val obj = evaluate(expr.obj)

        if (obj is LoxInstance) {
            val value = evaluate(expr.value)
            obj.set(expr.name, value)
            return value
        } else {
            throw RuntimeError(expr.name, "Only instance have fields.")
        }
    }

    override fun visitThisExpr(expr: Expr.This): Any? {
        return lookupVariable(expr.keyword, expr)
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        val value = stmt.value?.let { evaluate(it) }
        throw Return(value)
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
        pushEnvironment()
        try {
            stmt.stmts.forEach(::execute)
        } finally {
            popEnvironment()
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

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        environment.define(stmt.name.lexeme, null)
        val methods = mutableMapOf<String, LoxFunction>()
        stmt.methods.forEach {
            methods[it.name.lexeme] = LoxFunction(it, environment, it.name.lexeme == "init")
        }
        val klass = LoxClass(stmt.name.lexeme, methods)
        environment.assign(stmt.name, klass)
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

    private fun pushEnvironment() {
        this.environment = Environment(this.environment)
    }

    private fun popEnvironment() {
        this.environment = this.environment.enclosing!!
    }
}