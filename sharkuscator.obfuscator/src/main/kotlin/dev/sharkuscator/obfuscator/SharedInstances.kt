package dev.sharkuscator.obfuscator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.sharkuscator.obfuscator.assembler.ClassRemapper
import meteordevelopment.orbit.EventBus
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.mapleir.context.IRCache
import org.mapleir.ir.cfg.builder.ControlFlowGraphBuilder

object SharedInstances {
    val logger: Logger = LogManager.getLogger("(Sharkuscator)")
    val gson: Gson = GsonBuilder().create()

    val irFactory = IRCache(ControlFlowGraphBuilder::build)
    val eventBus = EventBus()

    val classRemapper = ClassRemapper()
}
