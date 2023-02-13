package pipeline.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple2
import com.github.h0tk3y.betterParse.utils.Tuple3
import entity.expr.*
import entity.expr.binary_expr.*
import entity.expr.notation.*
import error.PosError
import error.SpoofError
import math.ArithmeticExpr
import math.Fraction
import math.FractionFactory
import pipeline.ArithmeticExpander.createArithmeticMap
import pipeline.ArithmeticExpander.mergeMapToDivNotation
import pipeline.ArithmeticExpander.simplifyTwoMaps
import pipeline.inference.DoubleSidedInference
import pipeline.inference.Inference
import pipeline.interpreter.Signature
import pipeline.interpreter.TheoremBody
import pipeline.parser.GrammarHelperMethods.getBinaryRelationByString
import pipeline.parser.GrammarHelperMethods.getReturnableEquals
import utils.ExtensionUtils.toRange
import utils.Utils.keyForArithmeticNumeric

@Suppress("UNCHECKED_CAST", "UNUSED")
object GeomGrammar : Grammar<Any>() {
    // region tokens
    private val semicolonToken by literalToken(";")
    private val colon by literalToken(":")
    private val comma by literalToken(",")
    private val inferToken by literalToken("=>")
    private val anyToken by literalToken("any")
    private val ofToken by literalToken("of")
    private val negationToken by literalToken("not")
    private val newToken by literalToken("new")
    private val thDefStart by literalToken("th")
    private val returnToken by literalToken("return")
    private val comment by regexToken("//.*(\\n[\\t ]*)*", ignore = true)
    private val multilineComment by regexToken("/\\*[.\n]*\\*/", ignore = true)

    // region entity tokens
    private val ray by literalToken("ray")
    private val segment by literalToken("segment")
    private val lineToken by literalToken("line")
    private val arc by literalToken("arc")
    private val number by regexToken("((\\d+\\.)?\\d*)|(\\d*)?\\.\\d+")
    private val angle by 3 times parser(this::point)
    private val linear by 2 times parser(this::point) map { Pair(it[0].text, it[1].text) }
    private val line by -lineToken and linear
    private val point by regexToken("[A-Z][0-9]*")
    //endregion

    //region relation tokens
    private val intersectsToken by literalToken("intersects")
    private val perpendicularToken by literalToken("perpendicular")
    private val parallelToken by literalToken("parallel")
    private val inToken by literalToken("in")
    private val shortIntersectsToken by literalToken("∩")
    private val shortPerpendicularToken by literalToken("⊥")
    private val shortParallelToken by literalToken("||")
    private val relationToken by intersectsToken or shortIntersectsToken or inToken or
            perpendicularToken or shortPerpendicularToken or parallelToken or shortParallelToken
    //endregion

    //region comparison tokens
    private val iffToken by literalToken("<=>")
    private val sameToken by literalToken("===")
    private val eqToken by literalToken("==")
    private val neqToken by literalToken("!=")
    private val geq by literalToken(">=")
    private val leq by literalToken("<=")
    private val gt by literalToken(">")
    private val lt by literalToken("<")
    private val compToken by geq or leq or gt or lt or sameToken or eqToken or neqToken
    // endregion

    private val assignmentToken by literalToken("=")

    private val mul by literalToken("*")
    private val div by literalToken("/")
    private val minus by literalToken("-")
    private val plus by literalToken("+")

    private val leftPar by literalToken("(")
    private val rightPar by literalToken(")")
    private val ws by regexToken("[\\t ]+", ignore = true)
    private val ident by regexToken("[a-zA-Z]+[\\w_]*")
    private val lineBreak by regexToken("(\\n[\\t ]*)+")
    private val repeatedSeparator by lineBreak or comment or semicolonToken
    private val statementSeparator by oneOrMore(repeatedSeparator)
    // endregion

    //region statements
    private val creation: Parser<Expr> by -newToken and (point or ident) map {
        if (it.text[0] in 'A'..'Z')
            PointCreation(it.text)
        else CircleCreation(it.text)
    }

    // angle, segment, arc
    private val comparableNotation: Parser<Notation> by (angle map {
        Point3Notation(it[0].text, it[1].text, it[2].text)
    }) or (linear map {
        SegmentNotation(it.first, it.second)
    }) or (-arc and linear and -ofToken and ident map {
        ArcNotation(it.t1.first, it.t1.second, it.t2.text)
    }) or (number map {
        NumNotation(FractionFactory.fromInt(it.text.toIntOrNull() ?: throw Exception("Not a number")))
    })

