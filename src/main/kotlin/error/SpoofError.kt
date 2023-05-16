package error

import external.Spoof.changeAllIndicesInOrder
import external.Spoof.mapToJsonString

/**
 * Error in which args will be changed to support multiple languages
 * @param msg message of the error (different for all languages)
 * @param args arguments of the error (same for all languages)
 */
open class SpoofError(val msg: String, vararg val args: Pair<String, Any>) : Exception() {
    override val message: String
        get() = changeAllIndicesInOrder(msg, args.toList())

    fun toJson(): MutableMap<String, String> {
        return mutableMapOf(
            "message" to msg,
            "args" to mapToJsonString(args.toList().associate { it.first to it.second.toString() })
        )
    }
}
