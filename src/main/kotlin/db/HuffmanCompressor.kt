package db

import Utils.addOrCreate
import Utils.max
import java.util.*

fun makeBinaryCodes(root: HuffmanNode?, binaryCode: MutableList<Boolean>, map: MutableMap<Char, BitList>) {
    if (root == null)
        return
    if ((root.left == null) && (root.right == null)) {
        map[root.char] = binaryCode.toBitList()
        return
    }
    makeBinaryCodes(root.left, (binaryCode + false).toMutableList(), map)
    makeBinaryCodes(root.right, (binaryCode + true).toMutableList(), map)
}

class BitList {
    val set = BitSet()
    var size = 0

    operator fun get(index: Int): Boolean = set[index]

    operator fun set(index: Int, value: Boolean) {
        size = max(size, index + 1)
        set[index] = value
    }

    fun add(value: Boolean) {
        set[size++] = value
    }

    fun clear() {
        set.clear()
        size = 0
    }

    fun addBitList(other: BitList) {
        for (i in 0 until other.size) {
            this[this.size] = other[i]
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BitList)
            return false
        return size == other.size && set == other.set
    }

    override fun hashCode(): Int {
        return set.hashCode() + (size * 31).hashCode()
    }
}

fun MutableList<Boolean>.toBitList(): BitList {
    val res = BitList()
    for ((i, v) in this.withIndex()) {
        res[i] = v
    }
    return res
}

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

class MyComparator : Comparator<HuffmanNode> {
    override fun compare(x: HuffmanNode, y: HuffmanNode): Int {
        return x.frequency - y.frequency
    }
}

fun compress(code: String): Map<Char, BitList> {
    val frequencies = mutableMapOf<Char, Int>()
    for (c in code)
        frequencies.addOrCreate(c, 1)
    val q = PriorityQueue(frequencies.size, MyComparator())
    for ((char, frequency) in frequencies) {
        q.add(HuffmanNode(frequency, char, null, null))
    }
    var root: HuffmanNode? = null
    while (q.size > 1) {
        val x = q.peek()
        q.poll()
        val y = q.peek()
        q.poll()
        val f = HuffmanNode(x.frequency + y.frequency, '-', x, y)
        root = f
        q.add(f)
    }
    val map = mutableMapOf<Char, BitList>()
    makeBinaryCodes(root, mutableListOf(), map)

    return map
}

fun encode(code: String, map: Map<Char, BitList>): BitList {
    val res = BitList()
    for (char in code) {
        res.addBitList(map[char]!!)
    }
    return res
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

fun main() {
    val map = compress(TEXT)

    val bitList = encode(TEXT, map)
    println(bitList)
    val decoded = decode(bitList, map.entries.associateBy({ it.value }) { it.key })
    println(decoded)

    println(TEXT.toByteArray().size)
    println(bitList.set.toByteArray().size)
}

const val TEXT = """th equal_sided_triangles_i(AB == A1B1, BC == B1C1, ABC == A1B1C1):
    check(A != B)
    check(A != C)
    check(B != C)
    check(A1 != B1)
    check(A1 != C1)
    check(B1 != C1)
    return AC == A1C1, ACB == A1C1B1, BAC == C1A1B1
    // triangle ABC == triangle A1B1C1 // are triangles needed???

th straight_angle(O, AB): // развернутый угол
    check(O in AB)
    return AOB == 180

th bisector(new D, ABC): // makes a new point distinct from all currently placed
    return ABD == CBD, ABD == ABC / 2, D in AC

th adjacent_angle(AOB, BOC): // new in args cannot be - bad
    check(O in AB)
    return AOB + BOC == 180, AOC == 180, AOB + BOC == AOC

th bisector(new D, ABC): // makes a new point distinct from all currently placed
    return ABD == CBD, ABD == ABC / 2

th projection(new H, A, BC): // projection H of A on BC
    H in BC
    return HA ⊥ BC, BHA == 90, CHA == 90

th mid_point(new M, AB):
    return M in AB

th merge_projections(AH, AH1, BC):
    check(BHA == 90)
    check(BH1A == 90)
    return H == H1

// rwefiofjfwoifejfoiw */
th merge_points_in_line(AP == AP1, BP == BP1):
    check(P in AB)
    check(P1 in AB)
    return P == P1


//Старое:

//    Line relations
//
//    AB || l and AC || l => AB == AC
//
//    AB || l and CD || l => AB || CD // this is not true
//    we can eventually find out that AB == CD, out second theorem is incorrect)

//    AB || l and CD || l => AB | CD // maybe like a symbol for collinearity?

//    Triangles

//    TriangleEquality1(AB == A1B1, AC == A1C1, BAC == B1A1C1) => * ABC == A1B1C1, ACB == A1C1B1, BC == B1C1

//    Trapezoid
"""