

object PrintVisitor: ExprVisitor<String> {
    override fun visitLiteral(expr: Literal): String {
        return if (expr.value == null) "nil" else expr.value.toString()
    }

    override fun visitGrouping(expr: Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitUnary(expr: Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visitBinary(expr: Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitVariable(expr: Variable): String {
        return expr.name.lexeme
    }

    override fun visitAssignExpr(expr: Assign): String {
        return "(Assign ${expr.value} to ${expr.name.lexeme})"
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        return  "(" +
                name + " " +
                exprs.joinToString(separator = " ") { visitExpr(it, this) } +
                ")"
    }
}

fun main(args: Array<String>) {
    val expr = Binary(
        Unary(
            Token(TokenType.MINUS, "-", null, 1),
            Literal(123)
        ),
        Token(TokenType.STAR, "*", null, 1),
        Grouping(
            Literal(45.67)
        )
    )
    println(visitExpr(expr, PrintVisitor))
}
