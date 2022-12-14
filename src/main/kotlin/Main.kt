import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when (args.size) {
        0 -> Lox().runPrompt()
        1 -> Lox().runFile(args[0])
        else -> {
            println("Usage: klox [script]")
        }
    }
}


class Lox {

    fun runFile(path: String) {
        // Read the file
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes))

        // Indicate an error in the exit code.
        if (hadError) exitProcess(65)
        if (hadRuntimeError) exitProcess(70)
    }


    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }


    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.tokens
        val stmts = Parser(tokens).parse()
        if (hadError) return
        Resolver(interpreter).resolve(stmts)
        if (hadError) return
        interpreter.interpret(stmts)
    }


    private fun error(line: Int, message: String) {
        report(line, "", message)
    }


    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }


    companion object {
        private val interpreter = Interpreter()

        private var hadError = false
        private var hadRuntimeError = false

        fun error(line: Int, msg: String) {
        }

        fun error(token: Token, message: String) {
            if (token.type == TokenType.EOF) {
                report(token.line, " at end", message)
            } else {
                report(token.line, " at '${token.lexeme}'", message)
            }
        }

        fun report(line: Int, position: String, message: String) {
        }

        fun runtimeError(error: RuntimeError) {
            System.err.println("${error.message}\n[line ${error.token.line}]")
            hadRuntimeError = true
        }
    }
}
