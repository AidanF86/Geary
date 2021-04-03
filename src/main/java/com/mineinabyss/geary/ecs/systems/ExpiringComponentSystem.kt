package com.mineinabyss.geary.ecs.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.components.Expiry

public object ExpiringComponentSystem : TickingSystem() {
    private val expiry by relation<Expiry>()

    override fun GearyEntity.tick() {
        //TODO implement once traits are in
        if (expiry.data.timeOver()) {
            remove(expiry.component.id)
            remove(expiry.relation.id)
        }
    }
}
