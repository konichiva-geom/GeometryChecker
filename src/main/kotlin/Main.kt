import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.components
import entity.ConstNumber
import entity.Entity

interface GeomTerm
//class Relation : Term // A in CD, AC intersects DB, new A

class Procedure
data class Eq(val left: GeomTerm, val right: GeomTerm) : GeomTerm
data class TheoremDefinition(val name: String, val args: List<GeomTerm>, val body: Procedure)

val symbolTable = SymbolTable()

object GeomGrammar : Grammar<Entity>() {
    val number by regexToken("((\\d+\\.)?\\d*)|(\\d*)?\\.\\d+")
    val angle by 3 times parser(this::point)
    val line by 2 times parser(this::point)
    val point by regexToken("[A-Z][0-9]*")

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
    }) or (point map { symbolTable.newPoint(it.text) }) or (number map {
        ConstNumber(
            it.text.toIntOrNull() ?: it.text.toFloatOrNull() ?: throw Exception("Not a number")
        )
    })

    val eqToken by literalToken("==")
    val neqToken by literalToken("!=")
    val intersectsToken by literalToken("intersects")
    val shortIntersectsToken by literalToken("∩")
    val inToken by literalToken("in")

    val geq by literalToken(">=")
    val leq by literalToken("<=")
    val gt by literalToken(">")
    val lt by literalToken("<")
    val mul by literalToken("*")
    val div by literalToken("/")
    val minus by literalToken("-")
    val plus by literalToken("+")

    val divMulChain: Parser<Entity> by leftAssociative(entity, div or mul use { type }) { a, op, b ->
        a
    }
    val arithmeticExpression by  leftAssociative(divMulChain, plus or minus use { type }) { a, op, b ->
        b
    }
    val relation: Parser<Entity> by leftAssociative(
        arithmeticExpression, eqToken or
            neqToken or
            intersectsToken or
            shortIntersectsToken or
            inToken or
            gt or lt or geq or leq
    ) { a, op, b ->
        println("${a}, $b, $op")
        a }
    val ws by regexToken("\\s+", ignore = true)
    // val intersects by (leftAssociative(entity, literalToken("!=")) { a, _, b -> Eq(a, b) })// or literalToken("∩")
    //val assignment by point and literalToken("=")// and intersects
    // val relation by eq

    val comma by literalToken(",")
    // val term by relation
     val args by separatedTerms(relation, comma)//relation and zeroOrMore(comma and relation) map {it.t1}
    //
    val inferToken by literalToken("=>")
    val identToken by regexToken("\\w+[\\w\\d_]*")
    val lpar by literalToken("(")
    val rpar by literalToken(")")
    private val theoremParser by identToken and lpar and args and rpar and inferToken and (mul or args) map { it.t3.first() }
    // val theorems = oneOrMore(theoremParser)

    // val structureParser
//val theoremDefinitionParser by
    override val rootParser: Parser<Entity> by theoremParser
}

class ArithmeticsEvaluator : Grammar<Entity>() {
    val angle by 3 times parser(this::point)
    val line by 2 times parser(this::point)
    val point by regexToken("[A-Z][0-9]*")
    val lpar by literalToken("(")
    val rpar by literalToken(")")

    // val inRelation by literalToken("in")
    //  val perpendicular by literalToken(" perpendicular ") or literalToken("⊥")
    // TODO CACREFFUL WITH PARSER TYPE HERE AND IN ROOT
    // TODO CAREFUL WITH ASSIGNMENTS! SHOULD USE by FOR PREDICATES!
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

    val powChain: Parser<Entity> by leftAssociative(entity, pow) { a, _, b -> b }

    val divMulChain: Parser<Entity> by leftAssociative(powChain, div or mul use { type }) { a, op, b ->
        a
    }

    val subSumChain: Parser<Entity> by leftAssociative(divMulChain, plus or minus use { type }) { a, op, b ->
        b
    }

    override val rootParser: Parser<Entity> by subSumChain
}

// fun main(args: Array<String>) {
//     val expr = "A + R * (B - V^C) - D^E^F * (G + H)"
//     val result = ArithmeticsEvaluator().parseToEnd(expr)
//     println(result)
// }

fun main() {
    val result = GeomGrammar.parseToEnd("dsd(1==B) => 4==3, 3>5")
    println(result)
}