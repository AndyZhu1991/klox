sealed class Expr

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

interface ExprVisitor<R> {
    fun visitLiteral(expr: Literal): R
    fun visitGrouping(expr: Grouping): R
    fun visitUnary(expr: Unary): R
    fun visitBinary(expr: Binary): R
}

fun <R> visitExpr(expr: Expr, visitor: ExprVisitor<R>): R {
    return when(expr) {
        is Literal -> visitor.visitLiteral(expr)
        is Binary -> visitor.visitBinary(expr)
        is Grouping -> visitor.visitGrouping(expr)
        is Unary -> visitor.visitUnary(expr)
    }
}
