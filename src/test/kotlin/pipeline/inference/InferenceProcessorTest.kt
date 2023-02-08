package pipeline.inference

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
}