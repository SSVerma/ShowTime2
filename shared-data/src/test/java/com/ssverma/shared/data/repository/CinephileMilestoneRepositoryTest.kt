package com.ssverma.shared.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssverma.shared.domain.model.stats.CinephileMilestone
import com.ssverma.shared.domain.model.stats.CinephileMilestoneDefinition
import com.ssverma.shared.domain.model.stats.MilestoneActionType
import com.ssverma.shared.domain.model.stats.MilestoneMetricType
import com.ssverma.shared.domain.model.stats.MilestoneTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.InputStreamReader

class CinephileMilestoneRepositoryTest {

    private val gson = Gson()

    @Test
    fun `milestone catalog json parses correctly and validates definitions`() {
        val inputStream =
            javaClass.classLoader?.getResourceAsStream("cinephile_milestones_catalog.json")
                ?: javaClass.getResourceAsStream("/cinephile_milestones_catalog.json")
                ?: java.io.File("src/main/assets/cinephile_milestones_catalog.json").inputStream()

        val type = object : TypeToken<List<CinephileMilestoneDefinition>>() {}.type
        val definitions: List<CinephileMilestoneDefinition> =
            InputStreamReader(inputStream).use { reader ->
                gson.fromJson(reader, type)
            }

        assertNotNull(definitions)
        assertTrue(definitions.size >= 10)

        // Ensure unique IDs
        val ids = definitions.map { it.id }
        assertEquals(ids.size, ids.distinct().size)

        definitions.forEach { def ->
            assertTrue("ID should not be blank: ${def.id}", def.id.isNotBlank())
            assertTrue("Title should not be blank: ${def.title}", def.title.isNotBlank())
            assertTrue(
                "Description should not be blank: ${def.description}",
                def.description.isNotBlank()
            )
            assertTrue("Category should not be blank: ${def.category}", def.category.isNotBlank())
            assertTrue(
                "MaxProgress should be greater than 0: ${def.maxProgress}",
                def.maxProgress > 0
            )
            assertNotNull(def.tier)
            assertNotNull(def.metricType)
            assertNotNull(def.actionType)
            assertTrue(
                "ActionLabel should not be blank: ${def.actionLabel}",
                def.actionLabel.isNotBlank()
            )
        }

        // Verify specific known milestones
        val firstReel = definitions.firstOrNull { it.id == "first_reel" }
        assertNotNull(firstReel)
        assertEquals(MilestoneTier.BRONZE, firstReel?.tier)
        assertEquals(MilestoneMetricType.TOTAL_LOGS, firstReel?.metricType)
        assertEquals(MilestoneActionType.DIARY, firstReel?.actionType)

        val centuryClub = definitions.firstOrNull { it.id == "century_club" }
        assertNotNull(centuryClub)
        assertEquals(MilestoneTier.GOLD, centuryClub?.tier)
        assertEquals(100, centuryClub?.maxProgress)
    }

    @Test
    fun `cinephile milestone progress calculations are accurate`() {
        val lockedMilestone = CinephileMilestone(
            id = "century_club",
            title = "Century Club",
            iconEmoji = "🏆",
            description = "Log 100 films",
            category = "Volume",
            tier = MilestoneTier.GOLD,
            currentProgress = 35,
            maxProgress = 100,
            actionType = MilestoneActionType.DIARY,
            actionLabel = "Log in Cinema Diary"
        )

        assertFalse(lockedMilestone.isUnlocked)
        assertEquals(65, lockedMilestone.remainingProgress)
        assertEquals(0.35f, lockedMilestone.progressPercentage, 0.001f)

        val completedMilestone = lockedMilestone.copy(
            currentProgress = 100,
            isUnlocked = true
        )

        assertTrue(completedMilestone.isUnlocked)
        assertEquals(0, completedMilestone.remainingProgress)
        assertEquals(1.0f, completedMilestone.progressPercentage, 0.001f)

        val overAchievedMilestone = lockedMilestone.copy(
            currentProgress = 120,
            isUnlocked = true
        )

        assertTrue(overAchievedMilestone.isUnlocked)
        assertEquals(0, overAchievedMilestone.remainingProgress)
        assertEquals(1.0f, overAchievedMilestone.progressPercentage, 0.001f)
    }
}
