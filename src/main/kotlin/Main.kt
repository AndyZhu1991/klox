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

    private var hadError = false


    fun runFile(path: String) {
        // Read the file
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes))

        // Indicate an error in the exit code.
        if (hadError) exitProcess(65)
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

        for (token in tokens) {
            println(token)
        }
    }


    private fun error(line: Int, message: String) {
        report(line, "", message)
    }


    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }


    companion object {
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
    }
}
