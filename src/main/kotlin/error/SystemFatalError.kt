package error

/**
 * This error means that there is some fatal flaw in the design.
 * When it happens in the production code, it sends a message to dev mail.
 */
class SystemFatalError(private val msg: String) : Exception() {
    override val message: String
        get() = "Something really bad happened X(. Sending message to devs.\n$msg"
}
