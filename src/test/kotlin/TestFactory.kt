import pipeline.Pipeline
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object TestFactory {
    fun interpret(code: String): SymbolTable {
        val pipeline = Pipeline()
        pipeline.addTheoremsFromFile().parse(code).interpret()
        val symbolTableField = pipeline.interpreter::class.memberProperties.find { it.name == "symbolTable" }!!
        symbolTableField.isAccessible = true
        return symbolTableField.getter.call(pipeline.interpreter) as SymbolTable
    }
}