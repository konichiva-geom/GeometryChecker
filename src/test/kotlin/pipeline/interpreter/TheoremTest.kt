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

    @Test
    fun testEqualSidedTrianglesWithCommonPoints() {
        interpret(
            """
            description:
                new W                  //   W---X
                new X; new Y; new Z    //    \ / \
                WYX == YXZ             //     Y---Z
                segment YX == segment XY
                segment YZ == segment WX
            prove:
                segment VW == segment YZ
                YWX == YZX
                YXW == XYZ
            solution: // should throw at U
                equal_sided_triangles_i(segment XY == segment XY, segment YZ == segment XW, XUW == ZXY) => *
                equal_sided_triangles_i(segment AB == segment A1B1, segment BC == segment B1C1, ABC == A1B1C1):
        """.trimIndent()
        )
    }
}