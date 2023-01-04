sealed class Stmt {
    object Empty : Stmt()
    class Print(val expr: Expr) : Stmt()
    class Expression(val expr: Expr) : Stmt()
    class Var(val name: Token, val initializer: Expr?) : Stmt()
    class Block(val stmts: List<Stmt>) : Stmt()
    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?): Stmt()
    class While(val condition: Expr, val body: Stmt): Stmt()
    class Function(val name: Token, val parameters: List<Token>, val body: List<Stmt>): Stmt()
    class Return(val keyword: Token, val value: Expr?): Stmt()
    class Class(val name: Token, val methods: List<Function>): Stmt()
}

interface StmtVisitor<R> {
    fun visitEmptyStmt(stmt: Stmt.Empty): R
    fun visitPrintStmt(stmt: Stmt.Print): R
    fun visitExpressionStmt(stmt: Stmt.Expression): R
    fun visitVarStmt(stmt: Stmt.Var): R
    fun visitBlockStmt(stmt: Stmt.Block): R
    fun visitIfStmt(stmt: Stmt.If): R
    fun visitWhileStmt(stmt: Stmt.While): R
    fun visitFunctionStmt(stmt: Stmt.Function): R
    fun visitReturnStmt(stmt: Stmt.Return): R
    fun visitClassStmt(stmt: Stmt.Class): R
}

fun <R> visitStmt(stmt: Stmt, visitor: StmtVisitor<R>): R {
    return when(stmt) {
        is Stmt.Empty -> visitor.visitEmptyStmt(stmt)
        is Stmt.Print -> visitor.visitPrintStmt(stmt)
        is Stmt.Expression -> visitor.visitExpressionStmt(stmt)
        is Stmt.Var -> visitor.visitVarStmt(stmt)
        is Stmt.Block -> visitor.visitBlockStmt(stmt)
        is Stmt.If -> visitor.visitIfStmt(stmt)
        is Stmt.While -> visitor.visitWhileStmt(stmt)
        is Stmt.Function -> visitor.visitFunctionStmt(stmt)
        is Stmt.Return -> visitor.visitReturnStmt(stmt)
        is Stmt.Class -> visitor.visitClassStmt(stmt)
    }
}