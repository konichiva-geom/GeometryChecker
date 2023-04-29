package logic_checks

import TestFactory.failDescription
import TestFactory.passTask
import kotlin.test.Test

internal class LogicCheckTest {
    @Test
    fun failEqualPointsInNotation() {
        failDescription(
            """
            new A; new B;
            A == B
            A in AB
        """, "Line AA consists of same points"
        )
    }

    @Test
    fun failEqualPointsInNotationTwoSteps() {
        failDescription(
            """
            new A; new B; new C; new E; new D;
            B == C
            A == C
            E in AB
        """, "Line AA consists of same points"
        )
    }

    @Test
    fun testCollectionRenaming() {
        passTask(
            """
            description:
                new A; new B; new C; new E; new D;
                E in CD
                B == C;
            prove:
                E in DC
                E in DB
            solution:;
        """
        )
    }

    @Test
    fun testNotationRenaming() {
        passTask(
            """
            description:
                new A; new B; new C; new E; new D;
                ∠ABE == ∠ABD
                C == E
            prove:
                C == E
                ∠ABC == ∠ABD
            solution:;
        """
        )
    }
}