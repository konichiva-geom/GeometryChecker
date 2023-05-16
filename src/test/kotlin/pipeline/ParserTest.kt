package pipeline

import TestFactory.failInference
import TestFactory.failTask
import TestFactory.passDescription
import TestFactory.passInference
import TestFactory.passTask
import entity.expr.Creation
import entity.expr.Relation
import entity.expr.binary_expr.BinaryAssignment
import entity.expr.binary_expr.BinaryIntersects
import entity.expr.notation.Point2Notation
import entity.expr.notation.PointNotation
import pipeline.parser.Parser
import kotlin.test.Ignore
import kotlin.test.Test

internal class ParserTest {
    // region pipeline.inference
    @Test
    fun parseInference() {
        passInference("A in AB, any C => AB == AB")
    }

    @Test
    fun parseTheorems() {
        val parser = Parser()

        parser.parseTheorems("""
            th equal_triangles_1(AB == A1B1, BC == B1C1, ∠ABC == ∠A1B1C1):
    check(A != B)
    check(A != C)
    check(B != C)
    check(A1 != B1)
    check(A1 != C1)
    check(B1 != C1)
    return AC == A1C1, ∠ACB == ∠A1C1B1, ∠BAC == ∠C1A1B1
    // triangle ABC == triangle A1B1C1 // are triangles needed???

th straight_angle(O, AB): // развернутый угол
    check(O in AB)
    return ∠AOB == 180
        """.trimIndent())

        parser.parseTheorems("""
            
th straight_angle(O, AB): // развернутый угол
    check(O in AB)
    return ∠AOB == 180
        """.trimIndent())

        parser.parseTheorems("""
/*
 * describe theorem here
 */            
th name(args):
/*
 * describe theorem here
 */
    return A
    /*
 * describe theorem here
 */
        """.trimIndent())
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
        for (op in listOf("intersects ", "parallel ", "in ", "perpendicular "))
            failTask("AB $op A", "`$op` is not applicable")
    }

    @Test
    fun failsAngleInRelations() {
        for (op in listOf("intersects ", "parallel ", "in ", "perpendicular "))
            failTask("AB $op ∠ABC", "`$op` is not applicable")
    }

    @Test
    fun failsInRelations() {
        failTask("AB in A", "is not applicable to points in this position")
        failTask("line AB in AB", "is 'smaller' than")
        failTask("omega in AB", "is not applicable to circle in this position")
        failTask(
            "arc AB of omega in line AB",
            "If arc is at the first position in `in `, then it should be in the second position too"
        )
        failTask(
            "line AB in arc AB of omega",
            "If arc is at the second position in `in `, then point or arc should be in the first position"
        )
    }

    @Ignore("make readable error for this situation")
    @Test
    fun failTwoBlocks() {
        passTask(
            """
            int:
                A in AB
            double:
                A in AB
        """
        )
    }
}
