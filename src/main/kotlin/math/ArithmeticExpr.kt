package math

import entity.Renamable
import entity.expr.Expr
import entity.expr.notation.Notation
import error.SpoofError
import pipeline.ArithmeticExpander.getArithmeticToString
import pipeline.symbol_table.SymbolTable
import pipeline.interpreter.IdentMapperInterface
import utils.ExtensionUtils.addOrCreate

class ArithmeticExpr(val map: MutableMap<Notation, Double>) : Expr, Renamable {
    override fun getChildren(): List<Expr> = map.keys.toList()

    override fun getRepr(): StringBuilder {
        return getArithmeticToString(map) { n -> n.getRepr() }
    }

    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr {
        val res = mutableMapOf<Notation, Double>()

        for ((notation, fraction) in map)
            res[notation.createNewWithMappedPointsAndCircles(mapper) as Notation] = fraction

        return ArithmeticExpr(res)
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        val renamedList = map.entries.map {
            val renamedKey = it.key
            renamedKey.renameToMinimalAndRemap(symbolTable)
            renamedKey to it.value
        }
        val resultingMap = mutableMapOf<Notation, Double>()
        for ((k, v) in renamedList) {
            resultingMap.addOrCreate(k, v)
        }
        map.clear()
        map.putAll(resultingMap)
    }

    override fun checkValidityAfterRename(): Exception? {
        map.keys.forEach {
            val exception = it.checkValidityAfterRename()
            if (exception != null)
                return exception
        }
        return null
    }

    override fun toString(): String {
        return getArithmeticToString(map).toString()
    }
}