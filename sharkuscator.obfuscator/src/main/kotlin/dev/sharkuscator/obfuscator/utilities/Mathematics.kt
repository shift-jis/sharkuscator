package dev.sharkuscator.obfuscator.utilities

import kotlin.random.Random

object Mathematics {
    fun generateOperandsForAnd(value: Number): Pair<Number, Number> {
        return when (value) {
            is Int, is Byte, is Short -> {
                val maskAmount = Random.nextInt()
                val operandA = (Random.nextInt() and maskAmount) or value.toInt()
                val operandB = (Random.nextInt() and maskAmount.inv()) or value.toInt()
                operandA to operandB
            }

            is Long -> {
                val maskAmount = Random.nextLong()
                val operandA = (Random.nextLong() and maskAmount) or value
                val operandB = (Random.nextLong() and maskAmount.inv()) or value
                operandA to operandB
            }

            else -> throw IllegalStateException("generateOperandsForAnd cannot handle type ${value::class.simpleName} (value: $value)")
        }
    }

    fun generateOperandsForShift(value: Number): Pair<Number, Number> {
        return when (value) {
            is Int, is Byte, is Short -> {
                val shiftAmount = Random.nextInt()
                (value.toInt() shl shiftAmount) to shiftAmount
            }

            is Long -> {
                val shiftAmount = Random.nextInt()
                (value shl shiftAmount) to shiftAmount
            }

            else -> throw IllegalStateException("generateOperandsForAnd cannot handle type ${value::class.simpleName} (value: $value)")
        }
    }
}
