package external.db

import utils.MathUtils.max
import java.util.*

class BitList(val set: BitSet = BitSet(), var size: Int = 0) : Comparable<BitList> {

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

    override fun toString(): String {
        val res = StringBuilder()
        for (i in 0 until size)
            res.append(if (set[i]) '1' else '0')
        return res.toString()
    }

    // TODO: might optimize
    override fun compareTo(other: BitList): Int {
        return if (size != other.size) size.compareTo(other.size)
        else toString().compareTo(other.toString())
    }

    fun toByteArray(): Pair<ByteArray, Int> {
        return set.toByteArray() to size
    }

    companion object {
        fun fromBooleanList(list: MutableList<Boolean>): BitList {
            val res = BitList()
            for ((i, v) in list.withIndex()) {
                res[i] = v
            }
            return res
        }

        fun fromString(str: String): BitList {
            val res = BitList()
            for ((i, v) in str.withIndex()) {
                res[i] = v == '1'
            }
            return res
        }

        fun fromByteArray(byteArray: ByteArray, size: Int) = BitList(BitSet.valueOf(byteArray), size)
    }
}
