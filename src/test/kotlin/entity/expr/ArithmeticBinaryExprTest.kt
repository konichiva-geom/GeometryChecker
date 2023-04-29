package entity.expr

import TestFactory.passDescription
import kotlin.test.Test

internal class ArithmeticBinaryExprTest {
    @Test
    fun testVectorMerge() {
        passDescription("""
            new A; new B; new C; new D;
            3 - (AB + 4) == 2
            3 * AB == BC + 90
        """)
    }
}
