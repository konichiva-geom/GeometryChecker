package external.geogebra

import kotlin.random.Random

@Deprecated("Moved to JS")
class GeogebraApi {
    val ggbResult = StringBuilder()
    private val SIZE = 10
    private val rnd = Random(42)

    fun addPoint(name: String, x: Int?, y: Int) {
        val xVal = x ?: rnd.nextInt(0, SIZE)
        val yVal = x ?: rnd.nextInt(0, SIZE)
        ggbResult.append("x=($xVal, $yVal)\n")
    }

    fun addAngle() {

    }

    fun connectPoints(first: String, second: String) {
        ggbResult.append("Segment($first, $second)\n")
    }

    fun lineBetween(first: String, second: String) {
        ggbResult.append("Line($first, $second)")
    }

    fun setPointStyle(name: String, block: Int) {
    }
}