    // relations, creations, comparisons are for notations
    private val notation: Parser<Notation> by (line map {
        Point2Notation(it.first, it.second)
    }) or (-ray and linear map {
        RayNotation(it.first, it.second)
    }) or (comparableNotation map {
        it
    }) or (point map {
        PointNotation(it.text)
    }) or (ident map { IdentNotation(it.text) })

    private val arithmeticTerm: Parser<MutableMap<Notation, Fraction>> by (number and comparableNotation map {
        // for terms like 2AB, omitting * operator
        if (it.t2 is NumNotation)
            throw SpoofError("Unexpected 2 numbers in a row")
        mutableMapOf(it.t2 to FractionFactory.fromInt(it.t1.text.toInt()))
    }) or (notation map {
        if (it is NumNotation)
            mutableMapOf((keyForArithmeticNumeric as Notation) to it.number)
        else
            mutableMapOf(it to FractionFactory.one())
    }) or (-leftPar and parser(GeomGrammar::arithmeticExpression) and -rightPar map { it })

    private val divMulChain: Parser<MutableMap<Notation, Fraction>> by leftAssociative(
        arithmeticTerm, div or mul
    ) { a, op, b -> createArithmeticMap(a, b, op.text) }

    private val arithmeticExpression: Parser<MutableMap<Notation, Fraction>> by leftAssociative(
        divMulChain, plus or minus
    ) { a, op, b -> createArithmeticMap(a, b, op.text) }


    private val comparison by (notation and compToken and parser(this::relation) map {
        getReturnableEquals(it.t3, it.t1, it.t2.text)
    }) or (parser(this::relation) and compToken and notation map {
        getReturnableEquals(it.t1, it.t3, it.t2.text)
    }) or (arithmeticExpression and compToken and arithmeticExpression map {
        val divLeft = mergeMapToDivNotation(it.t1)
        val divRight = mergeMapToDivNotation(it.t3)
        val leftMap = createArithmeticMap(divLeft.numerator, divRight.denominator, "*")
        val rightMap = createArithmeticMap(divRight.numerator, divLeft.denominator, "*")
        simplifyTwoMaps(leftMap, rightMap)
        val left = ArithmeticExpr(leftMap)
        val right = ArithmeticExpr(rightMap)
        when (it.t2.text) {
            "===" -> BinarySame(left, right)
            "==" -> BinaryEquals(left, right)
            "!=" -> BinaryNotEquals(left, right)
            ">" -> BinaryGreater(left, right)
            "<" -> BinaryGreater(left, right)
            ">=" -> BinaryGEQ(left, right)
            "<=" -> BinaryGEQ(left, right)
            else -> throw Exception("Unexpected comparison")
        }
    })

    private val relation: Parser<Expr> by notation and relationToken and notation or
            (negationToken and parser(GeomGrammar::relation)) map {
        if (it is Tuple3<*, *, *>) {
            getBinaryRelationByString((it as Tuple3<Notation, TokenMatch, Notation>))
        } else {
            (it as Tuple2<*, *>)
            PrefixNot(it.t2 as Expr)
        }
    }

    private val assignment by notation and -assignmentToken and relation map { BinaryAssignment(it.t1, it.t2) }

    private val binaryStatement by creation or assignment or comparison or relation map { it }

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
        if (it is Signature) Invocation(
            it,
            emptyList()
        ) else {
            if ((it as Tuple2<Any, Any>).t2 is TokenMatch)
                Invocation(it.t1 as Signature, emptyList())
            else {
                it as Tuple2<Signature, List<Expr>>
                Invocation(it.t1, it.t2)
            }
        }
    }
    //endregion

    private val blockContent by separatedTerms(
        theoremUsage or /*pipeline.inference or*/ binaryStatement, statementSeparator
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
                throw PosError(
                    it.t2.toRange(),
                    "any expressions are not allowed at the right side of the pipeline.inference"
                )
            Inference(it.t1, it.t3)
        } else
            DoubleSidedInference(it.t1, it.t3)
    }

    override val rootParser: Parser<Any> by
    // theorem parser
    -zeroOrMore(statementSeparator) and oneOrMore(thDef) or
            // solution parser
            (-zeroOrMore(statementSeparator) and (3 times block map { it })) or
            // pipeline.inference parser
            (-optional(statementSeparator) and separatedTerms(
                inferenceStatement,
                statementSeparator
            ) and -optional(statementSeparator))
}
