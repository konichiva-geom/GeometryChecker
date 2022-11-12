package entity

class Ray() {}
class Segment():Entity {
    fun inRelation() {
    }
}

class Point(val distinctSet: MutableSet<String> = mutableSetOf()) : Entity {
    val lines: MutableMap<String, Boolean> = mutableMapOf()
    val segments: MutableMap<String, Boolean> = mutableMapOf()
    val rays: MutableMap<String, Boolean> = mutableMapOf()

    fun inLine(name: String): Boolean? {
        return lines[name] ?: rays[name] ?: segments[name]
    }
}