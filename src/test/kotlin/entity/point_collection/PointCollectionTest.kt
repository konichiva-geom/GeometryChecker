package entity.point_collection

import TestFactory.failTask
import TestFactory.passTask
import kotlin.test.Test

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

    @Test
    fun testAngleCollectionAdding() {
        passTask("""
            description:
                new A; new B; new C; new T
                T in BC
            prove:
                 ABC == ABT
                 ACT == ACB
                 ray BT == ray BC
                 ray CT == ray CB
            solution:;
        """)

        failTask("""
            description:
                new A; new B; new C; new T
                T in ray BC
            prove:
                 ABC == ABT
                 ray BT == ray BC
                 ACT == ACB
                 ray CT == ray CB
            solution:;
        """, "Relation ACT == ACB unknown")

        failTask("""
            description:
                new A; new B; new C; new T
                T in line BC
            prove:
                 ABC == ABT
                 ray BT == ray BC
                 ACT == ACB
                 ray CT == ray CB
            solution:;
        """, "Relation ABC == ABT unknown")
    }
}
