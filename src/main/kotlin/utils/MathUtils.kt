package utils

import kotlin.jvm.internal.CallableReference
import kotlin.math.abs

object MathUtils {
    inline fun <reified T> maxToMin(f: Any, vararg elements: T): Array<T> {
        f as CallableReference
        return elements
            .toList()
            .map { f.call(it) as Comparable<Any> to it }
            .sortedBy { it.first }
            .map { it.second }
            .toTypedArray()
    }

    fun <T> maxToMin(f: Any, first: T, second: T): Pair<T, T> {
        f as CallableReference
        return if (f.call(first) as Comparable<Any> > f.call(second) as Comparable<Any>)
            first to second else second to first
    }

    fun <T : Comparable<T>?> max(t1: T, t2: T): T {
        return if (t1!! > t2) t1 else t2
    }

    fun <T : Comparable<T>?> min(t1: T, t2: T): T {
        return if (t1!! < t2) t1 else t2
    }

    fun getLCM(first: Int, second: Int): Int = first / getGCD(first, second) * second

    fun getGCD(first: Int, second: Int): Int {
        if (first == 0 && second == 0)
            return 1
        return calcGCD(max(abs(first), abs(second)), min(abs(first), abs(second)))
    }

    private fun calcGCD(first: Int, second: Int): Int {
        if (second == 0)
            return first
        return calcGCD(second, first % second)
    }
}
