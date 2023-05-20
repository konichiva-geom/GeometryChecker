package entity.expr.binary_expr

import TestFactory.passDescription
import TestFactory.passTask
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

    @Test
    fun notEqualsTest() {
        passTask("""
        description:
            new A; new B; new C
            AB != AC + 1
        prove:
            AB != AC + 1
            AB - 1 != AC
            AC != AB - 1
        solution:
            
        """)
    }
}
