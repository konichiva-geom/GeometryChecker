package error

import external.Spoof.changeAllIndicesInOrder

/**
 * Error in which args will be changed to support multiple languages
 * @param msg message of the error (different for all languages)
 * @param args arguments of the error (same for all languages)
 */
open class SpoofError(var msg: String, vararg val args: Pair<String, Any>) : Exception() {
    override val message: String
        get() = changeAllIndicesInOrder(msg, args.toList())
}
