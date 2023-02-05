import TestFactory.failDescription
import TestFactory.parseFirst
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ArithmeticTest {
    @Test
    fun testAddition() {
        val arithmeticExpr3 = parseFirst("ABC/(3/7) == (2 + ABC) * (3 + VF) + 1 /(2 + AG)")
        assertEquals(
            arithmeticExpr3.toString(),
            "7ABC*AG+14ABC == 6AG*FV+9ABC*AG+6ABC*FV+3ABC*FV*AG+18ABC+18AG+12FV+39"
        )

        val threeLevelFraction = parseFirst("ABC == 2+2/(2+2/(2+ABE))")
        assertEquals(threeLevelFraction.toString(), "ABE*ABC+3ABC == 3ABE+8")



        val fractionInNumerator = parseFirst("ABC == 2 + (2+(2+ABE)/2)/2")
        assertEquals(fractionInNumerator.toString(), "4ABC == ABE+14")

        val arithmeticExpr = parseFirst("ABC == 90 - 7 BDC + 12 - 6 - CBA - CDB")
        assertEquals(arithmeticExpr.toString(), "ABC == -8BDC-ABC+96")
        val arithmeticExpr2 = parseFirst("ABC == (2 + ABC) * (3 + VF)")
        assertEquals(arithmeticExpr2.toString(), "ABC == ABC*FV+3ABC+2FV+6")
    }

    @Test
    fun testWithZeros() {
        val manyZeros = parseFirst("ABC == (2-10/5)*RTY + 0*BSC + 0* SDS")
        assertEquals(manyZeros.toString(), "ABC == 0")

        val zeroAsTwoFractions = parseFirst("ABC == 2*(3ABE/5)-(6ABE/5)")
        assertEquals(zeroAsTwoFractions.toString(), "ABC == 0")

        val zeroInNumerator = parseFirst("ABC == (1-1)*(3BC)/2")
        assertEquals(zeroInNumerator.toString(), "ABC == 0")

        failDescription("ABC == 1/0", "Expression leads to one with zero in denominator")
    }
}