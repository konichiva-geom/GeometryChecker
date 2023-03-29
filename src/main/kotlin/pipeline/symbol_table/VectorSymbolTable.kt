package pipeline.symbol_table

import entity.expr.notation.*
import entity.point_collection.AnglePointCollection
import entity.point_collection.ArcPointCollection
import entity.point_collection.SegmentPointCollection
import error.SpoofError
import math.*
import utils.multiSetOf

open class VectorSymbolTable : PointCollectionSymbolTable() {
    val segmentVectors = VectorContainer<SegmentPointCollection>()
    val angleVectors = VectorContainer<AnglePointCollection>()

    fun getOrCreateVector(notation: Notation): Vector {
        when (notation) {
            is NumNotation -> return mutableMapOf(multiSetOf(0) to if (notation.number == 0.0) 1.0 else notation.number)
            is ArcNotation -> {
                val angle = arcToAngleList.find {
                    it.e1 == ArcPointCollection(
                        notation.getPointsAndCircles().toMutableSet(), circle = notation.circle
                    )
                } ?: throw SpoofError("Angle for arc %{arc} not specified", "arc" to notation)
                return angleVectors.getOrCreate(angle.e2)
            }
            is Point3Notation -> return angleVectors.getOrCreate(getKeyByNotation(notation) as AnglePointCollection)
            is SegmentNotation -> {
                val key = SegmentPointCollection(notation.getPointsAndCircles().toMutableSet())
                if (segmentVectors.vectors[key] == null)
                    equalIdentRenamer.addSubscribers(key, *key.getPointsInCollection().toTypedArray())
                return segmentVectors.getOrCreate(key)
            }
            is MulNotation -> {
                return notation.elements.map {
                    getOrCreateVector(it)
                }.fold(mutableMapOf(multiSetOf<Int>() to 1.0)) { acc, i ->
                    acc.mergeWith(i, "*")
                }
            }
            else -> throw SpoofError(
                "Unexpected notation %{notation} in arithmetic expression",
                "notation" to notation
            )
        }
    }
}