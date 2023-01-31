package external.db

class HuffmanNode(val frequency: Int, val char: Char, val left: HuffmanNode? = null, val right: HuffmanNode? = null) {
    override fun toString(): String {
        return "$frequency, $char"
    }

    fun getChildNumAndFreq(): Pair<Int, Int> {
        val leftRes = left?.getChildNumAndFreq() ?: (0 to 0)
        val rightRes = right?.getChildNumAndFreq() ?: (0 to 0)
        return (1 + leftRes.first + rightRes.first) to (frequency + leftRes.second + rightRes.second)
    }
}