import TheoremParser.addTheorems
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.st.liftToSyntaxTreeGrammar

// TODO check wolfram alpha paid how can he check geom, geogebra

// class InRelation(left: RelatableNotation, right: RelatableNotation, isNot: Boolean = false) :
//     Relation(left, right, isNot)
//class Relation : Term // A in CD, AC intersects DB, new A

class Procedure

val symbolTable = SymbolTable()

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