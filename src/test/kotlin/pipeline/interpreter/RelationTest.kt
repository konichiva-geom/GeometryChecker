package pipeline.interpreter

import TestFactory.passTask
import kotlin.test.Test

internal class RelationTest {
    @Test
    fun testInRelationTrivial() {
        passTask(
            """
            description: 
                new A;new B;
            prove:
                AB in line AB
                ray AB in line AB
                A in AB
                B in AB
                A in ray AB
                AB in ray AB
                ray AB in line AB
            solution:;
        """
        )
    }

    @Test
    fun testNotInRelation() {
        passTask("""
        description:
            new A; new B; new C
            A !in line BC
        prove:
            A !in line BC
            A !in ray BC
            A !in ray CB
            A !in BC
            B !in line AC
            C !in line AB
            C !in ray AB
            C !in ray BA
            C !in AB
            B !in ray AC
            B !in ray CA
            B !in AC
        solution:
            
        """)
    }

    @Test
    fun testParallelRelation() {
        passTask(
            """
            description:
                new A;new B;new C; new D
                line AD parallel BC
            prove:
                line AD parallel BC
                AD parallel BC
                ray DA || line CB
            solution:;
        """
        )
    }
    @Test
    fun parallelInfer() {
        passTask("""
        description:
           new A; new B; new C; new D 
            line AB || line CD
        prove:
            ray AB || ray CD
            AB || CD
        solution:
            
        """)
    }

    @Test
    fun testAssignment() {
        passTask(
            """
            description:
                new A; new B; new C; new D;
                new omega; new alpha
                AB ∩ CD
                O = AB ∩ CD
                (B, O) = omega ∩ AB
                (O, K) = omega ∩ alpha
            prove:
                O == AB ∩ CD
                K in alpha; K in omega; O in alpha; O in omega; B in omega
            solution:
                G = AB ∩ CD
                (E, F) = omega ∩ AB
                check(E in omega); check(E in AB); check(F in omega); check(F in AB)
                check(G in AB); check(G in CD)
            """
        )
    }

    @Test
    fun testInference() {
        passTask(
            """
            description:
                new A; new B; new C;
                A in BC
            prove:
                A in line BC
                A in ray BC
            solution:;
        """
        )
    }

    @Test
    fun testInRelation() {
        passTask(
            """
            description:
                new A
                new B
                new C
                A in BC
            prove:
                A in BC
                A in ray BC
            solution:;
        """
        )
    }
}
