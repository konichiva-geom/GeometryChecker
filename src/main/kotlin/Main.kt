import TheoremParser.getTheorems
import Utils.getOpByString
import Utils.mergeWithOperation
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.st.liftToSyntaxTreeGrammar
import entity.Point
import entity.Segment
import notation.IdentNotation
import notation.NumNotation
import notation.Point2Notation
import notation.Point3Notation
import notation.PointNotation
import notation.RayNotation
import notation.SegmentNotation

// TODO check wolfram alpha paid how can he check geom, geogebra

// class InRelation(left: RelatableNotation, right: RelatableNotation, isNot: Boolean = false) :
//     Relation(left, right, isNot)
//class Relation : Term // A in CD, AC intersects DB, new A

class Procedure
data class Theorem(val signature: Signature, val body: Any) {}
data class Signature(val name: String, val args: Any)

val symbolTable = SymbolTable()

interface Expr : Comparable<Expr> {
    fun flatten(): MutableMap<Any, Float> = mutableMapOf(this to 1f)

    fun getChildren(): List<Expr>
}

class MockExpr() : Expr {
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getChildren(): List<Expr> {
        TODO("Not yet implemented")
    }
}

open class BinaryExpr(val left: Expr, val right: Expr) : Expr {
    override fun getChildren(): List<Expr> = listOf(left, right)

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }
}

class BinaryEquals(left: Expr, right: Expr) : BinaryExpr(left, right)
class BinaryNotEquals(left: Expr, right: Expr) : BinaryExpr(left, right)
class BinaryGreater(left: Expr, right: Expr) : BinaryExpr(left, right)
class BinaryGEQ(left: Expr, right: Expr) : BinaryExpr(left, right)

class ArithmeticBinaryExpr(left: Expr, right: Expr, val op: (Float, Float) -> Float) : BinaryExpr(left, right) {
    override fun flatten(): MutableMap<Any, Float> = left.flatten().mergeWithOperation(right.flatten(), op)

    override fun toString(): String {
        return "$left${
            when (op) {
                { a: Float, b: Float -> a + b } -> "+"
                { a: Float, b: Float -> a - b } -> "-"
                { a: Float, b: Float -> a * b } -> "*"
                { a: Float, b: Float -> a / b } -> "/"
                else -> "#"
            }
        }$right"
    }
}

object GeomGrammar : Grammar<Any>() {
    // region entity prefix tokens
    private val ray by literalToken("ray")
    private val segment by literalToken("segment")
    //endregion

    private val negationToken by literalToken("not")
    private val newToken by literalToken("new")
    private val comment by regexToken("//.*\n?", ignore = true)
    val thDefStart by literalToken("th")
    private val returnToken by literalToken("return")
    private val number by regexToken("((\\d+\\.)?\\d*)|(\\d*)?\\.\\d+")
    private val angle by 3 times parser(this::point)
    private val line by 2 times parser(this::point) map { Pair(it[0].text, it[1].text) }
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

    //region creation tokens
    private val pointCreation by -newToken and point map {
        val res = PointNotation(it.text)
        symbolTable.newPoint(res)
        res
    }
    //endregion

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

    private val relatableNotation by (line map {
        val res = Point2Notation(it.first, it.second)
        symbolTable.getLine(res)
        res
    }) or (point map {
        MockExpr()
    }) or (identToken map { IdentNotation(it.text) })

    // segment AB, A, ray DF
    private val notation by (-segment and line map { SegmentNotation(it.first, it.second) }) or
        (-ray and line map { RayNotation(it.first, it.second) }) or (angle map {
        val res = Point3Notation(it[0].text, it[1].text, it[2].text)
        symbolTable.getAngle(res)
        res
    }) or relatableNotation or (number map {
        NumNotation(it.text.toIntOrNull() ?: it.text.toFloatOrNull() ?: throw Exception("Not a number"))
    })

    private val term by notation or (-lpar and parser(::arithmeticExpression) and -rpar) map {
        it as Expr
    }

