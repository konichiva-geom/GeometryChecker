import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.st.liftToSyntaxTreeGrammar
import com.github.h0tk3y.betterParse.utils.Tuple2
import expr.Expr
import pipeline.GeomGrammar
import pipeline.Interpreter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.Test

class RelationTest {
    @Test
    fun testRelationAdding() {
        interpret("""
            description:
                A in AB
            prove:
                A in AB
            solution:
                A in AB
        """.trimIndent())
    }

    fun interpret(code: String): SymbolTable {
        val interpreter = Interpreter()
        interpreter.interpret(
            GeomGrammar.liftToSyntaxTreeGrammar().parseToEnd(code) as SyntaxTree<List<Tuple2<Any, List<Expr>>>>
        )
        val symbolTableField = interpreter::class.memberProperties.find { it.name == "symbolTable" }!!
        symbolTableField.isAccessible = true
        return symbolTableField.get(interpreter as Nothing) as SymbolTable
    }
}
