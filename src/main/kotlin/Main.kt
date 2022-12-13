import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.LiteralToken
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.AlternativesFailure
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.st.LiftToSyntaxTreeOptions
import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.st.liftToSyntaxTreeGrammar
import com.github.h0tk3y.betterParse.utils.Tuple2

// TODO check wolfram alpha paid how can he check geom, geogebra

// class InRelation(left: RelatableNotation, right: RelatableNotation, isNot: Boolean = false) :
//     Relation(left, right, isNot)
//class Relation : Term // A in CD, AC intersects DB, new A

class Procedure

val symbolTable = SymbolTable()

fun parseTree(tree: SyntaxTree<Any>) {
}

fun parseProgram(tree: SyntaxTree<Any>) {
    checkHeaders(
        tree.item as List<Tuple2<String, *>>,
        TokenMatch(LiteralToken("", ""), 0, "", 0, tree.range.count(), 0, 0)
    )
    parseDescription(tree.children[0])
    parseGoal(tree.children[1])
    parseSolution(tree.children[2])
}

fun parseDescription(syntaxTree: SyntaxTree<*>) {
}

fun parseGoal(syntaxTree: SyntaxTree<*>) {
    for (relation in (syntaxTree.item as Tuple2<*, List<*>>).t2) {

    }
}

fun parseSolution(syntaxTree: SyntaxTree<*>) {}

fun checkHeaders(blocks: List<Tuple2<String, *>>, allMatch: TokenMatch) {
    if (blocks[0].t1 != "description"
        || blocks[1].t1 != "prove"
        || blocks[2].t1 != "solution"
    )
        throw SpoofError(
            "Expected structure: %{expected}got: %{got}",
            "expected" to "\ndescription:\n\t...\nprove:\n\t...\nsolution:\n\t...\n",
            "got" to "\n${blocks[0].t1}:\n\t...\n${blocks[1].t1}:\n\t...\n${blocks[2].t1}:\n\t..."
        )
}

open class A(vararg val args: Any)

class B(val smth: String, vararg args: Any) : A(*args)

fun main() {
    val b = B(smth = "smth")

    val a = """description:
        equal_sided_triangles_i(C1D == D1C1, EC == C1E1, ECD == D1C1E1) => *
        //R==2*(3*4+(42-R))+A
       // D==3, R==2*(3*4+(42-R))+A => F==3
        tUse(T in A) => *
        //tUse() => T in B
        //fdfds4
        //
        prove:
        D == 3
        R + 42 > 4
        //RD || CD
        R in S
        tUse(T in A) => *
       // D==3, R==4+42 => F==3
         solution  :   
        
        
      //  D==3, R==4+42 => F==3
        arc AB < 3
        tUse() => T in A, B == C
        """

    val th = """th name(): /**/
        D in A
        not D in A
        check(W==W)
        
        th defa(D > 3* 2+4/T, F in E, D ): // re fewfw
        D in A
        circe in A
        check(W==W) //
         
        th defa(D > 3* 2+4/T, F in E, D ): // re fewfw
        return A
    //"""
    // val test = GeomGrammar.parseToEnd("tUse(T == A) => *")
    try {
        val res = GeomGrammar.liftToSyntaxTreeGrammar(LiftToSyntaxTreeOptions(retainSeparators = false)).parseToEnd(a)
        parseProgram(res)
    } catch (e: ParseException) {
        val tokens = getAllErrorTokens(e.errorResult as AlternativesFailure)
        chooseFurthestUnexpectedToken(tokens)
        findProblemToken(e.errorResult as AlternativesFailure)
    }
    val sig = GeomGrammar.parseToEnd(a)
    val result = GeomGrammar.parseToEnd(th)
    val t = GeomGrammar.liftToSyntaxTreeGrammar().parseToEnd(th)
    val tp = TheoremParser()
    val sg = ((sig as List<Tuple2<String, List<Any>>>).first().t2.first() as Tuple2<Signature, Any>).t1
    val theorems = tp.addTheorems()
    val body = tp.getTheoremBodyBySignature(sg)
    tp.parseTheorem(sg, tp.getSignature(sg), body) // TODO get theorem signature from call signature
}