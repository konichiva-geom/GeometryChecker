import TestFactory.failDescription
import TestFactory.parseFirst
import TestFactory.passTask
import entity.expr.BinaryExpr
import math.*
import pipeline.SymbolTable
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ArithmeticTest {
    @Test
    fun testAddition() {
        val arithmeticExpr3 = parseFirst("ABC/(3/7) == (2 + ABC) * (3 + VFB) + 1 /(2 + AGB)")

        assertEquals(
            arithmeticExpr3.toString(),
            "7AGB*ABC+14ABC == 6AGB*BFV+9AGB*ABC+6BFV*ABC+3BFV*ABC*AGB+18AGB+12BFV+18ABC+39"
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

    @Test
    fun testVectorCreation() {
        val withMultiplication = parseFirst(" 2 AB * CD == 3AB + 2CD")
        val table = SymbolTable()

        val left = vectorFromArithmeticMap(((withMultiplication as BinaryExpr).left as ArithmeticExpr).map, table)
        val right = vectorFromArithmeticMap(((withMultiplication).right as ArithmeticExpr).map, table)

        val expected = mutableMapOf(
            setOf(2) to FractionFactory.fromInt(-2),
            setOf(3) to FractionFactory.fromInt(-3),
            setOf(2, 3) to FractionFactory.fromInt(2)
        )

        left.mergeWithOperation(right, "-").forEach {
            assert(expected[it.key].contentEquals(it.value))
        }

        val (nullified, substitution) = left.mergeWithOperation(right, "-").getNullifiedAndSubstitution()
        table.segmentVectors.simplifyVectorCollection(nullified, substitution)


    }

    @Test
    fun testResolveVector() {
//        val table = SymbolTable()
//        val resolvableExpr = parseFirst("ABC == 90 + 2DCB")
//        val resolveLeft =
//            vectorFromArithmeticMap(((resolvableExpr as BinaryExpr).left as ArithmeticExpr).map, table)
//        val resolveRight = vectorFromArithmeticMap(((resolvableExpr).right as ArithmeticExpr).map, table)
//
//        table.angleVectors.resolveVector(resolveLeft.mergeWithOperation(resolveRight, "-"))


    }

    @Test
    fun testArithmeticEquals() {
        passTask("""
            description:
                new A; new B; new C; new D
                ABC == 90 + 2DCB
            prove:
                BCD == ABC/2 - 45
            solution:;
        """)
    }

    @Test
    fun testInferenceInVectors() {
        passTask("""
             description:
                new A; new B; new C
                AB == 2AC + BC - 6 
                AC == 7AB - 3
                BC + AC == AB + AC + 5
            prove:
                AB == 1/2
                BC == 11/2
                AC == 1/2
            solution:;
        """)
    }

    @Test
    fun testTrivial() {
        passTask("""
            description:
                new A; new B; new C; new D
                ABC == BCD + 90
                ABC == BCD + 90
                ABC == BCD + 90
            prove:
                ABC == BCD + 90
            solution:;
        """.trimIndent())
    }
}
