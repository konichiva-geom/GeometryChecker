package entity

import TestFactory.failTask
import TestFactory.passTask
import kotlin.test.Test

class PointTest {
    @Test
    fun failPointNotInstantiated() {
        failTask(
            """
        description:
           new A;
           angle ABC == angle BAC
        prove:
            
        solution:
            
        """, "Point B is not instantiated"
        )
    }

    @Test
    fun failPointNotInstantiatedInTriangle() {
        failTask(
            """
        description:
           new A;
           ABC == BAC
        prove:
            
        solution:
            
        """, "Point B is not instantiated"
        )
    }
}
