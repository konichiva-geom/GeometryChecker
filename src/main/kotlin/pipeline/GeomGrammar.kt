package pipeline

import PosError
import Utils
import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.oneOrMore
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple2
import com.github.h0tk3y.betterParse.utils.Tuple3
import expr.AnyExpr
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
import inference.DoubleSidedInference
import inference.Inference
import pipeline.interpreter.Signature
import pipeline.interpreter.TheoremBody
import toRange

object GeomGrammar : Grammar<Any>() {
    // region entity prefix tokens
    private val ray by literalToken("ray")
    private val segment by literalToken("segment")
    private val lineToken by literalToken("line")
    private val arc by literalToken("arc")
    //endregion

    private val semicolonToken by literalToken(";")
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
    private val linear by 2 times parser(this::point) map { Pair(it[0].text, it[1].text) }
    private val line by -lineToken and linear
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
    private val repeatedSeparator by lineBreak or comment or semicolonToken
    private val statementSeparator by oneOrMore(repeatedSeparator)

    //region creation tokens
    private val creation by -newToken and (point or ident) map {
        if (it.text[0] in 'A'..'Z')
            PointCreation(it.text)
        else CircleCreation(it.text)
    }
    //endregion

    private val relatableNotation by (angle map {
        Point3Notation(it[0].text, it[1].text, it[2].text)
    }) or (linear map {
        SegmentNotation(it.first, it.second)
    }) or (-arc and linear and -ofToken and ident map { ArcNotation(it.t1.first, it.t1.second, it.t2.text) })

    // segment AB, A, ray DF
    private val notation by (line map {
        Point2Notation(it.first, it.second)
    }) or
        (-ray and linear map { RayNotation(it.first, it.second) }) or relatableNotation or (number map {
        NumNotation(it.text.toIntOrNull() ?: it.text.toFloatOrNull() ?: throw Exception("Not a number"))
    }) or (point map { PointNotation(it.text) }) or
        (ident map { IdentNotation(it.text) })

    private val arithmeticTerm by (number and relatableNotation map {
        ArithmeticBinaryExpr(
            NumNotation(
                it.t1.text.toIntOrNull() ?: it.t1.text.toFloatOrNull() ?: throw Exception("Not a number")
            ), it.t2, "*"
        )
    }) or notation or (-leftPar and parser(GeomGrammar::arithmeticExpression) and -rightPar) map { it }

    private val divMulChain: Parser<Expr> by leftAssociative(arithmeticTerm, div or mul) { a, op, b ->
        ArithmeticBinaryExpr(a, b, op.text)
    }

    private val arithmeticExpression: Parser<Expr> by leftAssociative(
        divMulChain,
        plus or minus
    ) { a, op, b -> ArithmeticBinaryExpr(a, b, op.text) }

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

    private val invocation by ident and -leftPar and args and -rightPar map {
        Signature(
            it.t1.text,
            it.t2 as List<Expr>
        )
    }
    private val zeroArgsOrMoreInvocation by ident and -leftPar and optionalArgs and -rightPar map {
        Signature(
            it.t1.text,
            it.t2 as List<Expr>
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

    private val blockContent by separatedTerms(
        theoremUsage or /*inference or*/ binaryStatement, statementSeparator
    ) map { it }
    private val block by ident and -colon and -statementSeparator and -optional(statementSeparator) and
            optional(blockContent) and -optional(statementSeparator) map { Tuple2(it.t1.text, it.t2) }

    private val returnStatement by -returnToken and args map { it }
    private val thStatement by theoremUsage or relation or comparison

    private val thBlock by -optional(statementSeparator) and (separatedTerms(
        thStatement, statementSeparator
    ) and optional(-statementSeparator and returnStatement) or returnStatement) and
            -optional(statementSeparator) map {
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

    private val inferenceArgs by separatedTerms(
        (anyToken and notation) or binaryStatement,
        comma
    ) map { list -> list.map { if (it !is Expr) AnyExpr((it as Tuple2<Any, Notation>).t2) else it } }

    private val inferenceStatement by inferenceArgs and (iffToken or inferToken) and inferenceArgs map { it ->
        if (it.t2.text == "=>") {
            if (it.t3.any { it is AnyExpr })
                throw PosError(it.t2.toRange(), "any expressions are not allowed at the right side of the inference")
            Inference(it.t1, it.t3)
        } else
            DoubleSidedInference(it.t1, it.t3)
    }

    override val rootParser: Parser<Any> by
    // theorem parser
    oneOrMore(thDef) or
            // solution parser
            (3 times block map { it }) or
            // inference parser
            (-optional(statementSeparator) and separatedTerms(
                inferenceStatement,
                statementSeparator
            ) and -optional(statementSeparator))
}
