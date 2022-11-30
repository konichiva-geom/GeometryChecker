import com.github.h0tk3y.betterParse.lexer.CharToken
import com.github.h0tk3y.betterParse.lexer.LiteralToken
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.AlternativesFailure
import com.github.h0tk3y.betterParse.parser.MismatchedToken
import com.github.h0tk3y.betterParse.parser.NoMatchingToken
import com.github.h0tk3y.betterParse.parser.UnexpectedEof
import com.github.h0tk3y.betterParse.parser.UnparsedRemainder

const val REPLACED = "%{}"
val normalFailures = mutableSetOf<Token>(LiteralToken("thDefStart", "th"))

/**
 * msg %{} is changed to args
 */
open class SpoofError(var msg: String, private vararg val args: Any) : Exception() {
    override val message: String
        get() = changeMessage()

    fun changeMessage(): String {
        val res = StringBuilder(msg)
        var index = msg.indexOf(REPLACED, 0)
        val ranges = mutableListOf<IntRange>()
        while (index != -1) {
            ranges.add(IntRange(index, index + REPLACED.length))
            index = msg.indexOf(REPLACED, index + 1)
        }
        if (ranges.size != args.size)
            throw Exception("Expected ${ranges.size} args, got ${args.size}")
        for (i in args.lastIndex downTo 0)
            res.replace(ranges[i].first, ranges[i].last, args[i].toString())
        return res.toString()
    }
}

class PosError(val range: IntRange, msg: String, vararg args: Any) : SpoofError(msg, *args) {
    override val message: String
        get() = super.message + " at $range"
}

/**
 * This error means that there is some fatal flaw in the design.
 * When it happens in the production code, it sends a message to dev mail.
 */
class SystemFatalError(val msg: String) : Exception() {
    override val message: String
        get() = "Something really bad happened X(. Sending message to devs.\n$msg"
}

fun getAllErrorTokens(error: AlternativesFailure): List<MismatchedToken> {
    val res = mutableListOf<MismatchedToken>()
    for (err in error.errors) {
        when (err) {
            is AlternativesFailure -> res.addAll(getAllErrorTokens(err))
            is MismatchedToken -> res.add(err)
            is UnexpectedEof -> throw SpoofError(
                "Expected %{}, got end of program",
                err.expected
            )

            is NoMatchingToken -> {
                val realTokenEnd = err.tokenMismatch.text.indexOfAny(charArrayOf(' ', '\n', '\t', '\r'))
                val realToken = if (realTokenEnd == -1) err.tokenMismatch.text
                else err.tokenMismatch.text.substring(0, realTokenEnd)
                throw PosError(
                    err.tokenMismatch.offset..
                        (err.tokenMismatch.offset + realToken.length),
                    "Tokenization error. No token for %{}", realToken
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
tailrec fun findProblemToken(error: AlternativesFailure) {
    val tokenizationError = error.errors.filterIsInstance<NoMatchingToken>().firstOrNull()
    if (tokenizationError != null) {
        val thrown = PosError(tokenizationError.tokenMismatch.toRange(), "TokenizationError")
        throw thrown
    }
    val nextLevel = error.errors.filterIsInstance<AlternativesFailure>()
    if (nextLevel.isEmpty()) {
        val foundToken = (error.errors.last() as MismatchedToken).found
        val expectedTokens = (error.errors.map { (it as MismatchedToken).expected.toViewable() }.distinct())
        val th = PosError(
            IntRange(foundToken.offset, foundToken.length),
            "Expected %{}, got %{}",
            expectedTokens.joinToString(separator = " or "),
            if (Regex("\\s+").matches(foundToken.text)) "linebreak" else foundToken
        )
        th.toString()
        throw th
    }
    // if (nextLevel.size != 1)
    //     throw SystemFatalError("Hypothesis failed. Got ${nextLevel.size} alternatives, expected 1")
    findProblemToken(nextLevel.first())
}

fun chooseFurthestUnexpectedToken(errors: List<MismatchedToken>) {
    val furthest = errors.maxBy { it.found.offset }
    val fit = errors.filter { it.found.offset == furthest.found.offset }
    throw PosError(
        furthest.found.toRange(),
        "Expected %{}, got %{}",
        fit.map { it.expected.toViewable() }.distinct().sorted().joinToString(separator = " or "),
        if (Regex("\\s+").matches(furthest.found.text)) "`linebreak`" else furthest.found.text
    )
}

fun Token.toViewable(): String {
    return when (this) {
        is LiteralToken -> "'${this.text}'"
        is CharToken -> "'${this.text}'"
        else -> "${this.name!!}:Token"
    }
}

fun TokenMatch.toRange(): IntRange {
    return IntRange(offset, length + offset - 1)
}