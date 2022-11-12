import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.utils.Tuple3
import entity.Entity
import notation.RelatableNotation

class Relation(
    val left: RelatableNotation,
    val right: RelatableNotation,
    val symbol: RelationType,
    val isNot: Boolean = false
)

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

fun createRelation(parsed: Tuple3<RelatableNotation, TokenMatch, RelatableNotation>, isNot: Boolean = false): Relation {
    val relationType = RelationType.getRelationType(parsed.t2.text)
    return Relation(parsed.t1, parsed.t3, relationType, isNot)
}