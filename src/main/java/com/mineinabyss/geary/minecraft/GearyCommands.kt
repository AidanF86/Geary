package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.engine.EngineImpl
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.plugin.getService

@ExperimentalCommandDSL
object GearyCommands: IdofrontCommandExecutor() {
    override val commands = commands(geary) {
        "geary" {
            "components"{
                val type by stringArg()
                action {
                    (getService<Engine>() as EngineImpl).bitsets.forEach { (t, u) ->
                        if (t.simpleName == type) {
                            var sum = 0
                            u.forEachBit { sum++ }
                            sender.info("$sum entities with that component")
                        }
                    }
                }
            }
        }
    }
}
