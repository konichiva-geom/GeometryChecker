package relations

import entity.EntityRelations

object Intersections {
    val intersections = mutableMapOf<EntityRelations, EntityRelations>()
}

object In {
    // key in value
    val inMap = mutableMapOf<EntityRelations, Set<EntityRelations>>()
}