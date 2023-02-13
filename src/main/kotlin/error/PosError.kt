package error

/**
 * Error that happens at position in code
 * @property range position at which an error happened
 */
class PosError(private val range: IntRange, msg: String, vararg args: Pair<String, Any>) : SpoofError(msg, *args) {
    override val message: String
        get() = super.message + " at $range"
}
