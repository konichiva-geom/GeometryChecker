import pipeline.Parser
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ParserTest {
    // region inference
    @Test
    fun failAnyExprAtTheRight() {
        defaultErrorTest("any A => any B", "any expressions are not allowed at the right side of the inference", allCodeWritten = true)
    }
    // endregion

    @Test
    fun commentTest() {
        defaultPassTest(
            """
            // comment
            description:
            // comment
                    A in AB
                prove:
                    A in AB
                    
                    //comment
                solution:
                //
                //
                    A in AB
                //comment
                
                    A in AB
           
        """.trimIndent(), allCodeWritten = true)
    }

    @Test
    fun failsNotApplicableOperator() {
        for (op in listOf("intersects", "parallel", "in", "perpendicular"))
            defaultErrorTest("AB $op A", "`$op` is not applicable")
    }

    @Test
    fun failsAngleInRelations() {
        for (op in listOf("intersects", "parallel", "in", "perpendicular"))
            defaultErrorTest("AB $op ABC", "`$op` is not applicable")
    }

    @Test
    fun failsInRelations() {
        defaultErrorTest("segment AB in A", "is not applicable to points in this position")
        defaultErrorTest("AB in segment AB", "is 'smaller' than")
        defaultErrorTest("omega in segment AB", "is not applicable to circle in this position")
        defaultErrorTest(
            "arc AB of omega in segment AB",
            "If arc is at the first position in `in`, then it should be in the second position too"
        )
        defaultErrorTest(
            "AB in arc AB of omega",
            "If arc is at the second position in `in`, then point or arc should be in the first position"
        )
    }

    private fun defaultErrorTest(
        code: String,
        expected: String,
        allCodeWritten: Boolean = true,
        print: Boolean = true
    ) {
        val exception = assertFails {
            val parser = Parser()
            parser.parse(
                if (allCodeWritten) code else
                    """
                description:
                $code
                prove:
                    A in AB
                solution:
                    A in AB
            """.trimIndent()
            )
        }
        if (print)
            println(exception.message!!)
        assertTrue(exception.message!!.contains(expected))
    }

    private fun defaultPassTest(code: String, allCodeWritten: Boolean = false) {
        val parser = Parser()
        parser.parse(
            if (allCodeWritten) code else """
                description:
                $code
                prove:
                    A in AB
                solution:
                    A in AB
            """.trimIndent()
        )
    }
}