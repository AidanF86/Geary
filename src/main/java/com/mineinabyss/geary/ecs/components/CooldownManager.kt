package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.SerializableGearyComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A component that manages cooldowns for an entity. Each cooldown has a unique key, but may be used to keep track of
 * anything. Typically a good idea to persist this component.
 *
 * @property completionTime A map of cooldown keys to the value of [System.currentTimeMillis] when they are complete.
 * @property incompleteCooldowns Calculated map of incomplete cooldowns.
 */
//TODO persist cooldowns on entity, but dont serialize any cooldowns that are already complete
@Serializable
@SerialName("geary:cooldowns")
public class CooldownManager : SerializableGearyComponent {
    private val completionTime: MutableMap<String, Long> = mutableMapOf()
    public val incompleteCooldowns: Map<String, Long> get() = completionTime.filterValues { it > System.currentTimeMillis() }

    /** @return Whether a certain cooldown is complete. */
    public fun isDone(key: String): Boolean {
        return (completionTime[key] ?: return true) <= System.currentTimeMillis()
    }

    /**
     * Runs [something][run] if no cooldown is active and starts the cooldown after running it.
     *
     * @param key The key for this cooldown.
     * @param length The length of this cooldown in milliseconds.
     * @param run What to run if this cooldown is complete.
     *
     * @return whether [run] was executed.
     */
    public inline fun onCooldown(key: String, length: Long, run: () -> Unit): Boolean {
        if (isDone(key)) {
            run()
            start(key, length)
            return true
        }
        return false
    }

    /**
     * Runs [something][run] if no cooldown is active and starts the cooldown only if [run] returns true.
     *
     * @param key The key for this cooldown.
     * @param length The length of this cooldown in milliseconds.
     * @param run What to run if this cooldown is complete. Returns whether or not to start the cooldown.
     *
     * @return whether [run] was executed.
     */
    public inline fun onCooldownIf(key: String, length: Long, run: () -> Boolean): Boolean {
        if (isDone(key))
            if (run()) {
                start(key, length)
                return true
            }
        return false
    }

    /**
     * Starts a cooldown of a certain [length] under a [key].
     *
     * @param key The key for this cooldown.
     * @param length The length of this cooldown in milliseconds.
     * */
    public fun start(key: String, length: Long) {
        completionTime[key] = System.currentTimeMillis() + length
    }

    /** Clears the cooldown for a [key]. */
    public fun reset(key: String) {
        completionTime -= key
    }
}
