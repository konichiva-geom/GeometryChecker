import TestFactory.passDescription
import org.junit.jupiter.api.Test

internal class ArithmeticTest {
    @Test
    fun testAddition() {
        passDescription("""
            new A; new B; new C;
            ABC == 90 - 7 BDC + 12 - 6 - CBA - CDB
        """)
    }
}