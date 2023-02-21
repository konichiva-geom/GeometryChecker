package entity.point_collection

import pipeline.EqualIdentRenamer

object PointCollectionFactory {
    fun addSubscriber(equalIdentRenamer: EqualIdentRenamer, pointCollection: PointCollection<*>) {
        equalIdentRenamer.addSubscribers(pointCollection, *pointCollection.getPointsInCollection().toTypedArray())
    }

    fun createRayPointCollection(start: String, points: MutableSet<String>): RayPointCollection {
        return RayPointCollection(start, points)
    }
}