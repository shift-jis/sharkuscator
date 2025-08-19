package dev.sharkuscator.obfuscator.utilities

import org.clyze.jphantom.Driver
import org.clyze.jphantom.Options
import org.clyze.jphantom.adapters.PhantomAdder

object LoggerConfigurator {
    private val JPHANTOM_CLASSES_WITH_LOGGERS = listOf(Options::class.java, Driver::class.java, PhantomAdder::class.java)
//    private val MAPLEIR_CLASSES_WITH_LOGGERS = listOf(ClassTree::class.java)

    fun disableExternalLogging() {
        JPHANTOM_CLASSES_WITH_LOGGERS.forEach { declaration ->
            try {
                val loggerField = declaration.getDeclaredField("logger")
                loggerField.isAccessible = true
                val logger = loggerField.get(null) as ch.qos.logback.classic.Logger
                logger.level = ch.qos.logback.classic.Level.OFF
            } catch (_: NoSuchFieldException) {
            }
        }

//        MAPLEIR_CLASSES_WITH_LOGGERS.forEach { declaration ->
//            try {
//                val loggerField = declaration.getDeclaredField("LOGGER")
//                loggerField.isAccessible = true
//                val logger = loggerField.get(null) as org.apache.log4j.Logger
//                logger.level = org.apache.log4j.Level.OFF
//            } catch (exception: NoSuchFieldException) {
//                exception.printStackTrace()
//            }
//        }
    }
}
