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
                VU == XY
                UW == ZX
            prove:
                VW == YZ
                UVW == XYZ
                UWV == XZY
            solution:
                equal_sided_triangles_i(VU == XY, UW == XZ, VUW == ZXY) => *
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
                YZ == WX
            prove:
                WY == XZ
                YWX == YZX
                YXW == XYZ
            solution:
                equal_sided_triangles_i(XY == XY, YZ == XW, ZYX == WXY) => *
        """.trimIndent()
        )
    }
}
