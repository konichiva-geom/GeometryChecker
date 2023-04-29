package pipeline

import TestFactory.failTask
import kotlin.test.Test

class TheoremTest {
    @Test
    fun failUnknownRelationInSignature() {
        failTask("""
        description:
            new A; new B; new C; new A1; new B1; new C1
        prove:
            
        solution:
            equal_sided_triangles_i(AB == A1B1, BC == B1C1, ∠ABC == ∠A1B1C1)
        """, "Relation AB == A1B1 unknown")
    }
}