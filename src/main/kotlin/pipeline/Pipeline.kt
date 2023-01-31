package pipeline

import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.utils.Tuple2
import entity.expr.Expr
import error.SpoofError
import pipeline.inference.InferenceProcessor
import pipeline.interpreter.Interpreter
import utils.Utils.THEOREMS_PATH
import java.io.File

/**
 * Builder-like class that is responsible for all API operations (also a facade)
 */
@Suppress("UNCHECKED_CAST")
class Pipeline {
    val parser = Parser()
    val inferenceProcessor = InferenceProcessor()
    val interpreter = Interpreter(inferenceProcessor)
    lateinit var tree: SyntaxTree<Any>

    // TODO: delete in production
    lateinit var code: String

    fun clearTheorems(): Pipeline {
        interpreter.theoremParser.clearTheorems()
        return this
    }

    fun addTheorems(theorems: String): Pipeline {
        interpreter.theoremParser.addTheorems(theorems)
        return this
    }

    fun addInference(code: String) {
        inferenceProcessor.setInference(parser.parseInference(code).item)
    }

    fun addInferenceFromFile(path: String) {
        inferenceProcessor.setInference(parser.parseInference(File(path).readText()).item)
    }

    fun addTheoremsFromFile(path: String = THEOREMS_PATH): Pipeline {
        addTheorems(File(path).readText())
        return this
    }

    fun parse(code: String): Pipeline {
        tree = parser.parseSolution(code)
        this.code = code
        return this
    }

    fun parseFile(path: String): Pipeline {
        tree = parser.parseSolution(File(path).readText())
        return this
    }

    fun interpretForProduction(): Pipeline {
        if (!this::tree.isInitialized)
            throw SpoofError("Parse code before interpreting")
        interpreter.interpret(tree as SyntaxTree<List<Tuple2<Any, List<Expr>?>>>)
        return this
    }

    fun interpret(): Pipeline {
        if (!this::tree.isInitialized)
            throw SpoofError("Parse code before interpreting")
        interpreter.interpret(tree as SyntaxTree<List<Tuple2<Any, List<Expr>?>>>)
        return this
    }
}
