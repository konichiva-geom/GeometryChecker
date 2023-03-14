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

//    /**
//     * solve equation of type a_0 + a_1x + ... + a_{n-1}x^{n-1} = 0
//     * args is an array [a_{n-1},..., a_1, a_0]
//     */
//    fun solve(argsWithZeroes: MutableList<Fraction>): MutableList<Fraction> {
//        var args = removeFirstZeros(argsWithZeroes)
//        if (args.size <= 1 || args.size >= 6) {
//            throw SystemFatalError("Expected from 2 to 5 elements in input array, not counting preceding zeros")
//        }
//        // a = 0
//        if (args.size == 1)
//            return [if (args[0].isZero()) [1, 2, 3, 4, 5] else []]
//        args = firstToOne(args)
//        // x + a = 0
//        if (args.size == 2)
//            return mutableListOf(args[1].unaryMinus())
//        if (args.size == 3)
//            return solveSquare(args[1], args[2]).sorted()
//        if (args.size == 4)
//            return solveCubic(args[1], args[2], args[3]).sorted()
//        if (args.size == 5)
//            return solveQuadratic(args[1], args[2], args[3], args[4]).sorted()
//    }
//
//    // remove all zeros from start of the array
//    private fun removeFirstZeros(array: MutableList<Fraction>): MutableList<Fraction> {
//        if (array.size > 0 && array[0].isZero()) {
//            array.removeAt(0)
//            return removeFirstZeros(array)
//        }
//        return array
//    }
//
//    private fun firstToOne(array: MutableList<Fraction>): MutableList<Fraction> {
//        var i = 0
//        val coeff = array[0]
//        while (i < array.size) {
//            array[i] = array[i].divide(coeff)
//            i += 1
//        }
//        return array
//    }
//
//    // x^4 + ax^3 + bx^2 + cx + d = 0
//    private fun solveQuadratic(a: Fraction, b: Fraction, c: Fraction, d: Fraction): MutableList<Fraction> {
//        if (a.isZero())
//            return solveQuadraticZeroCube(b, c, d)
//        if (d.isZero())
//            return (solveCubic(a, b, c) + 0.0).sorted()
//        val e = a.divide(4)
//        val h = e.multiply(e)
//        val p = h.multiply(-6).plus(b)
//        val q = h.multiply(e).multiply(8).subtract(b.multiply(e).multiply(2).add(c))
//        val r = h.multiply(h).multiply(-3).add(b.multiply(h).subtract(c.multiply(e)).add(d))
//        val offset = solveQuadraticZeroCube(p, q, r)
//        var i = 0
//        while (i < offset.size) {
//            offset[i] = offset[i] - e
//            i++
//        }
//        return offset
//    }
//
//    private fun m1(p: Fraction, r: Fraction): MutableList<Fraction> {
//        var n = solveSquare(p, r).sorted()
//        if (n.size == 0) return mutableListOf()
//        if (n[0] >= 0) {
//            n[0] = sqrt(n[0])
//            n[1] = -n[0]
//        } else
//            n = mutableListOf<Fraction>()
//        if (n[1] >= 0) {
//            n.add(sqrt(n[1]))
//            n.add(-n[n.size - 1])
//        }
//        return n
//    }
//
//    // x^4 + ax^2 + bx + c = 0
//    private fun solveQuadraticZeroCube(aVal: Fraction, b: Fraction, c: Fraction): MutableList<Fraction> {
//        var a = aVal
//        if (c.isZero()) // x^3 + 0x^2 + ax + b
//            return (solveCubic(FractionFactory.zero(), a, b) + FractionFactory.zero()).toMutableList()
//        if (b.isZero())
//            return m1(a, c)
//        val n = solveCubic(a.multiply(2), a.multiply(a).subtract(c.multiply(4)), b.multiply(b).unaryMinus())
//        var p = n[0]
//        if (n.size == 3) {
//            if (p < n[1]) p = n[1]
//            if (p < n[2]) p = n[2]
//        }
//        if (!p.moreThanZero())
//            return m1(a, c)
//        a = a.add(p)
//        p = sqrt(p)
//        val ba = b.divide(p)
//        var sol = solveSquare(p, FractionFactory.create(1, 2).multiply(a.subtract(ba)))
//        sol = (sol + solveSquare(p.unaryMinus(), FractionFactory.create(1, 2).multiply(a.add(ba)))).toMutableList()
//        return sol
//    }
//
//    // x^3 + ax + b = 0
//    fun newton(aVal: Fraction, bVal: Fraction): Fraction {
//        var a = aVal
//        var b = bVal
//        var s = FractionFactory.one()
//        while (b.add(a) > -1) {
//            a = a.multiply(4)
//            b = b.multiply(8)
//            s = s.multiply(FractionFactory.create(1, 2))
//        }
//        while (a.multiply(2).add(b) < -8) {
//            a = a.multiply(FractionFactory.create(1, 4))
//            b = b.multiply(FractionFactory.create(1, 8))
//            s = s.multiply(2)
//        }
//        var x = 1.5
//        var i = 0
//        while (i < 9) {
//            x = x - (x * (x * x + a) + b) / (3.0 * x * x + a)
//            i++
//        }
//        return x * s
//    }
//
//    // x^3 + ax^2 + bx + c = 0
//    private fun solveCubic(aVal: Fraction, bVal: Fraction, c: Fraction): MutableList<Fraction> {
//        var a = aVal
//        var b= bVal
//        var solution: Fraction
//        if (c.isZero()) {
//            solution = FractionFactory.zero()
//        } else {
//            val a3 = a.divide(FractionFactory.fromInt(3))
//            val p = b.subtract(a3.multiply(a))
//            val q = c.subtract((a3.multiply(a3).add(p)).multiply(a3))
//            solution = if (q.lessThanZero()) {
//                newton(p, q)
//            } else if (q.moreThanZero()) {
//                newton(p, q.unaryMinus()).unaryMinus()
//            } else {
//                FractionFactory.zero()
//            }
//            solution = solution.subtract(a3)
//            val t = solution.multiply(solution.multiply(FractionFactory.fromInt(3)).add(a.multiply(2))).add(b)
//            if (t.abs() > 0.001) {
//                solution = solution - (solution * (solution * (solution + a) + b) + c) / t
//            }
//            a = a.add(solution)
//            b = solution.multiply(a).add(b)
//        }
//        return (solveSquare(a, b) + solution).sorted()
//    }
//
//    // x^2 + ax + b = 0
//    private fun solveSquare(a: Fraction, b: Fraction): MutableList<Fraction> {
//        if (b.isZero())
//            return if (a.isZero()) mutableListOf(FractionFactory.zero(), FractionFactory.zero())
//            else mutableListOf(FractionFactory.zero(), a.unaryMinus()).sorted()
//        val solution = mutableListOf<Fraction>()
//        var a = a.multiply(FractionFactory.create(-1, 2))
//        var d = a * a - b
//        if (d < 0)
//            return mutableListOf()
//        d = sqrt(d)
//        if (d.isZero())
//            return mutableListOf(a, a)
//        return mutableListOf(a.add(d), a.subtract(d)).sorted()
//    }
//
//    /**
//     * Return array of doubles as a solution of ax^2 + bx + c = 0
//     */
//    fun solveSquare(a: Fraction, bVal: Fraction, c: Fraction): MutableList<Fraction> {
//        var b = bVal
//        val solution = mutableListOf(FractionFactory.zero(), FractionFactory.zero())
//        if (a.isZero()) {
//            if (c.isZero()) {
//                if (b.isZero()) {
//                    return mutableListOf(FractionFactory.one(), FractionFactory.fromInt(2), FractionFactory.fromInt(3))
//                }
//                return mutableListOf(FractionFactory.zero())
//            } else {
//                if (b.isZero()) {
//                    return mutableListOf()
//                }
//                return mutableListOf(FractionFactory.zero())
//            }
//        } else {
//            if (c.isZero()) {
//                return mutableListOf(FractionFactory.zero(), b.unaryMinus().divide(a))
//            } else {
//                b = b.multiply(FractionFactory.create(-1, 2))
//                var d = b.multiply(b).subtract(a.multiply(c))
//                if (d < 0) {
//                    return FractionFactory.zero()
//                }
//                d = sqrt(d)
//                val t = if (b > 0) b.add(d) else b.subtract(d)
//                return mutableListOf(c.divide(t), t.divide(a))
//            }
//        }
//    }
}
