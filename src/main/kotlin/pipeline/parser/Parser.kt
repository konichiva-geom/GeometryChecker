package pipeline.parser

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.LiteralToken
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.st.LiftToSyntaxTreeOptions
import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.st.liftToSyntaxTreeGrammar
import com.github.h0tk3y.betterParse.utils.Tuple2
import entity.expr.Expr
import error.PosError
import error.SpoofError
import pipeline.inference.Inference
import utils.ExtensionUtils.toRange
import utils.ExtensionUtils.toViewable
import utils.MathUtils.min

val filteredFailures = mutableSetOf<Token>(LiteralToken("thDefStart", "th"))

@Suppress("UNCHECKED_CAST")
open class Parser {
    fun parseInference(code: String): SyntaxTree<List<Inference>> {
        return parse(code) as SyntaxTree<List<Inference>>
    }

    fun parseSolution(code: String): SyntaxTree<List<Tuple2<Any, List<Expr>>>> {
        return parse(code) as SyntaxTree<List<Tuple2<Any, List<Expr>>>>
    }

    /**
     * Parse input and return syntax tree
     * @param code parsed input
     * @param smartExceptions whether to convert exceptions to human-readable
     */
    private fun parse(code: String, smartExceptions: Boolean = true): SyntaxTree<Any> {
        try {
            return GeomGrammar.liftToSyntaxTreeGrammar(LiftToSyntaxTreeOptions(retainSeparators = false))
                .parseToEnd(code)
        } catch (e: ParseException) {
            if (!smartExceptions)
                throw e
            if (e.errorResult is UnparsedRemainder) {
                val token = (e.errorResult as UnparsedRemainder).startsWith
                val remainderLength = token.input.length - token.offset
                throw PosError(
                    IntRange(token.offset, token.input.length),
                    "Couldn't parse input, starting with: %{text}",
                    "text" to token.input.substring(token.offset - 1, token.offset - 1 + min(20, remainderLength))
                )
            }
            val tokens = getAllErrorTokens(e.errorResult as AlternativesFailure)
            chooseFurthestUnexpectedToken(tokens)
            throw findProblemToken(e.errorResult as AlternativesFailure)
        }
    }

    protected fun getAllErrorTokens(error: AlternativesFailure): List<MismatchedToken> {
        val res = mutableListOf<MismatchedToken>()
        for (err in error.errors) {
            when (err) {
                is AlternativesFailure -> res.addAll(getAllErrorTokens(err))
                is MismatchedToken -> res.add(err)
                is UnexpectedEof -> throw SpoofError(
                    "Expected %{token}, got end of program",
                    "token" to err.expected
                )

                is NoMatchingToken -> {
                    val realTokenEnd = err.tokenMismatch.text.indexOfAny(charArrayOf(' ', '\n', '\t', '\r'))
                    val realToken = if (realTokenEnd == -1) err.tokenMismatch.text
                    else err.tokenMismatch.text.substring(0, realTokenEnd)
                    throw PosError(
                        err.tokenMismatch.offset..
                                (err.tokenMismatch.offset + realToken.length),
                        "Tokenization error. No token for %{token}", "token" to realToken
                    )
                }

                is UnparsedRemainder -> throw PosError(
                    err.startsWith.toRange(),
                    "Parse Error. Couldn't parse remainder"
                )
            }
        }
        return res
    }

    /**
     * Trying to find problem token, with a hypothesis:
     * 1. any error has only one child of type AlternativesFailure
     * 2. the deepest child is a problem token that we are searching for
     *
     * This method throws user-readable exception
     */
    protected tailrec fun findProblemToken(error: AlternativesFailure): PosError {
        val tokenizationError = error.errors.filterIsInstance<NoMatchingToken>().firstOrNull()
        if (tokenizationError != null) {
            return PosError(tokenizationError.tokenMismatch.toRange(), "TokenizationError")
        }
        val nextLevel = error.errors.filterIsInstance<AlternativesFailure>()
        if (nextLevel.isEmpty()) {
            val foundToken = (error.errors.last() as MismatchedToken).found
            val expectedTokens = (error.errors.map { (it as MismatchedToken).expected.toViewable() }.distinct())
            val th = PosError(
                IntRange(foundToken.offset, foundToken.length),
                "Expected %{expected}, got %{got}",
                "expected" to expectedTokens.joinToString(separator = " or "),
                "got" to if (Regex("\\s+").matches(foundToken.text)) "linebreak" else foundToken
            )
            th.toString()
            return th
        }
        return findProblemToken(nextLevel.first())
    }

    protected fun chooseFurthestUnexpectedToken(errors: List<MismatchedToken>) {
        val furthest = errors.maxBy { it.found.offset }
        val fit = errors.filter { it.found.offset == furthest.found.offset }
        throw PosError(
            furthest.found.toRange(),
            "Expected %{expected}, got %{got}",
            "expected" to fit.map { it.expected.toViewable() }.distinct().sorted().joinToString(separator = " or "),
            "got" to if (Regex("\\s+").matches(furthest.found.text)) "`linebreak`" else furthest.found.text
        )
    }
}