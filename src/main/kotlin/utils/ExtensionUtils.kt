package utils

import com.github.h0tk3y.betterParse.lexer.CharToken
import com.github.h0tk3y.betterParse.lexer.LiteralToken
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import math.Fraction
import math.FractionFactory

object ExtensionUtils {
    fun <R, T> MutableMap<R, MutableSet<T>>.addToOrCreateSet(key: R, vararg elements: T) {
        if (this[key] == null)
            this[key] = mutableSetOf(*elements)
        else
            this[key]!!.addAll(elements.toList())
    }

    fun <R> MutableMap<R, Fraction>.addOrCreate(
        key: R,
        element: Fraction = FractionFactory.one()
    ): MutableMap<R, Fraction> {
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
            is LiteralToken -> "'${this.text}'"
            is CharToken -> "'${this.text}'"
            else -> "${this.name!!}:Token"
        }
    }

    fun TokenMatch.toRange(): IntRange {
        return IntRange(offset, length + offset - 1)
    }
}
