import TestFactory.parseFirst
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ArithmeticTest {
    @Test
    fun testAddition() {
        val threeLevelFraction = parseFirst("ABC == 2+2/(2+2/(2+ABE))")
        assertEquals(threeLevelFraction.toString(), "2ABE*ABC+6ABC == 6ABE+16")

        val arithmeticExpr3 = parseFirst("ABC/(3/7) == (2 + ABC) * (3 + VF) + 1 /(2 + AG)")
        println(arithmeticExpr3)
        assertEquals(arithmeticExpr3.toString(), "7ABC*AG+14ABC == 6AG*FV+9ABC*AG+6ABC*FV+3ABC*FV*AG+18ABC+18AG+12FV+39")


        val fractionInNumerator = parseFirst("ABC == 2 + (2+(2+ABE)/2)/2")
        assertEquals(fractionInNumerator.toString(), "4ABC == ABE+14")

        val arithmeticExpr = parseFirst("ABC == 90 - 7 BDC + 12 - 6 - CBA - CDB")
        assertEquals(arithmeticExpr.toString(), "ABC == -8BDC-ABC+96")
        val arithmeticExpr2 = parseFirst("ABC == (2 + ABC) * (3 + VF)")
        assertEquals(arithmeticExpr2.toString(), "ABC == ABC*FV+3ABC+2FV+6")
    }
}
