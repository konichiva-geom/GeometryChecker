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
                ∠VUW == ∠YXZ                          //  V---W   Y---Z
                VU == XY
                UW == ZX
                V !in line UW
                U !in line VW
                W !in line UV
                X !in line YZ
                Y !in line ZX
                Z !in line YX
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
                ∠ZYX == ∠YXW           //     Y---Z
                YZ == WX
                X != Y
                X != Z
                X != W
                Y != Z
                Y != W
                X !in line YZ
                Y !in line XZ
                Z !in line XY
                W !in line XY
                X !in line WY
                Y !in line WX
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
            new ABC
            ∠ACB == 90
            mid_point(new M, AB)
        prove:
            ∠ACM == ∠MAC
            ∠ACM == ∠BAC
            2∠CAB == ∠CMB
        solution:
            point_differs_if_not_in_line(line AM, C) => A != C, M != C
            rectangular_median_half_of_hypotenuse(∠ACB == 90, M in AB) => MC == MA
            isosceles_triangle_equal_angles(AM == MC)
            angles_180_in_triangle(AMC) => * // angle AMC + angle CMA + angle CAM == 180
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

    /**
     * Задача о биссектрисах смежного угла
     *
     * На отрезке AC отмечена точка O. Проведен луч OB, при этом B не лежит на AC.
     * Проведены биссектрисы OK и ON углов AOB и BOC соответственно.
     *
     * Докажите, что KON = 90.
     */
    @Test
    fun testBisectorsOfAdjacentAngle() {
        passTask("""
        description:
            distinct A; distinct B; distinct C;
            distinct O
            O in AC
            B !in AC
            bisector(new K, ∠AOB)
            bisector(new N, ∠BOC)
            E = KN intersects BO
        prove:
            ∠KON == 90
        solution:
            adjacent_angle(∠AOB, ∠BOC)
            merge_angles_in_triangle(angle KOE, angle EON)
        """)
    }

    /**
     * Признак равнобедренного треугольника
     *
     * Докажите, что если два угла равны, то треугольник равнобедренный
     */
    @Test
    fun isoscelesByAngles() {
        passTask("""
        description:
            new ABC
            ∠CAB == ∠ABC
        prove:
            AC == BC
        solution:
            bisector(new D, angle ACB)
            point_differs_if_not_in_line(line AD, C)
            angles_180_in_triangle(ADC)
            angles_180_in_triangle(BDC)
            point_differs_if_not_in_line(line CD, A)
            point_differs_if_not_in_line(line CD, B)
            equal_triangles_2(CD == CD, ∠ACD == ∠BCD, ∠ADC == ∠BDC)
        """)
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

    @Test
    fun test193() {
        passTask("""
description:
    new ABC
    ∠BAC == 40
    ∠ABC == 70
    distinct D
    ∠CBD == ∠ABC
    AD ∩ BC
prove:
    AC || BD
solution:
    angles_180_in_triangle(ABC)
    cross_lying_angles(∠ACB == ∠CBD, ray AC, ray BD)""")
    }

    @Test
    fun testParallelogramm() {
        passTask("""
description:
    new ABC; new ACD
    O = AC ∩ BD
    A != O; O != D; C != O; O != B
    AD == BC
    DC == AB
    line AD || line BC
    line AB || line DC
prove:
    AO == OC
    DO == OB
solution:
    cross_lying_angles_in_parallel(ray AD || ray BC)
    cross_lying_angles_in_parallel(ray DA || ray CB)
    equal_triangles_2(AD == BC, ∠DAO == ∠OCB, ∠ADO == ∠OBC)
        """.trimIndent())
    }
}
