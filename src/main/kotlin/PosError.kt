const val REPLACED = "%{}"

/**
 * msg %{} is changed to args
 */
class PosError(var msg: String, vararg val args: Any) : Exception() {
    private lateinit var position: Pair<Int, Int>
    override val message: String
        get() = msg

    fun setPosition(pair: Pair<Int, Int>) {
        position = pair
    }

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
        for (i in 0..args.lastIndex)
            res.replace(ranges[i].first, ranges[i].last, args[i].toString())
        return res.toString()
    }
}