package utils

class NameGenerator {
    private var index = 0
    private var indexForUnknownPointSize = 0

    /**
     * Character ¤ is bigger than any english letter,
     * so [pipeline.EqualIdentRenamer] will not point to this name for renaming
     */
    fun getName() = "¤${index++}"

    /**
     * Character § is bigger than ¤
     */
    fun getUnknownPointQuantityName() = "§${indexForUnknownPointSize++}"
}
