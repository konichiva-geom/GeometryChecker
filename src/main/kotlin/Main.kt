import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import entity.Entity

interface GeomTerm
//class Relation : Term // A in CD, AC intersects DB, new A

class Procedure
data class Eq(val left: GeomTerm, val right: GeomTerm) : GeomTerm
data class TheoremDefinition(val name: String, val args: List<GeomTerm>, val body: Procedure)

val symbolTable = SymbolTable()

object GeomGrammar : Grammar<Any>() {
    //val number by regexToken("((\\d+\\.)?\\d*)|(\\d*)?\\.\\d+")
    val angle by 3 times parser(this::point)
    val line by 2 times parser(this::point)
    val point by regexToken("[A-Z][0-9]*")

    // val inRelation by literalToken("in")
    //  val perpendicular by literalToken(" perpendicular ") or literalToken("⊥")
    private val entity: Parser<GeomTerm> by (angle map {
        symbolTable.getAngle(
            it[0].text,
            it[1].text,
            it[2].text
        )
    }) or (line map {
        symbolTable.getLine(
            it[0].text,
            it[1].text
        )
    }) or (point map { symbolTable.newPoint(it.text) })
    // or (number map {
    //     ConstNumber(
    //         it.text.toIntOrNull() ?: it.text.toFloatOrNull() ?: throw Exception("Not a number")
    //     )
    // })

    val eqToken = literalToken("==")
    val eq: Parser<GeomTerm> by leftAssociative(entity, eqToken use { type }) { a, _, b -> Eq(a, b) }
    // val intersects by (leftAssociative(entity, literalToken("!=")) { a, _, b -> Eq(a, b) })// or literalToken("∩")
    val assignment by point and literalToken("=")// and intersects
    // val relation by eq

    // val term by relation
    //  val args by term and zeroOrMore(term and literalToken(","))
    //
    // private val theoremParser by regexToken("")
    // val theorems = oneOrMore(theoremParser)

    // val structureParser
//val theoremDefinitionParser by
    override val rootParser by eq
}


class ArithmeticsEvaluator : Grammar<Entity>() {
    val angle by 3 times parser(this::point)
    val line by 2 times parser(this::point)
    val point by regexToken("[A-Z][0-9]*")
    val lpar by literalToken("(")
    val rpar by literalToken(")")

    // val inRelation by literalToken("in")
    //  val perpendicular by literalToken(" perpendicular ") or literalToken("⊥")
    private val entity: Parser<Entity> by (angle map {
        symbolTable.getAngle(
            it[0].text,
            it[1].text,
            it[2].text
        )
    }) or (line map {
        symbolTable.getLine(
            it[0].text,
            it[1].text
        )
    }) or (point map { symbolTable.newPoint(it.text) }) or
    (skip(lpar) and parser(::rootParser) and skip(rpar))
    // or (number map {
    //     ConstNumber(
    //         it.text.toIntOrNull() ?: it.text.toFloatOrNull() ?: throw Exception("Not a number")
    //     )
    // })

    val mul by literalToken("*")
    val pow by literalToken("^")
    val div by literalToken("/")
    val minus by literalToken("-")
    val plus by literalToken("+")
    val ws by regexToken("\\s+", ignore = true)

    val powChain:Parser<Entity> by leftAssociative(entity, pow) { a, _, b -> b }

    val divMulChain:Parser<Entity> by leftAssociative(powChain, div or mul use { type }) { a, op, b ->
        a
    }

    val subSumChain:Parser<Entity> by leftAssociative(divMulChain, plus or minus use { type }) { a, op, b ->
        b
    }

    override val rootParser: Parser<Entity> by subSumChain
}

fun main(args: Array<String>) {
    val expr = "A + R * (B - V^C) - D^E^F * (G + H)"
    val result = ArithmeticsEvaluator().parseToEnd(expr)
    println(result)
}

// fun main() {
//     val result = GeomGrammar.parseToEnd("AD==AB")
//     println(result)
// }