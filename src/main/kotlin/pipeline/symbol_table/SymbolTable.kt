package pipeline.symbol_table

import utils.MutablePair
import java.util.*

open class SymbolTable : VectorSymbolTable() {
    fun clear() {
        points.clear()
        lines.clear()
        rays.clear()
        segments.clear()
        angles.clear()
        circles.clear()
        arcs.clear()

        arcToAngleList.clear()
        angleVectors.vectors.clear()
        segmentVectors.vectors.clear()

        equalIdentRenamer.clear()
    }

    fun assertCorrectState() {
        assert(angleVectors.vectors.size == angleVectors.vectors.keys.toSet().size)
        assert(segmentVectors.vectors.size == segmentVectors.vectors.keys.toSet().size)

        assertMapCorrect(points)
        assertMapCorrect(circles)
        assertMapCorrect(segments)
        assertMapCorrect(arcs)

        assertLinkedListCorrect(angles)
        assertLinkedListCorrect(rays)
        assertLinkedListCorrect(lines)
        assertLinkedListCorrect(arcToAngleList)
    }

    fun <A, B> assertLinkedListCorrect(list: LinkedList<MutablePair<A, B>>) {
        assert(list.size == list.map { it.e1 }.toSet().size)
    }

    private fun <A, B> assertMapCorrect(map: Map<A, B>) {
        assert(map.size == map.keys.toSet().size)
    }
}
