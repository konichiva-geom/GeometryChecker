package pipeline

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.utils.Tuple2
import entity.expr.Expr
import error.SpoofError
import pipeline.inference.Inference
import pipeline.inference.InferenceProcessor
import pipeline.interpreter.Interpreter
import pipeline.interpreter.Signature
import pipeline.interpreter.TheoremBody
import pipeline.parser.GeomGrammar
import pipeline.parser.Parser
import utils.CommonUtils.INFERENCE_PATH
import utils.CommonUtils.THEOREMS_PATH
import java.io.File

/**
 * Builder-like class that is responsible for all API operations (also a facade)
 */
@Suppress("UNCHECKED_CAST")
class Pipeline {
    private val parser = Parser()
    private val inferenceProcessor = InferenceProcessor()
    val interpreter = Interpreter(inferenceProcessor)
    private lateinit var tree: SyntaxTree<Any>
    private lateinit var code: String
    private lateinit var theoremsConcurrent: MutableMap<Signature, TheoremBody>
    private lateinit var inferenceConcurrent: MutableList<Inference>

    fun addConcurrentTheorems(path: String): Pipeline {
        theoremsConcurrent = (GeomGrammar.parseToEnd(File(path).readText())
                as List<Pair<Signature, TheoremBody>>).toMap().toMutableMap()
        return this
    }

    fun updateConcurrentTheorems(theorems: String): Pipeline {
        theoremsConcurrent.putAll(parser.parseTheorems(theorems))
        return this
    }

    fun getConcurrentTheorems(): Map<Signature, TheoremBody> {
        return theoremsConcurrent.toMap()
    }


    fun addConcurrentInference(path: String): Pipeline {
        inferenceConcurrent = parser.parseInference(File(path).readText()).toMutableList()
        return this
    }

    fun updateConcurrentInference(inference: String): Pipeline {
        inferenceConcurrent.addAll(parser.parseInference(inference))
        return this
    }

    fun getConcurrentInference(): List<Inference> {
        return inferenceConcurrent.toList()
    }

    fun clearTheorems(): Pipeline {
        interpreter.theoremParser.clearTheorems()
        return this
    }

    fun addTheorems(theorems: String): Pipeline {
        interpreter.theoremParser.addTheorems(theorems)
        return this
    }

    fun addInference(code: String): Pipeline {
        inferenceProcessor.setInference(parser.parseInference(code))
        return this
    }

    fun addInferenceFromFile(path: String = INFERENCE_PATH): Pipeline {
        inferenceProcessor.setInference(parser.parseInference(File(path).readText()))
        return this
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

    fun parseAndInterpretWithNewInterpreter(code: String) {
        val inferenceProcessor = InferenceProcessor()
        inferenceProcessor.setInference(inferenceConcurrent)
        val interpreter = Interpreter(inferenceProcessor)
        interpreter.theoremParser.addTheorems(theoremsConcurrent)
        interpreter.interpret(parser.parseSolution(code) as SyntaxTree<List<Tuple2<Any, List<Expr>?>>>)
    }
}
