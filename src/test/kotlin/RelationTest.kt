import pipeline.Pipeline
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.Test

class RelationTest {
    @Test
    fun testInRelationTrivial() {
        interpret(
            """
            description: 
                new A;new B;
            prove:
                segment AB in AB
                ray AB in AB
                A in segment AB
                A in ray AB
                segment AB in ray AB
                ray AB in AB
            solution:;
        """.trimIndent())
    }

    @Test
    fun testParallelRelation() {
        interpret("""
            description:
                new A;new B;new C; new D
                AD parallel segment BC
            prove:
                AD parallel BC
                segment AD parallel segment BC
                ray DA || segment CB
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

    private fun interpret(code: String): SymbolTable {
        val pipeline = Pipeline()
        pipeline.addTheoremsFromFile().parse(code).interpret()
        val symbolTableField = pipeline.interpreter::class.memberProperties.find { it.name == "symbolTable" }!!
        symbolTableField.isAccessible = true
        return symbolTableField.getter.call(pipeline.interpreter) as SymbolTable
    }
}
