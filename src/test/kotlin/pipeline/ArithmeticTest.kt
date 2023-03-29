package pipeline

import TestFactory.failDescription
import TestFactory.parseFirst
import TestFactory.passTask
import entity.expr.binary_expr.BinaryExpr
import math.*
import pipeline.inference.InferenceProcessor
import pipeline.symbol_table.SymbolTable
import utils.multiSetOf
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
        assertEquals(threeLevelFraction.toString(), "2ABE*ABC+6ABC == 6ABE+16")


        val fractionInNumerator = parseFirst("ABC == 2 + (2+(2+ABE)/2)/2")
        assertEquals(fractionInNumerator.toString(), "4ABC == ABE+14")

        val arithmeticExpr = parseFirst("ABC == 90 - 7 BDC + 12 - 6 - CBA - CDB")
        arithmeticExpr.getChildren().last().toString()
        assertEquals(arithmeticExpr.toString(), "ABC == -8BDC-ABC+96")
        val arithmeticExpr2 = parseFirst("ABC == (2 + ABC) * (3 + VF)")
        assertEquals(arithmeticExpr2.toString(), "ABC == ABC*FV+3ABC+2FV+6")
    }

    @Test
    fun testWithZeros() {
        val manyZeros = parseFirst("ABC == (2-10/5)*RTY + 0*BSC + 0* SDS")
        assertEquals(manyZeros.toString(), "5ABC == 0")

        val zeroAsTwoFractions = parseFirst("ABC == 2*(3ABE/5)-(6ABE/5)")
        assertEquals(zeroAsTwoFractions.toString(), "25ABC == 0")

        val zeroInNumerator = parseFirst("ABC == (1-1)*(3BC)/2")
        assertEquals(zeroInNumerator.toString(), "2ABC == 0")

        failDescription("ABC == 1/0", "Expression leads to one with zero in denominator")
    }

    @Test
    fun testVectorCreation() {
        val withMultiplication = parseFirst(" 2 AB * CD == 3AB + 2CD")
        val table = SymbolTable()

        val left = vectorFromArithmeticMap(((withMultiplication as BinaryExpr).left as ArithmeticExpr).map, table)
        val right = vectorFromArithmeticMap(((withMultiplication).right as ArithmeticExpr).map, table)

        val expected = mutableMapOf(
            multiSetOf(2) to -2.0,
            multiSetOf(3) to -3.0,
            multiSetOf(2, 3) to 2.0
        )

        left.mergeWithOperation(right, "-").forEach {
            assert(expected[it.key] == it.value)
        }
    }

    @Test
    fun testArithmeticEquals() {
        passTask(
            """
            description:
                new A; new B; new C; new D
                ABC == 90 + 2DCB
            prove:
                BCD == ABC/2 - 45
            solution:;
        """
        )
    }

    @Test
    fun testInferenceInVectors() {
        passTask(
            """
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
        """
        )
    }

    @Test
    fun testTrivial() {
        passTask(
            """
            description:
                new A; new B; new C; new D
                ABC == BCD + 90
                ABC == BCD + 90
                ABC == BCD + 90
            prove:
                ABC == BCD + 90
            solution:;
        """
        )
    }

    @Test
    fun testMultiset() {
        passTask("""
        description:
            new A; new B; new C;
            AB * AB + 2 * AB - 1 == 0
        prove:
            
        solution:
            
        """)
    }
}
