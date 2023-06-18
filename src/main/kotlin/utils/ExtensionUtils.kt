package utils

import com.github.h0tk3y.betterParse.lexer.CharToken
import com.github.h0tk3y.betterParse.lexer.LiteralToken
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import utils.CommonUtils.CONSIDERED_DIGITS_AFTER_POINT
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

object ExtensionUtils {
    /**
     * Represent double as string
     */
    fun Double.asString(): String {
        return if (ceil(this) == this) {
            val value = this.toInt()
            value.toString()
        } else this.toString()
    }

    /**
     *
     */
    fun Double.isAlmostZero(): Boolean {
        return floor(abs(this * 10.0.pow(CONSIDERED_DIGITS_AFTER_POINT))) <= 0
    }

    fun <R, T> MutableMap<R, MutableSet<T>>.addToOrCreateSet(key: R, vararg elements: T) {
        if (this[key] == null)
            this[key] = mutableSetOf(*elements)
        else
            this[key]!!.addAll(elements.toList())
    }

    fun <R> MutableMap<R, Double>.addOrCreate(
        key: R,
        element: Double = 1.0
    ): MutableMap<R, Double> {
        if (this[key] == null)
            this[key] = element
        else
            this[key] = this[key]!! + element
        return this
    }

    fun <R> MutableMap<R, Int>.addOrCreate(key: R, element: Int) {
        if (this[key] == null)
            this[key] = element
        else
            this[key] = this[key]!! + element
    }

    fun Token.toViewable(): String {
        return when (this) {
            is LiteralToken -> "`${this.text}`"
            is CharToken -> "${this.text}"
            else -> "${this.name!!}:Token"
        }
    }

    fun TokenMatch.toRange(): IntRange {
        return IntRange(offset, length + offset - 1)
    }
}
