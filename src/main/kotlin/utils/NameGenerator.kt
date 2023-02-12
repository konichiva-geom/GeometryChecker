package utils

object NameGenerator {
    private var index = 0

    /**
     * Character ¤ is bigger than any english letter,
     * so [pipeline.EqualIdentRenamer] will not point to this name for renaming
     */
    fun getName() = "¤${index++}"
}
