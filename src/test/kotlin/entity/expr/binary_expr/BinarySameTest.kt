package entity.expr.binary_expr

import TestFactory.failTask
import TestFactory.passTask
import kotlin.test.Test


internal class BinarySameTest {
    @Test
    fun failEqualsNotSame() {
        failTask("""
            description:
                new A; new B; new C; new D
                AB == CD
            prove:
                AB === CD
            solution:;
        """, "Relation AB == CD unknown")
    }

    @Test
    fun passSameThereforeEquals() {
        passTask("""
            description:
                new A; new B; new C; new D
                AB === CD
            prove:
                AB === CD
                AB == CD
            solution:;
        """)
    }

    @Test
    fun inferSameSegmentsFromSamePointsAfterEqual() {
        passTask("""
            description:
                new A; new B; new C; new D
                A == C
                B == D
                AB + CD == 3
                
            prove:
                AB === CD
                AB == CD
                AB == 3/2
            solution:;
        """)
    }

    @Test
    fun inferSameSegmentsFromSamePointsBeforeEqual() {
        passTask("""
            description:
                new A; new B; new C; new D
                AB + CD == 3
                A == C
                B == D
            prove:
                AB === CD
                AB == CD
                AB == 3/2
            solution:;
        """)
    }
}