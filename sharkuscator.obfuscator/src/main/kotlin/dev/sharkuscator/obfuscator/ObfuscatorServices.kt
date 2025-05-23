package dev.sharkuscator.obfuscator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.sharkuscator.obfuscator.assembler.SymbolRemapper
import meteordevelopment.orbit.EventBus
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.mapleir.context.IRCache
import org.mapleir.ir.cfg.builder.ControlFlowGraphBuilder

object ObfuscatorServices {
    val sharkLogger: Logger = LogManager.getLogger("(Sharkuscator)")
    val jsonProcessor: Gson = GsonBuilder().setPrettyPrinting().create()

    val controlFlowGraphCache = IRCache(ControlFlowGraphBuilder::build)
    val mainEventBus = EventBus()

    val symbolRemapper = SymbolRemapper()
}
