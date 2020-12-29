package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.CooldownManager
import com.mineinabyss.geary.ecs.components.getOrAddPersisting
import com.mineinabyss.idofront.time.TimeSpan
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("cooldown")
public class CooldownAction(
        private val length: TimeSpan,
        private val run: List<GearyAction>,
        @SerialName("name")
        public val _name: String? = null
) : GearyAction() {
    @Transient
    private val name = _name ?: run.hashCode().toString()

    override fun runOn(entity: GearyEntity): Boolean {
        val cooldowns = entity.getOrAddPersisting { CooldownManager() }

        // restart cooldown if any of the actions ran successfully
        return cooldowns.onCooldownIf(name, length.millis) {
            run.any { it.runOn(entity) }
        }
    }
}
