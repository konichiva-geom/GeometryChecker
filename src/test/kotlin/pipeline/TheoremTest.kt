package pipeline

import TestFactory.failTask
import TestFactory.passTask
import kotlin.test.Test

class TheoremTest {
    @Test
    fun failUnknownRelationInSignature() {
        failTask(
            """
        description:
            new A; new B; new C; new A1; new B1; new C1
        prove:
            
        solution:
            equal_triangles_1(AB == A1B1, BC == B1C1, ∠ABC == ∠A1B1C1)
        """, "Relation AB == A1B1 unknown"
        )
    }

    @Test
    fun failWrongReturnExpression() {
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
    rectangular_median_half_of_hypotenuse(∠ACB == 90, M in AB) => CM == AM
    isosceles_triangle_equal_angles(AM == CM) => ∠MAC == ∠MCA
    angles_180_in_triangle(MCA) => ∠ACM + ∠MAC + ∠AMC == 180
    adjacent_angle(∠AMC, ∠BMC) => ∠AMC + ∠BMC == ∠AMB, ∠AMB == 180
        """
        )
    }
}
