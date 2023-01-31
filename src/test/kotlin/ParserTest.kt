import TestFactory.failInference
import TestFactory.failTask
import TestFactory.passInference
import TestFactory.passTask
import kotlin.test.Test

internal class ParserTest {
    // region pipeline.inference
    @Test
    fun parseInference() {
        passInference("A in AB, any C => AB == AB")
    }

    @Test
    fun failAnyExprAtTheRight() {
        failInference("any A => any B", "any expressions are not allowed at the right side of the pipeline.inference")
    }
    // endregion

    @Test
    fun commentTest() {
        passTask(
            """
                //comment
            description:
            // comment
            new A; new B;
                    A in AB
                prove:
                    A in AB
                    
                    //comment
                solution:
                //
                //
                    check(A in AB)
                //comment
                
                    check(A in AB)
           
        """
        )
    }

    @Test
    fun failsNotApplicableOperator() {
        for (op in listOf("intersects", "parallel", "in", "perpendicular"))
            failTask("AB $op A", "`$op` is not applicable")
    }

    @Test
    fun failsAngleInRelations() {
        for (op in listOf("intersects", "parallel", "in", "perpendicular"))
            failTask("AB $op ABC", "`$op` is not applicable")
    }

    @Test
    fun failsInRelations() {
        failTask("AB in A", "is not applicable to points in this position")
        failTask("line AB in AB", "is 'smaller' than")
        failTask("omega in AB", "is not applicable to circle in this position")
        failTask(
            "arc AB of omega in line AB",
            "If arc is at the first position in `in`, then it should be in the second position too"
        )
        failTask(
            "line AB in arc AB of omega",
            "If arc is at the second position in `in`, then point or arc should be in the first position"
        )
    }
}