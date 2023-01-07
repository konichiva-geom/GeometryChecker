import com.github.h0tk3y.betterParse.lexer.CharToken
import com.github.h0tk3y.betterParse.lexer.LiteralToken
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
val normalFailures = mutableSetOf<Token>(LiteralToken("thDefStart", "th"))

/**
 * Error in which args will be changed to support multiple languages
 * @param msg message of the error (different for all languages)
 * @param args arguments of the error (same for all languages)
 */
open class SpoofError(var msg: String, vararg val args: Pair<String, Any>) : Exception() {
    override val message: String
        get() = changeAllIndicesInOrder(msg)

    private fun changeAllIndicesInOrder(text: String): String {
        val regex = Regex("%\\{\\w+}")
        val sb = StringBuilder(text)
        val mapOfArgs = args.toMap()
        regex.findAll(text).sortedBy { -it.range.first }
            .map { Triple(it.range.first, it.range.last, it.value.substring(2, it.value.length - 1)) }
            .forEach { sb.replace(it.first, it.second + 1, (mapOfArgs[it.third] ?: "<NOT_FOUND>").toString()) }
        return sb.toString()
    }
}

/**
 * Error that happens at position in code
 * @property range position at which an error happened
 */
class PosError(private val range: IntRange, msg: String, vararg args: Pair<String, Any>) : SpoofError(msg, *args) {
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