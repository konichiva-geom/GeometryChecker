package error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

internal class SpoofErrorTest {
    @Test
    fun testSameArgUsedTwice() {
        val exception = assertFails {
            throw SpoofError("some error text %{point} with args %{point}", "point" to "A")
        }
        assertEquals(exception.message, "some error text A with args A")

        val exception2 = assertFails {
            throw SpoofError(
                "some error text %{point} with additional %{second} args %{point}",
                "second" to "B",
                "point" to "A"
            )
        }
        assertEquals(exception2.message, "some error text A with additional B args A")
    }
}