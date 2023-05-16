package pipeline.interpreter

import TestFactory.passTask
import org.junit.Ignore
import kotlin.test.Test

internal class TaskTest {
    @Test
    fun testEqualSidedTriangles() {
        passTask(
            """
            description:
                distinct U; distinct V; distinct W    //    U       X
                distinct X; distinct Y; distinct Z    //   / \     / \
                ∠VUW == ∠YXZ                            //  V---W   Y---Z
                VU == XY
                UW == ZX
            prove:
                VW == YZ
                ∠UVW == ∠XYZ
                ∠UWV == ∠XZY
            solution:
                equal_triangles_1(VU == XY, UW == XZ, ∠VUW == ∠ZXY) => *
        """.trimIndent()
        )
    }

    /**
     * Reason why expression mapper has links now
     */
    @Test
    fun testEqualSidedTrianglesWithCommonPoints() {
        passTask(
            """
            description:
                new W                  //   W---X
                new X; new Y; new Z    //    \ / \
                ∠ZYX == ∠YXW             //     Y---Z
                YZ == WX
                X != Y
                X != Z
                X != W
                Y != Z
                Y != W
            prove:
                WY == XZ
                ∠YWX == ∠YZX
                ∠YXW == ∠XYZ
            solution:
                equal_triangles_1(XY == XY, YZ == XW, ∠ZYX == ∠WXY) => *
        """.trimIndent()
        )
    }

    /**
     * Дан прямоугольный треугольник ∠ABC, C = 90. Проведена медиана CM. Докажите, что 2∠CAB = ∠CMB.
     */
    @Test
    fun testRectangularTriangleMedianTask() {
        passTask(
            """
        description:
            distinct A; distinct B; distinct C
            ∠ACB == 90
            mid_point(new M, AB)
        prove:
            ∠ACM == ∠MAC
            ∠ACM == ∠BAC
            2∠CAB == ∠CMB
        solution:
            rectangular_median_half_of_hypotenuse(∠ACB == 90, M in AB)
            isosceles_triangle_equal_angles(AM == MC)
            angles_180_in_triangle(AMC)
            adjacent_angle(∠AMC, ∠BMC)
        """
        )
    }

    @Ignore("Construction of equal segment on another segment with > < relations")
    @Test
    fun testMedianHalfOfHypotenuseTask() {
        passTask(
            """
        description:
            distinct A; distinct B; distinct C
            ∠ACB == 90
            mid_point(new M, AB)
        prove:
            AM == MC
        solution:
            
        """
        )
    }

    @Test
    fun testUnnecessaryTheorem() {
        passTask("""
            description:
                distinct A; distinct B; distinct C
                AB == AC
            prove:
                AB == AC
            solution:
                isosceles_triangle_equal_angles(AB == AC)
        """)
    }
}
