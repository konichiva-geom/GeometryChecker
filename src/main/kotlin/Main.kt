import pipeline.Pipeline

// TODO check wolfram alpha paid how can he check geom, geogebra

fun main() {
    val a = """description:
        new A
        new B
        AB || CD
        equal_sided_triangles_i(CD == D1C1, EC == C1E1, ECD == D1C1E1) => *
        //R==2*(3*4+(42-R))+A
       // D==3, R==2*(3*4+(42-R))+A => F==3
       A in AB
       check(A in AB)
       // tUse(T in AC) => *
        //tUse() => T in B
        //fdfds4
        //
        new D
        D in AB
        prove:
        D in AB
        
        // middle comment
        
        
        
      //  R + 42 > 4
        //RD || CD
       // tUse(T in AB) => *
       // D==3, R==4+42 => F==3
         solution:   
        
        
      //  D==3, R==4+42 => F==3
       // arc AB of omega < 3
       // tUse() => TB in BA, B == C
        """

    val th = """th name(): /**/
        check(W==W)
        
        th defa(D > 3* 2+4/T, F in EB, D ): // re fewfw
        check(W==W) //
         
        th defa(D > 3* 2+4/T, F in ED, D ): // re fewfw
        return A
        
        
        
    //"""
    val pipeline = Pipeline()
    pipeline.addInferenceFromFile("examples/inference.txt")
    pipeline.parse(th)
    pipeline.addTheoremsFromFile().parse(a).interpret()
}
