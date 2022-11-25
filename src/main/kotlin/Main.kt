import TheoremParser.addTheorems
import Utils.getRelationByString
import Utils.mergeWithOperation
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.st.liftToSyntaxTreeGrammar
import com.github.h0tk3y.betterParse.utils.Tuple2
import com.github.h0tk3y.betterParse.utils.Tuple3
import notation.IdentNotation
import notation.Notation
import notation.NumNotation
import notation.Point2Notation
import notation.Point3Notation
import notation.PointNotation
import notation.RayNotation
import notation.SegmentNotation
import relations.In
import java.util.ArrayList

val lambdas = mutableListOf<(Float, Float) -> Float>({ a: Float, b: Float -> a + b },
    { a: Float, b: Float -> a - b },
    { a: Float, b: Float -> a * b },
    { a: Float, b: Float -> a / b })
val signToLambda = mutableMapOf(
    "+" to lambdas[0],
    "-" to lambdas[1],
    "*" to lambdas[2],
    "/" to lambdas[3]
)
val lambdaToSign = mutableMapOf(
    lambdas[0] to "+",
    lambdas[1] to "-",
    lambdas[2] to "*",
    lambdas[3] to "/"
)

// TODO check wolfram alpha paid how can he check geom, geogebra

// class InRelation(left: RelatableNotation, right: RelatableNotation, isNot: Boolean = false) :
//     Relation(left, right, isNot)
//class Relation : Term // A in CD, AC intersects DB, new A

class Procedure

val symbolTable = SymbolTable()

interface Expr : Comparable<Expr> {
    fun flatten(): MutableMap<Any, Float> = mutableMapOf(this to 1f)

    fun getChildren(): List<Expr>
}

class MockExpr : Expr {
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getChildren(): List<Expr> {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "%"
}

abstract class BinaryExpr(val left: Expr, val right: Expr) : Expr, Relation {
    override fun getChildren(): List<Expr> = listOf(left, right)

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }
}

class PrefixNot(private val expr: Expr) : Expr {
    override fun getChildren(): List<Expr> = listOf(expr)
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "not $expr"
    }
}

class BinaryIn(left: Notation, right: Notation) : BinaryExpr(left, right), Relation {
    override fun check(): Boolean {
        // segment AB in segment AB
        if (left == right)
            return true
        if (left is PointNotation && right is Point2Notation) {
            // A in ray AB
            if ((right.p1 == left.p || right.p2 == left.p))
                return true
            // A == C; C in AB
            val pointEntity = symbolTable.getPoint(left)
            if (symbolTable.getPoint(right.p1) == pointEntity || symbolTable.getPoint(right.p2) == pointEntity)
                return true
        }
        return false
    }

    override fun toString(): String {
        return "$left in $right"
    }
}

class BinaryIntersects(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left ∩ $right"
    }

    override fun check(): Boolean {
        TODO("Not yet implemented")
    }
}

class BinaryParallel(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left || $right"
    }

    override fun check(): Boolean {
        TODO("Not yet implemented")
    }
}

class BinaryPerpendicular(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left ⊥ $right"
    }

    override fun check(): Boolean {
        TODO("Not yet implemented")
    }
}

class BinaryEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left == $right"
    }

    override fun check(): Boolean {
        TODO("Not yet implemented")
    }
}

class BinaryNotEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left != $right"
    }

    override fun check(): Boolean {
        TODO("Not yet implemented")
    }
}

class BinaryGreater(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left > $right"
    }

    override fun check(): Boolean {
        TODO("Not yet implemented")
    }
}

class BinaryGEQ(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left >= $right"
    }

    override fun check(): Boolean {
        TODO("Not yet implemented")
    }
}

class ArithmeticBinaryExpr(left: Expr, right: Expr, private val op: (Float, Float) -> Float) : BinaryExpr(left, right) {
    override fun flatten(): MutableMap<Any, Float> = left.flatten().mergeWithOperation(right.flatten(), op)

    override fun toString(): String {
        return "$left${lambdaToSign[op]}$right"
    }

    override fun check(): Boolean {
        TODO("Not yet implemented")
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
    private val thDefStart by literalToken("th")
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
    private val identToken by regexToken("[a-zA-Z]+[\\w_]*")
    private val lineBreak by regexToken("(\\n[\\t ]*)+")
    private val optionalLineBreak by regexToken("(\\n[\\t ]*)*")

    private val relatableNotation by (line map {
        val res = Point2Notation(it.first, it.second)
        symbolTable.getLine(res)
        res
    }) or (point map {
        PointNotation(it.text)
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

    private val term by notation or (-lpar and parser(::arithmeticExpression) and -rpar) map { it }

    private val divMulChain: Parser<Expr> by leftAssociative(term, div or mul) { a, op, b ->
        ArithmeticBinaryExpr(
            a,
            b,
            signToLambda[op.text]!!
        )
    }

    private val arithmeticExpression: Parser<Expr> by leftAssociative(
        divMulChain,
        plus or minus
    ) { a, op, b -> ArithmeticBinaryExpr(a, b, signToLambda[op.text]!!) }

    private val comparison by arithmeticExpression and compToken and arithmeticExpression map {
        when (it.t2.text) {
            "==" -> BinaryEquals(it.t1, it.t3)
            "!=" -> BinaryNotEquals(it.t1, it.t3)
            ">" -> BinaryGreater(it.t1, it.t3)
            "<" -> BinaryGreater(it.t3, it.t1)
            ">=" -> BinaryGEQ(it.t1, it.t3)
            "<=" -> BinaryGEQ(it.t3, it.t1)
            else -> throw Exception("Unexpected comparison")
        }
    }

    private val relation: Parser<Expr> by relatableNotation and relationToken and relatableNotation or
        (negationToken and parser(::relation)) map {
        if (it is Tuple3<*, *, *>) {
            getRelationByString((it as Tuple3<Notation, TokenMatch, Notation>))
        } else {
            (it as Tuple2<*, *>)
            PrefixNot(it.t2 as Expr)
        }
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

    private val returnStatement by -returnToken and args map { it }
    private val thStatement by theoremUsage or relation or invocation or comparison
    private val thBlock by -zeroOrMore(lineBreak) and (separatedTerms(
        thStatement, lineBreak,
    ) and optional(-lineBreak and returnStatement) or returnStatement) and
        -zeroOrMore(lineBreak) map {
        // only return statement
        if (it is ArrayList<*>)
            TheoremBody(listOf<Expr>(), it as List<Expr>)
        else {
            (it as Tuple2<*, *>)
            TheoremBody(it.t1 as MutableList<Expr>, it.t2 as List<Expr>?)
        }
    }

    private val thDef by -thDefStart and zeroArgsOrMoreInvocation and -colon and thBlock map {
        Pair(it.t1, it.t2)
    }
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

    val th = """th name():
        D in A
        not D in A
        check(W==W)
        
        th defa(D > 3* 2+4/T, F in E, D ): // re fewfw
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
    val theorems = addTheorems()
}