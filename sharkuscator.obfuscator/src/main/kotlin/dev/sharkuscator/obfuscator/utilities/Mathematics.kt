package dev.sharkuscator.obfuscator.utilities

import kotlin.random.Random

object Mathematics {
    fun generateOperandsForAnd(value: Number, mask: Number = Random.nextInt()): Pair<Number, Number> {
        return when (value) {
            is Int, is Byte, is Short -> {
                val operandA = (Random.nextInt() and mask.toInt()) or value.toInt()
                val operandB = (Random.nextInt() and mask.toInt().inv()) or value.toInt()
                operandA to operandB
            }

            is Long -> {
                val operandA = (Random.nextLong() and mask.toLong()) or value
                val operandB = (Random.nextLong() and mask.toLong().inv()) or value
                operandA to operandB
            }

            else -> throw IllegalStateException("generateOperandsForAnd cannot handle type ${value::class.simpleName} (value: $value)")
        }
    }
}
