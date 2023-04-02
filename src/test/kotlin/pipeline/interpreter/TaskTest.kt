package pipeline.interpreter

import TestFactory.passTask
import kotlin.test.Test

internal class TaskTest {
    @Test
    fun testEqualSidedTriangles() {
        passTask(
            """
            description:
                distinct U; distinct V; distinct W    //    U       X
                distinct X; distinct Y; distinct Z    //   / \     / \
                VUW == YXZ                            //  V---W   Y---Z
                VU == XY
                UW == ZX
            prove:
                VW == YZ
                UVW == XYZ
                UWV == XZY
            solution:
                equal_sided_triangles_i(VU == XY, UW == XZ, VUW == ZXY) => *
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
                ZYX == YXW             //     Y---Z
                YZ == WX
                X != Y
                X != Z
                X != W
                Y != Z
                Y != W
            prove:
                WY == XZ
                YWX == YZX
                YXW == XYZ
            solution:
                equal_sided_triangles_i(XY == XY, YZ == XW, ZYX == WXY) => *
        """.trimIndent()
        )
    }

    /**
     * Дан прямоугольный треугольник ABC, C = 90. Проведена медиана CM. Докажите, что 2CAB = CMB.
     */
    @Test
    fun testRectangularTriangleMedianTask() {
        passTask(
            """
        description:
            distinct A; distinct B; distinct C
            ACB == 90
            mid_point(new M, AB)
        prove:
            2CAB == CMB
        solution:
            rectangular_median_half_of_hypotenuse(ACB == 90, M in AB)
            isosceles_triangle_equal_angles(AM == MC)
            angles_180_in_triangle(AMC)
            adjacent_angle(AMC, BMC)
        """
        )
    }
}
