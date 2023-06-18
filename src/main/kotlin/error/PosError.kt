package error

import external.Spoof

/**
 * Error that happens at position in code
 * @property range position at which an error happened
 */
class PosError(val range: IntRange, msg: String, vararg args: Pair<String, Any>) : SpoofError(msg, *args) {
    override val message: String
        get() = super.message + " at $range"

    override fun toJson(): String {
        return """{"message": "$msg",
            "args": ${Spoof.mapToJsonString(args.toList().associate { it.first to it.second.toString() })},
            "range":[${range.first}, ${range.last}]}"""
    }
}
