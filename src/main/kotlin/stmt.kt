sealed class Stmt {
    class Print(val expr: Expr) : Stmt()
    class Expression(val expr: Expr) : Stmt()
    class Var(val name: Token, val initializer: Expr?) : Stmt()
    class Block(val stmts: List<Stmt>) : Stmt()
}

interface StmtVisitor<R> {
    fun visitPrintStmt(stmt: Stmt.Print): R
    fun visitExpressionStmt(stmt: Stmt.Expression): R
    fun visitVarStmt(stmt: Stmt.Var): R
    fun visitBlockStmt(stmt: Stmt.Block): R
}

fun <R> visitStmt(stmt: Stmt, visitor: StmtVisitor<R>): R {
    return when(stmt) {
        is Stmt.Print -> visitor.visitPrintStmt(stmt)
        is Stmt.Expression -> visitor.visitExpressionStmt(stmt)
        is Stmt.Var -> visitor.visitVarStmt(stmt)
        is Stmt.Block -> visitor.visitBlockStmt(stmt)
    }
}