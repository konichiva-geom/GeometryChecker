object Utils {
    fun sortLine(p1:String, p2:String): Pair<String, String> {
        if(p1==p2) throw Exception("Line consists of distinct points")
        return if(p1 < p2) Pair(p1, p2) else Pair(p2, p1)
    }

    fun sortAngle(p1:String, p2:String, p3:String): Triple<String, String, String> {
        return if(p1 < p3) Triple(p1, p2, p3) else Triple(p3, p2, p1)
    }
}