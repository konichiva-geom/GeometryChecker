import pipeline.Pipeline
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.Test

class RelationTest {
    @Test
    fun testRelationAdding() {
        interpret(
            """
            description:
                A in AB
                segment AB in AB
                ray AB in AB
                A in segment AB
                A in ray AB
                segment AB in ray AB
                ray AB in AB
                
                AB || CD1
                AB intersects CD
                AB perpendicular CD
            prove:
                A in AB
            solution:
                A in AB
        """.trimIndent()
        )
    }
    @Test fun testRelationHaving() {

    }

    private fun interpret(code: String): SymbolTable {
        val pipeline = Pipeline()
        pipeline.addTheoremsFromFile().parse(code).interpret()
        val symbolTableField = pipeline.interpreter::class.memberProperties.find { it.name == "symbolTable" }!!
        symbolTableField.isAccessible = true
        return symbolTableField.getter.call(pipeline.interpreter) as SymbolTable
    }
}
