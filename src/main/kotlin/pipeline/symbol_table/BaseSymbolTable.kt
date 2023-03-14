package pipeline.symbol_table

import pipeline.EqualIdentRenamer
import utils.NameGenerator

open class BaseSymbolTable {
    val equalIdentRenamer = EqualIdentRenamer()
    val nameGenerator = NameGenerator()
}