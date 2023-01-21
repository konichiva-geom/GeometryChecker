import TestFactory.interpret
import kotlin.test.Test

class RelationTest {
    @Test
    fun testInRelationTrivial() {
        interpret(
            """
            description: 
                new A;new B;
            prove:
                AB in line AB
                ray AB in line AB
                A in AB
                A in ray AB
                AB in ray AB
                ray AB in line AB
            solution:;
        """.trimIndent())
    }

    @Test
    fun testParallelRelation() {
        interpret("""
            description:
                new A;new B;new C; new D
                line AD parallel BC
            prove:
                line AD parallel BC
                AD parallel BC
                ray DA || line CB
            solution:

        """.trimIndent())
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
