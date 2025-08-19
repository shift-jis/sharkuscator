package dev.sharkuscator.commons.extensions

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <T> T.requireSizeOrElse(minSize: Int, defaultValue: () -> T): T where T : Collection<*> {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }
    return if (size < minSize) defaultValue() else this
}
