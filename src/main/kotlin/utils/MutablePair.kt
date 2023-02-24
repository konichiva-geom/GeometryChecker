package utils

data class MutablePair<A, B>(var e1: A, var e2: B) {
    override fun toString(): String {
        return "($e1, $e2)"
    }
}

infix fun <A, B> A.with(that: B): MutablePair<A, B> = MutablePair(this, that)