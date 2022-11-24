import notation.Point2Notation
import notation.Point3Notation

object Utils {
    fun sortLine(notation: Point2Notation): Point2Notation {
        if (notation.p1 == notation.p2) throw Exception("Line consists of same points")
        if (notation.p1 > notation.p2)
            notation.p1 = notation.p2.also { notation.p2 = notation.p1 }
        return notation
    }

    fun sortAngle(notation: Point3Notation): Point3Notation {
        if (notation.p1 > notation.p3)
            notation.p1 = notation.p3.also { notation.p3 = notation.p1 }
        return notation
    }

    fun <T> MutableMap<T, Float>.mergeWithOperation(
        other: MutableMap<T, Float>,
        operation: (Float, Float) -> Float
    ): MutableMap<T, Float> {
        return (keys + other.keys)
            .associateWith { operation(this[it] ?: 0f, other[it] ?: 0f) }
            .toMutableMap()
    }
}