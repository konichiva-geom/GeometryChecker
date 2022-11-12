import Utils.mergeWithOperation
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.st.liftToSyntaxTreeGrammar
import entity.ConstNumber
import entity.Entity
import entity.Point
import entity.Segment

// TODO check wolfram alpha paid how can he check geom, geogebra
class Relation
//class Relation : Term // A in CD, AC intersects DB, new A

class Procedure
data class TheoremDefinition(val name: String, val args: List<Procedure>, val body: List<Procedure>)
data class Point3Notation(var p1: String, var p2: String, var p3: String)
data class Point2Notation(var p1: String, var p2: String)

val symbolTable = SymbolTable()

interface Expr {
    fun flatten(): MutableMap<Any, Float>
}

class BinaryExpr(val left: Expr, val right: Expr, val op: (Float, Float) -> Float) : Expr {
    override fun flatten(): MutableMap<Any, Float> = left.flatten().mergeWithOperation(right.flatten(), op)
}

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
    private val relationToken by intersectsToken or shortIntersectsToken or
            inToken or perpendicularToken or shortPerpendicularToken
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

    private val entity by (segment and line map { Segment() }) or
            (ray and line map { Point() }) or (angle map {
        val res = Point3Notation(it[0].text, it[1].text, it[2].text)
        symbolTable.getAngle(res)
        res
    }) or (line map { symbolTable.getLine(Point2Notation(it[0].text, it[1].text)) }) or (point map {
        println("In entity")
        symbolTable.newPoint(it.text)
        it.text
    }) or (number map {
        ConstNumber(
            it.text.toIntOrNull() ?: it.text.toFloatOrNull() ?: throw Exception("Not a number")
        )
    }) or identToken map { Point() }

    private val term by entity or (-lpar and parser(::arithmeticExpression) and -rpar)

    private val divMulChain: Parser<Any> by leftAssociative(term, div or mul use { type }) { a, op, b -> Point() }

    private val arithmeticExpression: Parser<Any> by leftAssociative(
        divMulChain,
        plus or minus use { type }) { a, op, b -> b }

    private val comparison by arithmeticExpression and compToken and arithmeticExpression map { it.t1 }
    private val relation: Parser<Entity> by entity and relationToken and entity map { it.t1 }
    private val binaryStatement by relation or comparison

    private val args by separatedTerms(
        comparison or relation or entity, comma
    )//relation and zeroOrMore(comma and relation) map {it.t1}
    private val optionalArgs by optional(args) map { Point() }

    private val invocation by identToken and -lpar and args and -rpar
    private val zeroArgsOrMoreInvocation by identToken and -lpar and optionalArgs and -rpar
    private val theoremParser by zeroArgsOrMoreInvocation and -inferToken and (mul or args) map { it.t2 }

    private val inference by (comparison and -inferToken and comparison map { it.t1 }) or
            (comparison and -comma and comparison and -inferToken and comparison map { it.t1 })

    private val block by -zeroOrMore(lineBreak) and separatedTerms(
        theoremParser or inference or binaryStatement, lineBreak
    ) and -zeroOrMore(lineBreak) map { it }

    private val returnStatement by -returnToken and args
    private val thStatement by relation or invocation or comparison
    private val thBlock by -zeroOrMore(lineBreak) and separatedTerms(
        thStatement, lineBreak
    ) and -zeroOrMore(lineBreak) map { it }

    private val thDef by thDefStart and zeroArgsOrMoreInvocation and -colon and thBlock
    private val program by (2 times thDef) or (4 times (identToken and -colon and -lineBreak and block))
    override val rootParser: Parser<Entity> by program map { Point() }//block map { it.first() }
}


fun main() {
    val a = """ident1:
        R==2*(3*4+(42-R))+A
        D==3, R==2*(3*4+(42-R))+A => F==3
        //
        ident2:
        
        
        D==3, R==4+42 => F==3
        
        
        ident3:
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
        circle in A
        check(W==W) // 
    //"""
    val res = GeomGrammar.liftToSyntaxTreeGrammar().parseToEnd(a)
    GeomGrammar.parseToEnd(a)
    val result = GeomGrammar.parseToEnd(th)
    GeomGrammar.liftToSyntaxTreeGrammar().parseToEnd(th)
    // println(result)
}