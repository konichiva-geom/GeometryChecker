package inference

enum class ExprName {
    EQUALS,
    PARALLEL,
    PERPENDICULAR,
    IN,
}

class InferenceChecker {
    val inferenceSets = mutableMapOf<ExprName, Set<Inference>>()
}
