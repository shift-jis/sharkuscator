package dev.sharkuscator.commons

import kotlin.random.Random

object OperandFactory {
    fun generateOperandsForAnd(value: Number): Pair<Number, Number> {
        return when (value) {
            is Int, is Byte, is Short -> {
                val maskAmount = Random.Default.nextInt()
                val operandA = (Random.Default.nextInt() and maskAmount) or value.toInt()
                val operandB = (Random.Default.nextInt() and maskAmount.inv()) or value.toInt()
                operandA to operandB
            }

            is Long -> {
                val maskAmount = Random.Default.nextLong()
                val operandA = (Random.Default.nextLong() and maskAmount) or value
                val operandB = (Random.Default.nextLong() and maskAmount.inv()) or value
                operandA to operandB
            }

            else -> throw IllegalStateException("generateOperandsForAnd cannot handle type ${value::class.simpleName} (value: $value)")
        }
    }

    fun generateOperandsForShift(value: Number): Pair<Number, Number> {
        return when (value) {
            is Int, is Byte, is Short -> {
                val shiftAmount = Random.Default.nextInt()
                (value.toInt() shl shiftAmount) to shiftAmount
            }

            is Long -> {
                val shiftAmount = Random.Default.nextInt()
                (value shl shiftAmount) to shiftAmount
            }

            else -> throw IllegalStateException("generateOperandsForAnd cannot handle type ${value::class.simpleName} (value: $value)")
        }
    }
}