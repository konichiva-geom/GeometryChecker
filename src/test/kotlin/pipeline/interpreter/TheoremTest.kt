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
                YX == XY
                YZ == WX
            prove:
                VW == YZ
                UVW == XYZ
                UWV == XZY
            solution:
                equal_sided_triangles_i(VU == XY, UW == XZ, VUW == ZXY) => *
        """.trimIndent()
        )
    }
}