    private val divMulChain: Parser<Any> by leftAssociative(term, div or mul) { a, op, b ->
        ArithmeticBinaryExpr(
            MockExpr(),
            MockExpr(),
            getOpByString(op.text)
        )
    }

    private val arithmeticExpression: Parser<Any> by leftAssociative(
        divMulChain,
        plus or minus
    ) { a, op, b ->
        ArithmeticBinaryExpr(MockExpr(), MockExpr(), getOpByString(op.text))
    }

    private val comparison by arithmeticExpression and compToken and arithmeticExpression map {
        // when(it.t2.text) {
        //     "==" -> BinaryEquals(it.t1, it.t3)
        //     else -> BinaryGEQ(it.t1, it.t3)
        // }
        it
    }
    private val relation: Parser<Any> by relatableNotation and relationToken and relatableNotation or
        (negationToken and parser(::relation)) map {
        // if (it is Tuple2<*, *>)
        //     createRelation(it.t2 as Tuple3<RelatableNotation, TokenMatch, RelatableNotation>, true)
        // else
        //     createRelation(it as Tuple3<RelatableNotation, TokenMatch, RelatableNotation>)
        it
    }
    private val binaryStatement by pointCreation or comparison or relation map { it }

    private val args by separatedTerms(binaryStatement or notation, comma)
    private val optionalArgs by optional(args) map { it ?: emptyList() }

    private val invocation by identToken and -lpar and args and -rpar map { Signature(it.t1.text, it.t2) }
    private val zeroArgsOrMoreInvocation by identToken and -lpar and optionalArgs and -rpar map {
        Signature(
            it.t1.text,
            it.t2
        )
    }
    private val theoremUsage by (zeroArgsOrMoreInvocation and -inferToken and (mul or args))//.liftToSyntaxTreeParser()

    // private val inference by (comparison and -inferToken and comparison map { it }) or
    //     (comparison and -comma and comparison and -inferToken and comparison map { it })

    private val block by -zeroOrMore(lineBreak) and separatedTerms(
        theoremUsage or /*inference or*/ binaryStatement, lineBreak
    ) and -zeroOrMore(lineBreak) map { it }

    private val returnStatement by -returnToken and args
    private val thStatement by theoremUsage or relation or invocation or comparison
    private val thBlock by -zeroOrMore(lineBreak) and (separatedTerms(
        thStatement, lineBreak,
    ) and optional(lineBreak and returnStatement) or returnStatement) and
        -zeroOrMore(lineBreak) map { it }

    private val thDef by -thDefStart and zeroArgsOrMoreInvocation and -colon and thBlock map { Theorem(it.t1, it.t2) }
    private val program by oneOrMore(thDef) or (4 times (identToken and -colon and -lineBreak and block))
    override val rootParser: Parser<Any> by program map { it }//block map { it.first() }
}

fun main() {
    val a = """ident1:
        //R==2*(3*4+(42-R))+A
       // D==3, R==2*(3*4+(42-R))+A => F==3
        tUse(T in A) => *
        //tUse() => T in B
        //
        ident2:
        
        tUse(T in A) => *
       // D==3, R==4+42 => F==3
        
        
        ident3:
        tUse() => T in A, B == C
       // D==3, R==4+42 => F==3
        ident:
        
        
      //  D==3, R==4+42 => F==3
        circle < 3
        """

    val th = """th def():
        D in A
        not D in A
        check(W==W)
        
        th def(D > 3* 2+4/T, F in E, D ): // re fewfw
        D in A
        circle in A
        check(W==W) // 
    //"""
    // val test = GeomGrammar.parseToEnd("tUse(T == A) => *")
    val res = GeomGrammar.parseToEnd(a)
    GeomGrammar.parseToEnd(a)
    val result = GeomGrammar.parseToEnd(th)
    val t = GeomGrammar.liftToSyntaxTreeGrammar().parseToEnd(th)
    t
    val theorems = getTheorems()
}