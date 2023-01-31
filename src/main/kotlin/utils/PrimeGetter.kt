package utils

import java.io.File

/**
 * Returns next prime number for comparison vectors
 */
object PrimeGetter {
    val primes = File("src/main/resources/primes.txt").readText().split(" ", "\n").map { it.toInt() }
}
