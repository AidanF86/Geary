package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.components.Expiry
import com.mineinabyss.geary.ecs.engine.*
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
internal class SystemManagerTest {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    @Nested
    inner class FamilyMatchingTest {
        val entity = Engine.entity {
            set("Test")
            add<Int>()
        }
        val entity2 = Engine.entity {
            set("Test")
            set(1)
        }

        val system = object : TickingSystem() {
            val string by get<String>()
            val int = has<Int>()

            override fun GearyEntity.tick() {
                string shouldBe get<String>()
                entity.has<Int>() shouldBe true
            }
        }
        val stringId = componentId<String>() or HOLDS_DATA
        val intId = componentId<Int>()

        val correctArchetype = root + stringId + intId

        init {
            SystemManager.registerSystem(system)
        }

        @Test
        fun `family type is correct`() {
            system.family.match.getArchetype() shouldBe correctArchetype
        }

        @Test
        fun `archetypes have been matched correctly`() {
            system.matchedArchetypes shouldContain correctArchetype
        }

        @Test
        fun `get entities matching family`() {
            SystemManager.getEntitiesMatching(system.family).apply {
                shouldContain(entity)
                shouldNotContain(entity2)
            }
        }

        @Test
        fun `accessors in system correctly read data`() {
            system.tick()
        }
    }

    @Nested
    inner class ConcurrentModificationTests {
        var ran = 0

        val removingSystem = object : TickingSystem() {
            val string by get<String>()

            override fun GearyEntity.tick() {
                remove<String>()
                ran++
            }
        }

        init {
            SystemManager.registerSystem(removingSystem)
        }

        @Test
        fun `concurrent modification`() {
            val entities = (0 until 10).map { Engine.entity { set("Test") } }
            val total =
                SystemManager.getEntitiesMatching(Family(sortedSetOf(componentId<String>() or HOLDS_DATA))).count()
            removingSystem.tick()
            ran shouldBe total
            entities.map { it.getComponents() } shouldBe entities.map { setOf() }
        }
    }
    @Test
    fun traits() {
        var ran = 0
        val system = object : TickingSystem() {
            val expiry by relation<Expiry>()
            override fun GearyEntity.tick() {
                ran++
                expiry.relation.id and RELATION_PARENT_MASK shouldBe family.relations.first()
                get(expiry.component.id) shouldNotBe null
                (expiry.data is Expiry) shouldBe true
            }
        }
        system.family.relations shouldBe sortedSetOf(Relation(componentId<Expiry>(), 0uL))
        SystemManager.registerSystem(system)
        val entity = Engine.entity {
            setRelation<Expiry, String>(Expiry(3000))
            add<String>()
        }
        val entity2 = Engine.entity {
            setRelation<Expiry, Int>(Expiry(3000))
            add<Int>()
        }
        val entity3 = Engine.entity {
            setRelation<String, Expiry>("")
            add<Expiry>()
        }
        system.matchedArchetypes.shouldContainAll(entity.type.getArchetype(), entity2.type.getArchetype())
        system.matchedArchetypes.shouldNotContain(entity3.type.getArchetype())

        system.tick()
        ran shouldBe 2

    }
}
