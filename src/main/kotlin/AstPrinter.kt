

object PrintVisitor: ExprVisitor<String> {
    override fun visitLiteral(expr: Expr.Literal): String {
        return if (expr.value == null) "nil" else expr.value.toString()
    }

    override fun visitGrouping(expr: Expr.Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitUnary(expr: Expr.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visitBinary(expr: Expr.Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitVariable(expr: Expr.Variable): String {
        return expr.name.lexeme
    }

    override fun visitAssignExpr(expr: Expr.Assign): String {
        return "(Assign ${expr.value} to ${expr.name.lexeme})"
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        return  "(" +
                name + " " +
                exprs.joinToString(separator = " ") { visitExpr(it, this) } +
                ")"
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        TODO("Not yet implemented")
    }
}

fun main(args: Array<String>) {
    val expr = Expr.Binary(
        Expr.Unary(
            Token(TokenType.MINUS, "-", null, 1),
            Expr.Literal(123)
        ),
        Token(TokenType.STAR, "*", null, 1),
        Expr.Grouping(
            Expr.Literal(45.67)
        )
    )
    println(visitExpr(expr, PrintVisitor))
}
