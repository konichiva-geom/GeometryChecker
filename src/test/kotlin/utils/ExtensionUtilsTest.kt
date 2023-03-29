package utils

import utils.ExtensionUtils.isAlmostZero
import kotlin.test.Test

internal class ExtensionUtilsTest {
    @Test
    fun testIsAlmostZero() {
        assert(0.0.isAlmostZero())
        assert(0.0000009.isAlmostZero())
        assert(!0.000001.isAlmostZero())
    }
}