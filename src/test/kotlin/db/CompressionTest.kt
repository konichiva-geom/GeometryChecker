package db

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CompressionTest {
    @Test
    fun testDifferentCompressions() {
        testCompress("1")
        testCompress(
            """
            Some text to compress
        """
        )
        testCompress(
            """
            русский текст
            english text
            中文文本
            symbols: !@#$%^&*()—♫♣◘∩""⊥
        """
        )
        assertFails("Nothing to compress") { compress("") }
    }

    private fun testCompress(text: String) {
        val map = compress(text)
        val (bytes, size) = encode(text, map)
        val res = decode(BitList.fromByteArray(bytes, size), map.entries.associateBy({ it.value }) { it.key })
        assertEquals(text, res)
    }
}
