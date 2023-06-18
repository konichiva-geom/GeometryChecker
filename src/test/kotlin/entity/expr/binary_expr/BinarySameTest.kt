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
            new alpha;
            new omega;
            alpha == omega
        prove:
            alpha == omega
            alpha === omega
        solution:
            
        """
        )
    }

    @Test
    fun testTriangleEquals() {
        passTask(
            """
        description:
            new ABC; new ABD; new AEF
            ABC == ABD
        prove:
            ABD == ABC
            A != B; A != C; A != D;  A != E; A != F;
            B != C; B != D;
            E != F;
            A !in line BC; A !in line EF; A !in line BD
            B !in line AC; B !in line AD
            C !in line AB;
            D !in line AB;
            E !in line AF;
            F !in line AE
            ∠ABD == ∠ABC; ∠BAD == ∠BAC; ∠ADB == ∠ACB
            AB == AB; AC == AD; BC == BD
        solution:
            
        """
        )
    }

    @Test
    fun failTriangleEqualsWithOtherOperand() {
        failTask(
            """
        description:
            new ABC; new ABD
            ABC == ABD + 1
        prove:
            ABD == ABC
        solution:
        """, "Triangle equations should not contain any expressions besides triangles"
        )
    }
}
