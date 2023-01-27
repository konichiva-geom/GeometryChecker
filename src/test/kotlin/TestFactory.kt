import pipeline.Pipeline
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertFails
import kotlin.test.assertTrue

object TestFactory {
    private const val PRINT_ERRORS = true

    private fun interpret(code: String): SymbolTable {
        val pipeline = Pipeline()
        pipeline.addTheoremsFromFile().parse(code).interpret()
        val symbolTableField = pipeline.interpreter::class.memberProperties.find { it.name == "symbolTable" }!!
        symbolTableField.isAccessible = true
        return symbolTableField.getter.call(pipeline.interpreter) as SymbolTable
    }

    fun passBlock(code: String) {
        passDescription(code)
        passProve(code)
        passSolution(code)
    }

    fun passTask(code: String) {
        interpret(code)
    }

    fun passDescription(code: String) {
        passTask(
            """
            description:
                $code
            prove:;
            solution:;
        """
        )
    }

    fun passProve(code: String) {
        passTask(
            """
            description:;
            prove:
                $code
            solution:;
        """
        )
    }

    fun passSolution(code: String) {
        passTask(
            """
            description:;
            prove:;
            solution:
                $code
        """
        )
    }

    fun failBlock(code: String, expected: String) {
        failDescription(code, expected)
        failProve(code, expected)
        failSolution(code, expected)
    }

    fun failDescription(code: String, expected: String) {
        failTask(
            """
            description:
                $code
            prove:;
            solution:;
        """, expected
        )
    }

    fun failTask(
        code: String, expected: String, print: Boolean = PRINT_ERRORS
    ) {
        val exception = assertFails {
            interpret(code)
        }
        if (print) println(exception.message!!)
        assertTrue(exception.message!!.contains(expected))
    }

    fun failProve(code: String, expected: String) {
        failTask(
            """
            description:;
            prove:
                $code
            solution:;
        """, expected
        )
    }

    fun failSolution(code: String, expected: String) {
        failTask(
            """
            description:;
            prove:;
            solution:
                $code
        """, expected
        )
    }
}