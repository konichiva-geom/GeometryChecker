import com.github.h0tk3y.betterParse.lexer.CharToken
import com.github.h0tk3y.betterParse.lexer.LiteralToken
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.AlternativesFailure
import com.github.h0tk3y.betterParse.parser.MismatchedToken
import com.github.h0tk3y.betterParse.parser.NoMatchingToken

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

class PosError(val range: IntRange, msg: String, vararg args: Any) : SpoofError(msg, args)

/**
 * This error means that there is some fatal flaw in the design.
 * When it happens in the production code, it sends a message to dev mail.
 */
class SystemFatalError(val msg: String) : Exception() {
    override val message: String
        get() = "Something really bad happened X(. Sending message to devs.\n$msg"
}

/**
 * Trying to find problem token, with a hypothesis:
 * 1. any error has only one child of type AlternativesFailure
 * 2. the deepest child is a problem token that we are searching for
 *
 * This method throws user-readable exception
 */
fun findProblemToken(error: AlternativesFailure) {
    val tokenizationError = error.errors.filterIsInstance<NoMatchingToken>().firstOrNull()
    if (tokenizationError != null) {
        val thrown = PosError(tokenizationError.tokenMismatch.toRange(), "TokenizationError")
        thrown.toString()
        throw thrown
    }
    val nextLevel = error.errors.filterIsInstance<AlternativesFailure>()
    if (nextLevel.isEmpty()) {
        val foundToken = (error.errors.last() as MismatchedToken).found
        val expectedTokens = (error.errors.map { (it as MismatchedToken).expected.toViewable() }.distinct())
        throw PosError(IntRange(foundToken.offset, foundToken.length),
            expectedTokens.joinToString(separator = " or "),
            if (Regex("\\s+").matches(foundToken.text)) "linebreak" else foundToken
        )
    }
    // if (nextLevel.size != 1)
    //     throw SystemFatalError("Hypothesis failed. Got ${nextLevel.size} alternatives, expected 1")
    findProblemToken(nextLevel.first())
}

fun Token.toViewable(): String {
    return when (this) {
        is LiteralToken -> "'${this.text}'"
        is CharToken -> "'${this.text.toString()}'"
        else -> "${this.name!!}:Token"
    }
}

fun TokenMatch.toRange(): IntRange {
    return IntRange(offset, length + offset - 1)
}