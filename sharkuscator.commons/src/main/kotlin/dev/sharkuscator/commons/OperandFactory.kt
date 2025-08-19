package dev.sharkuscator.commons

import kotlin.random.Random

object OperandFactory {
    fun generateOperandsForAnd(value: Number): Pair<Number, Number> {
        return when (value) {
            is Int, is Byte, is Short -> {
                val maskAmount = Random.nextInt()
                ((Random.nextInt() and maskAmount) or value.toInt()) to ((Random.nextInt() and maskAmount.inv()) or value.toInt())
            }

            is Long -> {
                val maskAmount = Random.nextLong()
                ((Random.nextLong() and maskAmount) or value) to ((Random.nextLong() and maskAmount.inv()) or value)
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