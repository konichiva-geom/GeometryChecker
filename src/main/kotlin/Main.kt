import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import entity.ConstNumber
import entity.Entity
import entity.Point

// TODO check wolfram alpha paid how can he check geom, geogebra

interface GeomTerm
//class Relation : Term // A in CD, AC intersects DB, new A

class Procedure
data class Eq(val left: GeomTerm, val right: GeomTerm) : GeomTerm
data class TheoremDefinition(val name: String, val args: List<GeomTerm>, val body: Procedure)

val symbolTable = SymbolTable()

object GeomGrammar : Grammar<Entity>() {
    // region entity prefix tokens
    private val ray by literalToken("ray")
    private val segment by literalToken("segment")
    //endregion

    private val comment by regexToken("//.*\n?", ignore = true)
    val thDefStart by literalToken("th")
    private val returnToken by literalToken("return")
    private val number by regexToken("((\\d+\\.)?\\d*)|(\\d*)?\\.\\d+")
    private val angle by 3 times parser(this::point)
    private val line by 2 times parser(this::point)
    private val point by regexToken("[A-Z][0-9]*")

    //region relation tokens
    private val intersectsToken by literalToken("intersects")
    private val shortIntersectsToken by literalToken("∩")
    private val perpendicularToken by literalToken("perpendicular")
    private val shortPerpendicularToken by literalToken("⊥")
    private val inToken by literalToken("in")
    private val relationToken by intersectsToken or shortIntersectsToken or inToken or perpendicularToken or shortPerpendicularToken
    //endregion

    //region comparison tokens
    private val eqToken by literalToken("==")
    private val neqToken by literalToken("!=")
    private val geq by literalToken(">=")
    private val leq by literalToken("<=")
    private val gt by literalToken(">")
    private val lt by literalToken("<")
    private val compToken by geq or leq or gt or lt or eqToken or neqToken
    // endregion

    private val mul by literalToken("*")
    private val div by literalToken("/")
    private val minus by literalToken("-")
    private val plus by literalToken("+")

    private val colon by literalToken(":")
    private val comma by literalToken(",")
    private val inferToken by literalToken("=>")
    private val lpar by literalToken("(")
    private val rpar by literalToken(")")
    private val ws by regexToken("[\\t ]+", ignore = true)
    private val identToken by regexToken("\\w+[\\w\\d_]*")
    private val lineBreak by regexToken("(\\n[\\t ]*)+")
    private val optionalLineBreak by regexToken("(\\n[\\t ]*)*")

    private val entity: Parser<Entity> by (segment and line map { Point() }) or (ray and line map { Point() }) or (angle map {
        symbolTable.getAngle(
            it[0].text, it[1].text, it[2].text
        )
    }) or (line map { symbolTable.getLine(it[0].text, it[1].text) }) or (point map {
        println("In entity")
        symbolTable.newPoint(it.text)
    }) or (number map {
        ConstNumber(
            it.text.toIntOrNull() ?: it.text.toFloatOrNull() ?: throw Exception("Not a number")
        )
    }) or identToken map { Point() }

    private val divMulChain: Parser<Entity> by leftAssociative(entity, div or mul use { type }) { a, op, b -> a }

    private val arithmeticExpression by leftAssociative(divMulChain, plus or minus use { type }) { a, op, b -> b }

    private val comparison by arithmeticExpression and compToken and GeomGrammar.arithmeticExpression map { it.t1 }
    private val relation: Parser<Entity> by entity and relationToken and entity map { it.t1 }
    private val binaryStatement by relation or comparison

    private val args by separatedTerms(
        comparison or relation or entity, comma
    )//relation and zeroOrMore(comma and relation) map {it.t1}
    private val optionalArgs by optional(args) map { Point() }

    private val invocation by identToken and lpar and args and rpar
    private val zeroArgsOrMoreInvocation by identToken and lpar and optionalArgs and rpar
    private val theoremParser by zeroArgsOrMoreInvocation and inferToken and (mul or args) map { it.t1.t3 }

    private val inference by (comparison and inferToken and comparison map { it.t1 }) or (comparison and comma and comparison and inferToken and comparison map { it.t1 })

    private val block by zeroOrMore(lineBreak) and separatedTerms(
        theoremParser or inference or binaryStatement, lineBreak
    ) and zeroOrMore(lineBreak) map { it.t2 }

    private val returnStatement by returnToken and args
    private val thStatement by relation or invocation or comparison
    private val thBlock by zeroOrMore(lineBreak) and separatedTerms(
        thStatement, lineBreak
    ) and zeroOrMore(lineBreak) map { it.t2 }
    private val thDef by thDefStart and zeroArgsOrMoreInvocation and colon and thBlock

    private val program by (2 times thDef) or (4 times (identToken and colon and lineBreak and block))

    // val structureParser
//val theoremDefinitionParser by
    override val rootParser: Parser<Entity> by program map { Point() }//block map { it.first() }
}

// fun main(args: Array<String>) {
//     val expr = "A + R * (B - V^C) - D^E^F * (G + H)"
//     val result = ArithmeticsEvaluator().parseToEnd(expr)
//     println(result)
// }

fun main() {
    val a = """ident:
        D==3, R==4+42 => F==3
        //
        ident:
        
        
        D==3, R==4+42 => F==3
        
        
        ident:
        D==3, R==4+42 => F==3
        ident:
        
        
        D==3, R==4+42 => F==3
        circle < 3
        """

    val th = """th def():
        D in A
        check(W==W)
        
        th def(D > 3* 2+4/T, F in E, D ): // re fewfw
        D in A
        circke in A
        check(W==W) // 
    //"""
    GeomGrammar.parseToEnd(a)
    val result = GeomGrammar.parseToEnd(th)
    // println(result)
}