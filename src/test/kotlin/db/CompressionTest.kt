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

    @Test
    fun fuzzTest() {
        for (i in 0..100) {
            val str = getRandomString()
            testCompress(str)
            println(str)
        }
    }

    private fun testCompress(text: String) {
        val map = compress(text)
        val (bytes, size) = encode(text, map)
        val res = decode(BitList.fromByteArray(bytes, size), map.entries.associateBy({ it.value }) { it.key })
        assertEquals(text, res)
    }

    private fun getRandomString(): String {
        val res = StringBuilder()
        for (i in 1..IntRange(2, 1000).random()) {
            res.append(CharRange(Char.MIN_VALUE, Char.MAX_VALUE).random())
        }
        return res.toString()
    }
}
