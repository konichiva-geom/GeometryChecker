package entity.expr.binary_expr

import TestFactory.failTask
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

        passTask("""
        description:

        prove:
            1 != 0
            1 != 2
        solution:

        """)

        failTask("""
        description:
            
        prove:
            10 + 5 != 15.0000000000001
        solution:
            
        """, "Expression is incorrect")
    }

    @Test
    fun testBigger() {
        passTask("""
        description:
            new A; new B
            AB > 12
        prove:
           AB >= 11
           AB > 11
           -12 > -AB
           1 > 0
           124234 > 321 - 21
           0 > -214
           AB - 12 > 0
           0 < 1
           321 - 21 < 3124
           0 < AB - 12
           -AB < -12
           AB >= 12
        solution:
            
        """)
    }

    @Test
    fun biggerOrEqualTest(){
        passTask("""
        description:
            new A; new B
            AB >= 1
        prove:
            AB > 0.5
            0 >= 0
            1 >= 0
            AB >= 0
            AB > 0
            1 <= AB
        solution:
            
        """)
    }
}
