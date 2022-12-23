import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.AlternativesFailure
import com.github.h0tk3y.betterParse.parser.ParseException
import pipeline.GeomGrammar
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ParserTest {
    @Test
    fun testParenthesesParse() {
    }

    @Test
    fun failsNotApplicableOperator() {
        for (op in listOf("intersects", "parallel", "in", "perpendicular"))
            defaultErrorTest(
                """
            description:
                AB $op A
            prove:
            
            solution:
        """.trimIndent(), "`$op` is not applicable"
            )
    }

    @Test
    fun failsAngleInRelations() {
        // TODO make it get a proper exception text
        for (op in listOf("intersects", "parallel", "in", "perpendicular"))
            defaultErrorTest(
                """
            description:
                AB $op ABC
            prove:
            
            solution:
        """.trimIndent(), "", true
            )
    }

    private fun defaultErrorTest(code: String, expected: String, print: Boolean = false) {
        val exception = assertFails {
            try {
                GeomGrammar.parseToEnd(code)
            } catch (e: ParseException) {
                val tokens = getAllErrorTokens(e.errorResult as AlternativesFailure)
                chooseFurthestUnexpectedToken(tokens)
                findProblemToken(e.errorResult as AlternativesFailure)
            }
        }
        if (print)
            println(exception.message!!)
        assertTrue(exception.message!!.contains(expected))
    }
}