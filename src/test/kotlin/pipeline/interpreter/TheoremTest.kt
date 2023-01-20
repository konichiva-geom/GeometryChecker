package pipeline.interpreter

import TestFactory.interpret
import kotlin.test.Test

internal class TheoremTest {
    @Test
    fun testEqualSidedTriangles() {
        interpret(
            """
            description:
                new U; new V; new W    //    U       X
                new X; new Y; new Z    //   / \     / \
                VUW == YXZ             //  V---W   Y---Z
                segment VU == segment XY
                segment UW == segment ZX
            prove:
                segment VW == segment YZ
                UVW == XYZ
                UWV == XZY
            solution:
                equal_sided_triangles_i(segment VU == segment XY, segment UW == segment XZ, VUW == ZXY) => *
        """.trimIndent()
        )
    }

    /**
     * Reason why expression mapper has links now
     */
    @Test
    fun testEqualSidedTrianglesWithCommonPoints() {
        interpret(
            """
            description:
                new W                  //   W---X
                new X; new Y; new Z    //    \ / \
                ZYX == YXW             //     Y---Z
                //segment YX == segment XY
                segment YZ == segment WX
            prove:
                segment WY == segment XZ
                YWX == YZX
                YXW == XYZ
            solution:
                equal_sided_triangles_i(segment XY == segment XY, segment YZ == segment XW, ZYX == WXY) => *
        """.trimIndent()
        )
    }

    /**
     * Reason why expression mapper has links now
     */
    @Test
    fun testEqualSidedTrianglesWithEqualSides() {
        interpret(
            """
            description:
                new W                  //   W---X
                new X; new Y; new Z    //    \ / \
                ZYX == YXW             //     Y---Z
                //segment YX == segment XY
                segment YZ == segment WX
            prove:
                segment WY == segment XZ
                YWX == YZX
                YXW == XYZ
            solution:
                equal_sided_triangles_i(segment XY == segment XY, segment YZ == segment XW, ZYX == WXY) => *
        """.trimIndent()
        )
    }
}
