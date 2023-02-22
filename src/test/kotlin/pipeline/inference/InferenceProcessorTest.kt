package pipeline.inference

import TestFactory.passDescription
import TestFactory.passTask
import kotlin.test.Test

internal class InferenceProcessorTest {
    @Test
    fun testInferenceInRelation() {
        passTask(
            """
            description:
                new A; new B; new C
                A in BC
            prove:
                A in ray BC
            solution:;
        """
        )
    }

    @Test
    fun testPerpendicularInference() {
        passTask("""
            description:
                new A; new B; new C;
                ABC == 90
            prove:
                AB perpendicular BC
            solution:;
        """)
    }
}