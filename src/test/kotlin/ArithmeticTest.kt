import TestFactory.parseFirst
import math.ArithmeticExpr
import utils.Utils.mergeMapToDivNotation
import kotlin.test.Test

internal class ArithmeticTest {
    @Test
    fun testAddition() {
        val testReal = parseFirst("ABC == 2+2/(2+2/(2+ABE))")
        println(testReal)
        val optimized = mergeMapToDivNotation((testReal.getChildren()[1] as ArithmeticExpr).notationFractionMap)

        val testRealUpper = parseFirst("ABC == 2 + (2+(2+ABE)/2)/2")
        val optimizedUpper = mergeMapToDivNotation((testRealUpper.getChildren()[1] as ArithmeticExpr).notationFractionMap)

        val arithmeticExpr = parseFirst("ABC == 90 - 7 BDC + 12 - 6 - CBA - CDB")
        println(arithmeticExpr)
        val arithmeticExpr2 = parseFirst("ABC == (2 + ABC) * (3 + VF)")
        println(arithmeticExpr2)
        val arithmeticExpr3 = parseFirst("ABC/(3/7) == (2 + ABC) * (3 + VF) + 1 /(2 + AG)")
        println(arithmeticExpr3)
    }
}
