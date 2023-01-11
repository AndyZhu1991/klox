import java.util.Stack

class Resolver(
    private val interpreter: Interpreter,
): ExprVisitor<Unit>, StmtVisitor<Unit> {
    private val scopes: Stack<MutableMap<String, Boolean>> = Stack()
    private var currentFunctionType = FunctionType.NONE
    private var currentClass = ClassType.NONE

    fun resolve(stmt: Stmt) {
        visitStmt(stmt, this)
    }

    fun resolve(statements: List<Stmt>) {
        statements.forEach {
            visitStmt(it, this)
        }
    }

    private fun resolve(expr: Expr) {
        visitExpr(expr, this)
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return
        val scope = scopes.peek()
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Already variable with this name in this scope.")
        }
        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.peek()[name.lexeme] = true
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in (scopes.size-1) downTo 0) {
            if (scopes[i].contains(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    private fun resolveFunction(function: Stmt.Function, functionType: FunctionType) {
        val enclosingFunctionType = currentFunctionType
        currentFunctionType = functionType
        beginScope()
        function.parameters.forEach {
            declare(it)
            define(it)
        }
        resolve(function.body)
        endScope()
        currentFunctionType = enclosingFunctionType
    }

    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    private fun endScope() {
        scopes.pop()
    }

    override fun visitLiteral(expr: Expr.Literal) = Unit

    override fun visitGrouping(expr: Expr.Grouping) {
        resolve(expr.expression)
    }

    override fun visitUnary(expr: Expr.Unary) {
        resolve(expr.right)
    }

    override fun visitBinary(expr: Expr.Binary) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitVariable(expr: Expr.Variable) {
        if (!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false) {
            Lox.error(expr.name, "Can't read local variable in its own initializer.")
        }
        resolveLocal(expr, expr.name)
    }

    override fun visitAssignExpr(expr: Expr.Assign) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
    }

    override fun visitLogicalExpr(expr: Expr.Logical) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitCallExpr(expr: Expr.Call) {
        resolve(expr.callee)
        expr.arguments.forEach { resolve(it) }
    }

    override fun visitGetExpr(expr: Expr.Get) {
        resolve(expr.obj)
    }

    override fun visitEmptyStmt(stmt: Stmt.Empty) {
        TODO("Not yet implemented")
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        resolve(stmt.expr)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        resolve(stmt.expr)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        declare(stmt.name)
        stmt.initializer?.let { resolve(it) }
        define(stmt.name)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        beginScope()
        resolve(stmt.stmts)
        endScope()
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        stmt.elseBranch?.let { resolve(it) }
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        declare(stmt.name)
        define(stmt.name)
        resolveFunction(stmt, FunctionType.FUNCTION)
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        if (currentFunctionType == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.")
        }
        stmt.value?.let {
            if (currentFunctionType == FunctionType.INITIALIZER) {
                Lox.error(stmt.keyword, "Can't return a value from an initializer.")
            }
            resolve(it)
        }
    }

    override fun visitSetExpr(expr: Expr.Set) {
    }

    override fun visitThisExpr(expr: Expr.This) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "Can't use 'this' outside of a class.")
            return
        }
        resolveLocal(expr, expr.keyword)
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        val enclosingClass = currentClass
        currentClass = ClassType.CLASS
        declare(stmt.name)
        define(stmt.name)
        beginScope()
        scopes.peek()["this"] = true
        stmt.methods.forEach {
            val declaration =
            resolveFunction(it, FunctionType.METHOD)
        }
        endScope()
        currentClass = enclosingClass
    }
}

enum class FunctionType {
    NONE,
    FUNCTION,
    INITIALIZER,
    METHOD,
}

enum class ClassType {
    NONE,
    CLASS,
}