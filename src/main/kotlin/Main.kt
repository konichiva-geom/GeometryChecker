import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.st.liftToSyntaxTreeGrammar

sealed class BooleanExpression

object TRUE : BooleanExpression()
object FALSE : BooleanExpression()
data class Variable(val name: String) : BooleanExpression()
data class Not(val body: BooleanExpression) : BooleanExpression()
data class And(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
data class Or(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()
data class Impl(val left: BooleanExpression, val right: BooleanExpression) : BooleanExpression()

interface Term
class Relation : Term // A in CD, AC intersects DB, new A
//class Angle(left:Point, middle: Point, right: Point): Term
class Angle
class Line: Term
class Point(val name: String): Term
interface Entity : Term // A, |>ABC, BC, AOB
class Procedure
data class Eq(val left: Entity, val right: Entity)
data class TheoremDefinition(val name: String, val args: List<Term>, val body: Procedure)

val symbolTable = SymbolTable()

object GeomGrammar : Grammar<Any>() {
    val angle by 3 times parser(this::point)
    val line by 2 times parser(this::point)
    val point by regexToken("[A-Z][0-9]*")
    val eq by literalToken("==")
    val entity by (angle map {Angle()}) or (point map {Point(it.text)})
    val relation by regexToken("")// leftAssociative(entity, eq) {a,_,b ->Eq(a, b)}
    val term by relation or entity
    val args by term and zeroOrMore(term and literalToken(","))

    private val theoremParser by regexToken("")
    val theorems = oneOrMore(theoremParser)
    // val structureParser
    //val theoremDefinitionParser by

    override val rootParser by entity
}

object BooleanGrammar : Grammar<BooleanExpression>() {
    val tru by literalToken("true")
    val fal by literalToken("false")
    val id by regexToken("\\w+")
    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val not by literalToken("!")
    val and by literalToken("&")
    val or by literalToken("|")
    val impl by literalToken("->")
    val ws by regexToken("\\s+", ignore = true)

    val negation by -not * parser(this::term) map { Not(it) }
    val bracedExpression by -lpar * parser(this::implChain) * -rpar

    val term: Parser<BooleanExpression> by
    (tru asJust TRUE) or
            (fal asJust FALSE) or
            (id map { Variable(it.text) }) or
            negation or
            bracedExpression

    val andChain by leftAssociative(term, and) { a, _, b -> And(a, b) }
    val orChain by leftAssociative(andChain, or) { a, _, b -> Or(a, b) }
    val implChain by rightAssociative(orChain, impl) { a, _, b -> Impl(a, b) }

    override val rootParser by implChain
}

fun main() {
    val result = GeomGrammar.parseToEnd("ABC")
    println(result)
//    val a = regexToken("a+")
//    val b = regexToken("b+")
//    val tokenMatches = DefaultTokenizer(listOf(a, b)).tokenize("aabbaaa")
//   // val result = a.tryParse(tokenMatches, 0)
//    val id = regexToken("\\w+")
//    val aText = a map { it.text }
//    val exprs = listOf(
//        "a -> b | !c",
//        "a & !b | (a -> a & b) -> a | b | a & b",
//        "a & !(b -> a | c) | (c -> d) & !(!c -> !d & a)"
//    )
}