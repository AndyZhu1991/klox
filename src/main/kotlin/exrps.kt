sealed class Expr {

    data class Literal(
        val value: Any?
    ): Expr()

    data class Grouping(
        val expression: Expr,
    ): Expr()

    data class Unary(
        val operator: Token,
        val right: Expr,
    ): Expr()

    data class Binary(
        val left: Expr,
        val operator: Token,
        val right: Expr,
    ): Expr()

    data class Variable(
        val name: Token,
    ): Expr()

    data class Assign(
        val name: Token,
        val value: Expr,
    ): Expr()

    data class Logical(
        val left: Expr,
        val operator: Token,
        val right: Expr,
    ): Expr()

    data class Call(
        val callee: Expr,
        val paren: Token,
        val arguments: List<Expr>,
    ): Expr()

    data class Get(
        val obj: Expr,
        val name: Token,
    ): Expr()

    data class Set(
        val obj: Expr,
        val name: Token,
        val value: Expr,
    ): Expr()

    data class This(
        val keyword: Token,
    ): Expr()
}


interface ExprVisitor<R> {
    fun visitLiteral(expr: Expr.Literal): R
    fun visitGrouping(expr: Expr.Grouping): R
    fun visitUnary(expr: Expr.Unary): R
    fun visitBinary(expr: Expr.Binary): R
    fun visitVariable(expr: Expr.Variable): R
    fun visitAssignExpr(expr: Expr.Assign): R
    fun visitLogicalExpr(expr: Expr.Logical): R
    fun visitCallExpr(expr: Expr.Call): R
    fun visitGetExpr(expr: Expr.Get): R
    fun visitSetExpr(expr: Expr.Set): R
    fun visitThisExpr(expr: Expr.This): R
}

fun <R> visitExpr(expr: Expr, visitor: ExprVisitor<R>): R {
    return when(expr) {
        is Expr.Literal -> visitor.visitLiteral(expr)
        is Expr.Binary -> visitor.visitBinary(expr)
        is Expr.Grouping -> visitor.visitGrouping(expr)
        is Expr.Unary -> visitor.visitUnary(expr)
        is Expr.Variable -> visitor.visitVariable(expr)
        is Expr.Assign -> visitor.visitAssignExpr(expr)
        is Expr.Logical -> visitor.visitLogicalExpr(expr)
        is Expr.Call -> visitor.visitCallExpr(expr)
        is Expr.Get -> visitor.visitGetExpr(expr)
        is Expr.Set -> visitor.visitSetExpr(expr)
        is Expr.This -> visitor.visitThisExpr(expr)
    }
}
