// Used as a reference: https://gist.github.com/blogscot/fd28f3aa39f10c3f0950ae9401aacca7
package external.db

import error.SystemFatalError
import utils.ExtensionUtils.addOrCreate
import java.util.*

/**
 * maybe create universal map for theorems, problems and solutions (or 3 maps for all cases)
 * then maps won't be saved for each table row, therefore compressing even more
 */
val map = mutableMapOf<Char, BitList>()

fun makeBinaryCodes(root: HuffmanNode?, binaryCode: MutableList<Boolean>, map: MutableMap<Char, BitList>) {
    if (root == null)
        return
    if ((root.left == null) && (root.right == null)) {
        map[root.char] = BitList.fromBooleanList(binaryCode)
        return
    }
    makeBinaryCodes(root.left, (binaryCode + false).toMutableList(), map)
    makeBinaryCodes(root.right, (binaryCode + true).toMutableList(), map)
}

fun compress(code: String): Map<Char, BitList> {
    if (code.isEmpty())
        throw SystemFatalError("Nothing to compress")
    val frequencies = mutableMapOf<Char, Int>()
    for (char in code)
        frequencies.addOrCreate(char, 1)
    val queue = PriorityQueue(frequencies.size, Comparator<HuffmanNode> { x, y -> x.frequency - y.frequency })
    for ((char, frequency) in frequencies) {
        queue.add(HuffmanNode(frequency, char, null, null))
    }

    var root: HuffmanNode = queue.peek()
    while (queue.size > 1) {
        val left = queue.peek()
        queue.poll()
        val right = queue.peek()
        queue.poll()
        root = HuffmanNode(left.frequency + right.frequency, '-', left, right)
        queue.add(root)
    }
    val map = mutableMapOf<Char, BitList>()
    makeBinaryCodes(root, mutableListOf(), map)
    if (map.size == 1) {
        map.values.first().add(true)
    }
    return map
}

fun encode(code: String, map: Map<Char, BitList>): Pair<ByteArray, Int> {
    val res = BitList()
    for (char in code)
        res.addBitList(map[char]!!)
    return res.toByteArray()
}

fun decode(encoded: BitList, map: Map<BitList, Char>): String {
    val res = StringBuilder()
    val current = BitList()

    for (i in 0 until encoded.size) {
        current.add(encoded[i])
        if (map[current] != null) {
            res.append(map[current])
            current.clear()
        }
    }
    return res.toString()
}
