package pipeline

import Signature
import TheoremBody
import Utils
import Utils.signToLambda
import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.oneOrMore
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple2
import com.github.h0tk3y.betterParse.utils.Tuple3
import expr.ArcNotation
import expr.ArithmeticBinaryExpr
import expr.BinaryEquals
import expr.BinaryGEQ
import expr.BinaryGreater
import expr.BinaryNotEquals
import expr.CircleCreation
import expr.Expr
import expr.IdentNotation
import expr.Notation
import expr.NumNotation
import expr.Point2Notation
import expr.Point3Notation
import expr.PointCreation
import expr.PointNotation
import expr.PrefixNot
import expr.RayNotation
import expr.SegmentNotation
import expr.TheoremUse

object GeomGrammar : Grammar<Any>() {
    // region entity prefix tokens
    private val ray by literalToken("ray")
    private val segment by literalToken("segment")
    private val arc by literalToken("arc")
    //endregion

    private val anyToken by literalToken("any")
    private val ofToken by literalToken("of")
    private val negationToken by literalToken("not")
    private val newToken by literalToken("new")
    private val comment by regexToken("//.*(\\n[\\t ]*)*", ignore = true)
    private val multilineComment by regexToken("/\\*[.\n]*\\*/", ignore = true)
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
    private val parallelToken by literalToken("parallel")
    private val shortParallelToken by literalToken("||")
    private val inToken by literalToken("in")
    private val relationToken by intersectsToken or shortIntersectsToken or inToken or
            perpendicularToken or shortPerpendicularToken or parallelToken or shortParallelToken
    //endregion

    //region comparison tokens
    private val iffToken by literalToken("<=>")
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
    private val leftPar by literalToken("(")
    private val rightPar by literalToken(")")
    private val ws by regexToken("[\\t ]+", ignore = true)
    private val ident by regexToken("[a-zA-Z]+[\\w_]*")
    private val lineBreak by regexToken("(\\n[\\t ]*)+")
    private val statementSeparator by lineBreak or comment

    //region creation tokens
    private val creation by -newToken and (point or ident) map {
        if (it.text[0] in 'A'..'Z')
            PointCreation(it.text) as Expr
        else CircleCreation(it.text) as Expr
    }
    //endregion

    private val relatableNotation by (line map {
        val res = Point2Notation(it.first, it.second)
        res
    }) or (point map { PointNotation(it.text) }) or
            (-arc and line and -ofToken and ident map { ArcNotation(it.t1.first, it.t1.second, it.t2.text) }) or
            (ident map { IdentNotation(it.text) })

    // segment AB, A, ray DF
    private val notation by (-segment and line map { SegmentNotation(it.first, it.second) }) or
            (-ray and line map { RayNotation(it.first, it.second) }) or (angle map {
        Point3Notation(it[0].text, it[1].text, it[2].text)
    }) or relatableNotation or (number map {
        NumNotation(it.text.toIntOrNull() ?: it.text.toFloatOrNull() ?: throw Exception("Not a number"))
    })

    private val term by notation or (-leftPar and parser(GeomGrammar::arithmeticExpression) and -rightPar) map { it }

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

    private val relation: Parser<Expr> by notation and relationToken and notation or
            (negationToken and parser(GeomGrammar::relation)) map {
        if (it is Tuple3<*, *, *>) {
            Utils.getBinaryRelationByString((it as Tuple3<Notation, TokenMatch, Notation>))
        } else {
            (it as Tuple2<*, *>)
            PrefixNot(it.t2 as Expr)
        }
    }
    private val binaryStatement by creation or comparison or relation map { it }

    private val args by separatedTerms(binaryStatement or notation, comma)
    private val optionalArgs by optional(args) map { it ?: emptyList() }

    private val invocation by ident and -leftPar and args and -rightPar map { Signature(it.t1.text, it.t2) }
    private val zeroArgsOrMoreInvocation by ident and -leftPar and optionalArgs and -rightPar map {
        Signature(
            it.t1.text,
            it.t2
        )
    }

    private val theoremUsage by (zeroArgsOrMoreInvocation and -inferToken and (mul or args)) or (invocation) map {
        if (it is Signature) TheoremUse(
            it,
            emptyList()
        ) else {
            if ((it as Tuple2<Any, Any>).t2 is TokenMatch)
                TheoremUse(it.t1 as Signature, emptyList())
            else {
                it as Tuple2<Signature, List<Expr>>
                TheoremUse(it.t1, it.t2)
            }
        }
    }

    // private val inference by (comparison and -inferToken and comparison map { it }) or
    //     (comparison and -comma and comparison and -inferToken and comparison map { it })

    private val blockContent by -zeroOrMore(lineBreak) and separatedTerms(
        theoremUsage or /*inference or*/ binaryStatement, statementSeparator
    ) and -zeroOrMore(lineBreak) map { it }
    private val block by ident and -colon and -statementSeparator and blockContent map
            { Tuple2(it.t1.text, it.t2) }

    private val returnStatement by -returnToken and args map { it }
    private val thStatement by theoremUsage or relation or comparison

    //private val seq by
    private val thBlock by -zeroOrMore(lineBreak) and (separatedTerms(
        thStatement, statementSeparator
    ) and optional(-statementSeparator and returnStatement) or returnStatement) and
            -zeroOrMore(lineBreak) map {
        // only return statement
        if (it is ArrayList<*>)
            TheoremBody(emptyList(), it as List<Expr>)
        else {
            (it as Tuple2<*, *>)
            TheoremBody(it.t1 as MutableList<Expr>, (it.t2 ?: emptyList<Expr>()) as List<Expr>)
        }
    }

    private val thDef by -thDefStart and zeroArgsOrMoreInvocation and
            -colon and thBlock map { Pair(it.t1, it.t2) }

    private val inferenceArgs by separatedTerms((anyToken and notation) or binaryStatement or notation, comma)
    private val inferenceStatement by inferenceArgs and (iffToken or inferToken) and inferenceArgs

    override val rootParser: Parser<Any> by oneOrMore(thDef) or (3 times block map { it }) or
            (-zeroOrMore(lineBreak) and separatedTerms(inferenceStatement, statementSeparator) and -zeroOrMore(lineBreak))
}