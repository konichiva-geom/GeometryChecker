package utils

import kotlin.math.abs

object MathUtils {
    fun <T : Comparable<T>?> max(t1: T, t2: T): T {
        return if (t1!! > t2) t1 else t2
    }

    fun <T : Comparable<T>?> min(t1: T, t2: T): T {
        return if (t1!! < t2) t1 else t2
    }

    fun LCM(first: Int, second: Int): Int = first / GCD(first, second) * second

    fun GCD(first: Int, second: Int): Int {
        if (first == 0 && second == 0)
            return 1
        return calcGCD(max(abs(first), abs(second)), min(abs(first), abs(second)))
    }

    private tailrec fun calcGCD(first: Int, second: Int): Int {
        if (second == 0)
            return first
        return calcGCD(second, second % first)
    }
}
