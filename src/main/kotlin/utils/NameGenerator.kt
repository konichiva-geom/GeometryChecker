package utils

object NameGenerator {
    private var index = 0

    fun getName() = "$${index++}"
}
