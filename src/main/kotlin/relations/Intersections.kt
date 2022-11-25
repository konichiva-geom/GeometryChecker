package relations

import entity.Entity

object Intersections {
    val intersections = mutableMapOf<Entity, Entity>()
}

object In {
    // key in value
    val inMap = mutableMapOf<Entity, Set<Entity>>()
}