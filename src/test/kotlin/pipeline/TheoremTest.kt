package pipeline

import TestFactory.failTask
import TestFactory.passTask
import kotlin.test.Test

class TheoremTest {
    @Test
    fun testInnerTheorem() {
        passTask("""
        description:
            new A; new B; new C;
            new A1; new B1; new C1;
            A != B; A != C; B != C;
            A1 != B1; A1 != C1; B1 != C1;
            AB == A1B1; BC == B1C1; ∠ABC == ∠A1B1C1
            A !in line BC; B !in line AC; C !in line AB
            A1 !in line B1C1; B1 !in line A1C1; C1 !in line A1B1
        prove:
            ABC == A1B1C1
        solution:
            equal_triangles_1(AB == A1B1, BC == B1C1, ∠ABC == ∠A1B1C1)
        """)
    }
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
}
