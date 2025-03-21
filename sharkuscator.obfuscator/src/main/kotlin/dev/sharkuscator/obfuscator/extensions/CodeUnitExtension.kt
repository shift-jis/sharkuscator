package dev.sharkuscator.obfuscator.extensions

import org.mapleir.ir.code.CodeUnit
import org.mapleir.ir.code.Expr

fun CodeUnit.overwrite(previous: Expr, newest: Expr) {
    val exprIndex = indexOf(previous)
    writeAt(newest, exprIndex)
    if (children[exprIndex] == newest) {
        previous.unlink()
    }
}
