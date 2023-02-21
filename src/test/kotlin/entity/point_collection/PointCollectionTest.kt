package entity.point_collection

import TestFactory.passTask
import org.junit.Test

internal class PointCollectionTest {
    @Test
    fun testRelationMerging() {
        val symbolTable = passTask(
            """
            description:
                new A; new B; new C; new D
                A in line AB
                A in line AC
                C in line BD
                C in line CD
                B == C
            prove:
                line AB === line AC
                line BD === line CD
            solution:;
        """
        )
        assert(symbolTable.lines.size == 2)
    }

    @Test
    fun testCollectionMerging() {
        val symbolTable = passTask(
            """
            description:
                new A; new B; new C; new D
                A in line AB
                C in line CD
                A in ray AB
                C in ray CD
                A in AB
                C in CD
                B == D
                A == C
            prove:
                line AB === line AD
                line AB === line CD
                line BC === line CD

                ray AB === ray AD
                ray AB === ray CD
                ray AD === ray CD
            solution:;
        """
        )
        assert(symbolTable.lines.size == 1)
        assert(symbolTable.lines.keys.first() == LinePointCollection(mutableSetOf("A", "B")))
        assert(symbolTable.rays.size == 2)
        assert(symbolTable.rays.keys.contains(RayPointCollection("A", mutableSetOf("A", "B"))))
        assert(symbolTable.rays.keys.contains(RayPointCollection("B", mutableSetOf("A"))))
        assert(symbolTable.segments.size == 1)
        assert(symbolTable.segments.keys.first() == SegmentPointCollection(mutableSetOf("A", "B"), mutableSetOf("A")))
    }
}
