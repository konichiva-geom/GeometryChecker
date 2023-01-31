package external

object Logger {
    private val warnings = mutableListOf<Pair<String, List<Pair<String, Any>>>>()

    fun warn(message: String, vararg args: Pair<String, Any>) {
        warnings.add(message to args.toList())
    }

    fun getWarnings(): List<String> {
        return warnings.map { Spoof.changeAllIndicesInOrder(it.first, it.second) }
    }
}
