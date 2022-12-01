import TokenType.*

class Scanner {
    private val source: String
    val tokens: List<Token> by lazy { scanTokens() }

    val keywords = mapOf(
        "and"    to AND,
        "class"  to CLASS,
        "else"   to ELSE,
        "false"  to FALSE,
        "for"    to FOR,
        "fun"    to FUN,
        "if"     to IF,
        "nil"    to NIL,
        "or"     to OR,
        "print"  to PRINT,
        "return" to RETURN,
        "super"  to SUPER,
        "this"   to THIS,
        "true"   to TRUE,
        "var"    to VAR,
        "while"  to WHILE,
    )

    constructor(source: String) {
        this.source = source
    }

    private fun scanTokens(): List<Token> {
        var start = 0
        var current = 0
        var line = 1
        val tokens = mutableListOf<Token>()

        val alphaChars = ('a'..'z') + ('A'..'Z') + listOf('_')
        val alphaNumericChars = alphaChars + ('0'..'9')

        val isAtEnd: () -> Boolean = { current >= this.source.length }
        val advance: () -> Char = {
            current += 1
            source[current-1]
        }
        fun text() = source.substring(start, current)
        fun addToken(type: TokenType, literal: Any? = null) {
            tokens.add(Token(type, text(), literal, line))
        }

        fun match(expected: Char): Boolean {
            if (isAtEnd()) return false
            if (source[current] != expected) return false

            current++
            return true
        }

        fun peek(): Char {
            if (isAtEnd()) return Char.MIN_VALUE
            return source[current]
        }

        fun peekNext(): Char {
            if (current + 1 > source.length) return Char.MIN_VALUE
            return source[current + 1]
        }

        fun string(): Token? {
            while (peek() != '"' && !isAtEnd()) {
                if (peek() == '\n') line++
                advance()
            }

            if (isAtEnd()) {
                Lox.error(line, "Unterminated string.")
                return null
            }

            // The closing "
            advance()

            val value = source.substring(start+1, current-1)
            return Token(STRING, text(), value, line)
        }

        fun number(): Token {
            while (peek().isDigit()) advance()

            // Look for a fractional part
            if (peek() == '.' && peekNext().isDigit()) {
                // Consume the "."
                advance()

                while (peek().isDigit()) advance()
            }

            return Token(NUMBER, text(), source.substring(start, current).toDouble(), line)
        }

        fun identifier(): TokenType {
            while (peek() in alphaNumericChars) {
                advance()
            }
            return keywords[text()] ?: IDENTIFIER
        }

        while (!isAtEnd()) {
            start = current
            val token: Any? = when (advance()) {
                '(' -> LEFT_PAREN
                ')' -> RIGHT_PAREN
                '{' -> LEFT_BRACE
                '}' -> RIGHT_BRACE
                ',' -> COMMA
                '.' -> DOT
                '-' -> MINUS
                '+' -> PLUS
                ';' -> SEMICOLON
                '*' -> STAR

                '!' -> if (match('=')) BANG_EQUAL else BANG
                '=' -> if (match('=')) EQUAL_EQUAL else EQUAL
                '<' -> if (match('=')) LESS_EQUAL else LESS
                '>' -> if (match('=')) GREATER_EQUAL else GREATER
                '/' -> {
                    if (match('/')) {
                        while (peek() != '\n' && !isAtEnd()) {
                            advance()
                        }
                    } else {
                        SLASH
                    }
                }

                ' ', '\r', '\t' -> null
                '\n' -> line++

                '"' -> string()

                in '0'..'9' -> number()

                in alphaChars -> identifier()

                else -> Lox.error(line, "Unexpected character.")
            }

            when (token) {
                is TokenType -> addToken(token)
                is Token -> tokens.add(token)
            }
        }

        return tokens
    }
}