interface Relation {
    fun check(): Boolean
}

enum class RelationType {
    IN,
    INTERSECTS,
    PERPENDICULAR;

    companion object {
        fun getRelationType(text: String): RelationType {
            return when (text) {
                "in" -> IN
                "intersects", "∩" -> INTERSECTS
                "perpendicular", "⊥" -> PERPENDICULAR
                else -> throw Exception("$text is not a relation")
            }
        }
    }
}