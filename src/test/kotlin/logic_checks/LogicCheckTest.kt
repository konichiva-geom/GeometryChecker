package logic_checks

import TestFactory.failDescription
import org.junit.jupiter.api.Disabled
import kotlin.test.Test

class LogicCheckTest {
    @Disabled("Not yet implemented")
    @Test
    fun failEqualPointsInNotation() {
        failDescription(
            """
            new A; new B;
            A == B
            A in AB
        """, "DDWIOfkewoi"
        )
    }
}