package pipeline.interpreter

import TestFactory.failProve
import TestFactory.failSolution
import TestFactory.failTask
import kotlin.test.Test

internal class InterpreterTest {
    @Test
    fun failTaskStructure() {
        failTask(
            """
            describe:
            
            prove:
            
            solution:
        """, """Expected structure: 
description:
	...
prove:
	...
solution:
	..."""
        )
    }

    @Test
    fun failRelationInSolution() {
        failSolution(
            """
            new A; new B;
              A in AB
        """, "Cannot add relation in solution. Use check to check or theorem to add new relation"
        )
    }

    @Test
    fun failCreationInProve() {
        failProve(
            """
            new A; new B;
        """, "Expected relation to check in prove block"
        )
    }
}
