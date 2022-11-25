import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.utils.Tuple3
import entity.Entity
import notation.RelatableNotation

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