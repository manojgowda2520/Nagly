package com.manojbuilds.nagly.notifications

import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.model.Mood
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NudgeActionsTest {

    @Test
    fun buildNudgeActions_usesPersonaSkipLabels() {
        val persona = PersonaCatalog.get("indian_mom")
        val actions = buildNudgeActions(persona, Mood.WORRIED)
        assertTrue(actions.skipLabel in persona.skipLabels.getValue(Mood.WORRIED))
        assertEquals(Mood.WORRIED, actions.mood)
        assertEquals("WATER_NUDGE_WORRIED", actions.iosCategoryId)
    }

    @Test
    fun amountForAction_andSkip() {
        assertEquals(250, amountForAction(NudgeActionIds.ADD_250))
        assertEquals(500, amountForAction(NudgeActionIds.ADD_500))
        assertEquals(null, amountForAction(NudgeActionIds.SKIP))
        assertTrue(isSkipAction(NudgeActionIds.SKIP))
        assertFalse(isSkipAction(NudgeActionIds.ADD_250))
    }

    @Test
    fun iosSkipTitle_fixedPerMood() {
        assertEquals("Later", iosSkipTitle(Mood.NEUTRAL))
        assertEquals("Busy", iosSkipTitle(Mood.WORRIED))
        assertEquals("Skip", iosSkipTitle(Mood.DISAPPOINTED))
        assertEquals("Nice", iosSkipTitle(Mood.PROUD))
    }
}
