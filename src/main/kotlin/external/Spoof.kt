package external

object Spoof {
    /**
     * Change params in braces like %{someParam} to their values from [args].
     */
    fun changeAllIndicesInOrder(text: String, args: List<Pair<String, Any>>): String {
        val regexForArgs = Regex("%\\{\\w+}")
        val sb = StringBuilder(text)
        val mapOfArgs = args.toMap()

        regexForArgs.findAll(text)
            .sortedBy { -it.range.first }
            .map { Triple(it.range.first, it.range.last, it.value.substring(2, it.value.length - 1)) }
            .forEach { sb.replace(it.first, it.second + 1, (mapOfArgs[it.third] ?: "<NOT_FOUND>").toString()) }

        return sb.toString()
    }

    /**
     * If we try to send a map in GeomServer, it says that serializer is not capable.
     */
    fun mapToJsonString(map: Map<String, String>): String {
        if (map.isEmpty())
            return "{}"
        val res = StringBuilder("{")

        for (entry in map) {
            res.append("\"${entry.key}\"")
            res.append(":")
            res.append("\"${entry.value}\"")
            res.append(",")
        }
        res.deleteCharAt(res.length - 1)
        return res.append("}").toString()
    }
}
