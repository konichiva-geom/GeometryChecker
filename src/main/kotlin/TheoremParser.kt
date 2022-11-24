import com.github.h0tk3y.betterParse.grammar.parseToEnd
import entity.Entity
import java.io.File

object TheoremParser {
    fun getTheorems(path:String = "examples/theorems.txt"): Any {
        return GeomGrammar.parseToEnd(File(path).readText())
    }
    fun parseTheorem() {}

    //fun traverseSignature(call, definition) {}
}