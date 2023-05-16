package utils

import error.SystemFatalError
import kotlin.jvm.internal.CallableReference
import kotlin.math.abs
import kotlin.math.sqrt

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

    fun getLCM(first: Int, second: Int): Int = first / getGCD(first, second) * second

    fun getGCD(first: Int, second: Int): Int {
        if (first == 0 && second == 0)
            return 1
        return calcGCD(maxOf(abs(first), abs(second)), minOf(abs(first), abs(second)))
    }

    private fun calcGCD(first: Int, second: Int): Int {
        if (second == 0)
            return first
        return calcGCD(second, first % second)
    }

    /**
     * solve equation of type a_0 + a_1x + ... + a_{n-1}x^{n-1} = 0
     * args is an array [a_{n-1},..., a_1, a_0]
     */
    fun solve(argsWithZeroes: MutableList<Double>): List<Double> {
        var args = removeFirstZeros(argsWithZeroes)
        if (args.size <= 1 || args.size >= 6) {
            throw SystemFatalError("Expected from 2 to 5 elements in input array, not counting preceding zeros")
        }
        // a = 0
        if (args.size == 1)
            return if (args[0] == 0.0) mutableListOf(1.0, 2.0, 3.0, 4.0, 5.0) else mutableListOf()
        args = firstToOne(args)
        // x + a = 0
        if (args.size == 2)
            return mutableListOf(args[1].unaryMinus())
        if (args.size == 3)
            return solveSquare(args[1], args[2]).sorted()
        if (args.size == 4)
            return solveCubic(args[1], args[2], args[3]).sorted()
        if (args.size == 5)
            return solveQuadratic(args[1], args[2], args[3], args[4]).sorted()
        throw SystemFatalError("Expected from 2 to 5 elements")
    }

    // remove all zeros from start of the array
    private fun removeFirstZeros(array: MutableList<Double>): MutableList<Double> {
        if (array.size > 0 && array[0] == 0.0) {
            array.removeAt(0)
            return removeFirstZeros(array)
        }
        return array
    }

    private fun firstToOne(array: MutableList<Double>): MutableList<Double> {
        var i = 0
        val coeff = array[0]
        while (i < array.size) {
            array[i] = array[i] / coeff
            i += 1
        }
        return array
    }

    // x^4 + ax^3 + bx^2 + cx + d = 0
    private fun solveQuadratic(a: Double, b: Double, c: Double, d: Double): List<Double> {
        if (a == 0.0)
            return solveQuadraticZeroCube(b, c, d)
        if (d == 0.0)
            return (solveCubic(a, b, c) + 0.0).sorted()
        val e = a / 4
        val h = e * e
        val p = h * -6 + b
        val q = h * e * 8 - (b * e * 2 + c)
        val r = h * h * -3 + (b * h - (c * e) + d)
        val offset = solveQuadraticZeroCube(p, q, r)
        var i = 0
        while (i < offset.size) {
            offset[i] = offset[i] - e
            i++
        }
        return offset
    }

    private fun m1(p: Double, r: Double): MutableList<Double> {
        var n = solveSquare(p, r).sorted().toMutableList()
        if (n.isEmpty()) return mutableListOf()
        if (n[0] >= 0) {
            n[0] = sqrt(n[0])
            n[1] = -n[0]
        } else
            n = mutableListOf<Double>()
        if (n[1] >= 0) {
            n.add(sqrt(n[1]))
            n.add(-n[n.size - 1])
        }
        return n
    }

    // x^4 + ax^2 + bx + c = 0
    private fun solveQuadraticZeroCube(aVal: Double, b: Double, c: Double): MutableList<Double> {
        var a = aVal
        if (c == 0.0) // x^3 + 0x^2 + ax + b
            return (solveCubic(0.0, a, b) + 0.0).toMutableList()
        if (b == 0.0)
            return m1(a, c)
        val n = solveCubic(a * 2, a * a - (c * 4), -b * b)
        var p = n[0]
        if (n.size == 3) {
            if (p < n[1]) p = n[1]
            if (p < n[2]) p = n[2]
        }
        if (p <= 0.0)
            return m1(a, c)
        a += p
        p = sqrt(p)
        val ba = b / p
        var sol = solveSquare(p, 0.5 * (a - ba))
        sol = (sol + solveSquare(-p, 0.5 * (a + ba))).toMutableList()
        return sol
    }

    // x^3 + ax + b = 0
    fun newton(aVal: Double, bVal: Double): Double {
        var a = aVal
        var b = bVal
        var s = 1.0
        while (b + a > -1) {
            a *= 4
            b *= 8
            s *= 0.5
        }
        while (a * 2 + b < -8) {
            a *= 0.25
            b *= 0.125
            s *= 2
        }
        var x = 1.5
        var i = 0
        while (i < 9) {
            x -= (x * (x * x + a) + b) / (3.0 * x * x + a)
            i++
        }
        return x * s
    }

    // x^3 + ax^2 + bx + c = 0
    private fun solveCubic(aVal: Double, bVal: Double, c: Double): MutableList<Double> {
        var a = aVal
        var b = bVal
        var solution: Double
        if (c == 0.0) {
            solution = 0.0
        } else {
            val a3 = a / 3
            val p = b - (a3 * a)
            val q = c - ((a3 * a3 + p) * a3)
            solution = if (q < 0.0) {
                newton(p, q)
            } else if (q > 0.0) {
                newton(p, q.unaryMinus()).unaryMinus()
            } else {
                0.0
            }
            solution -= a3
            val t = solution * (solution * 3 + (a * 2)) + b
            if (abs(t) > 0.001) {
                solution -= (solution * (solution * (solution + a) + b) + c) / t
            }
            a += solution
            b += solution * a
        }
        return (solveSquare(a, b) + solution).sorted().toMutableList()
    }

    // x^2 + ax + b = 0
    private fun solveSquare(a: Double, b: Double): MutableList<Double> {
        if (b == 0.0)
            return if (a == 0.0) mutableListOf(0.0, 0.0)
            else mutableListOf(0.0, -a).sorted().toMutableList()
        val solution = mutableListOf<Double>()
        var a = a * -0.5
        var d = a * a - b
        if (d < 0)
            return mutableListOf()
        d = sqrt(d)
        if (d == 0.0)
            return mutableListOf(a, a)
        return mutableListOf(a + d, a - d).sorted().toMutableList()
    }

    /**
     * Return array of doubles as a solution of ax^2 + bx + c = 0
     */
    fun solveSquare(a: Double, bVal: Double, c: Double): MutableList<Double> {
        var b = bVal
        val solution = mutableListOf(0.0, 0.0)
        if (a == 0.0) {
            if (c == 0.0) {
                if (b == 0.0) {
                    return mutableListOf(1.0, 2.0, 3.0)
                }
                return mutableListOf(0.0)
            } else {
                if (b == 0.0) {
                    return mutableListOf()
                }
                return mutableListOf(0.0)
            }
        } else {
            if (c == 0.0) {
                return mutableListOf(0.0, -b / a)
            } else {
                b *= -0.5
                var d = b * b - (a * c)
                if (d < 0) {
                    return mutableListOf(0.0)
                }
                d = sqrt(d)
                val t = if (b > 0) b + d else b - d
                return mutableListOf(c / t, t / a)
            }
        }
    }
}
