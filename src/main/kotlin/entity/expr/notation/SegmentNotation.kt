package entity.expr.notation

import pipeline.interpreter.IdentMapperInterface

class SegmentNotation(p1: String, p2: String) : Point2Notation(p1, p2) {
    override fun getOrder(): Int = 2

    override fun toLine() = Point2Notation(p1, p2)
    override fun getRepr() = StringBuilder("AA")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) =
        SegmentNotation(mapper.get(p1), mapper.get(p2))

    override fun toString(): String = "$p1$p2"
}
