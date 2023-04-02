package entity.expr.binary_expr

import TestFactory.failTask
import TestFactory.passTask
import kotlin.test.Test


internal class BinarySameTest {
    @Test
    fun failEqualsNotSame() {
        failTask(
            """
            description:
                new A; new B; new C; new D
                AB == CD
            prove:
                AB === CD
            solution:;
        """, "Relation AB == CD unknown"
        )
    }

    @Test
    fun passSameThereforeEquals() {
        passTask(
            """
            description:
                new A; new B; new C; new D
                AB === CD
            prove:
                AB === CD
                AB == CD
            solution:;
        """
        )
    }

    @Test
    fun failDistinctToEqual() {
        failTask(
            """
        description:
           distinct A; distinct B 
           A == B
        prove:
            A == B
        solution:
            
        """, "Distinct points A, B cannot be made equal"
        )
    }

    @Test
    fun inferSameSegmentsFromSamePointsAfterEqual() {
        passTask(
            """
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
        """
        )
    }

    @Test
    fun inferSameSegmentsFromSamePointsBeforeEqual() {
        passTask(
            """
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
        """
        )
    }

    @Test
    fun testLineEquals() {
        passTask(
            """
        description:
            new A; new B; new C; new D
            line AB == line CD
        prove:
            line AB == line CD
        solution:
            
        """
        )
    }

    @Test
    fun testCircleEquals() {
        passTask(
            """
        description:
            
        prove:
            
        solution:
            
        """
        )
    }
}
