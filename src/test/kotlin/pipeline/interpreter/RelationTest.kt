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
        """.trimIndent())
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
            solution:

        """.trimIndent()
        )
    }

    @Test
    fun testAssignment() {
        passTask(
            """
            description:
                new A; new B; new C; new D;
                AB intersects CD
                O = AB intersects CD
            prove:
                O == AB intersects CD
            solution:;
            """
        )
    }

    @Test
    fun testInference() {
        passTask("""
            description:
                new A; new B; new C;
                A in BC
            prove:
                A in line BC
                A in ray BC
            solution:;
        """)
    }

//    @Test
//    fun testInRelation() {
//        interpret("""
//            description:
//                new A
//                new B
//                new C
//                A in segment BC
//            prove:
//                A in BC
//                A in ray BC
//            solution:
//
//        """.trimIndent())
//    }
}
