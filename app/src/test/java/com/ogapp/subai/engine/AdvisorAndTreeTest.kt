package com.ogapp.subai.engine

import com.ogapp.subai.domain.engine.ElectricalAdvisor
import com.ogapp.subai.domain.engine.Troubleshooter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvisorAndTreeTest {

    @Test fun low_power_does_not_force_big3() {
        val a = ElectricalAdvisor.advise(500)
        assertFalse(a.needsBig3)
        assertFalse(a.considerSecondBattery)
    }

    @Test fun mid_power_needs_big3_only() {
        val a = ElectricalAdvisor.advise(1000)
        assertTrue(a.needsBig3)
        assertFalse(a.considerSecondBattery)
    }

    @Test fun high_power_needs_everything() {
        val a = ElectricalAdvisor.advise(2500)
        assertTrue(a.needsBig3)
        assertTrue(a.considerSecondBattery)
        assertTrue(a.considerHighOutputAlt)
    }

    @Test fun troubleshooting_tree_is_well_formed() {
        val problems = Troubleshooter.validateGraph()
        assertTrue("Tree graph problems: $problems", problems.isEmpty())
    }

    @Test fun troubleshooting_start_exists_and_branches() {
        val start = Troubleshooter.step(Troubleshooter.START)
        assertEquals("start", start.id)
        assertTrue(start.yesNext != null && start.noNext != null)
    }

    @Test fun every_path_reaches_a_diagnosis() {
        // Walk both branches from every node; all terminals must carry a diagnosis.
        for (s in Troubleshooter.allSteps()) {
            if (s.yesNext == null && s.noNext == null) {
                assertTrue(
                    "Terminal ${s.id} must have a diagnosis",
                    !s.diagnosisIfTerminal.isNullOrBlank()
                )
            }
        }
    }
}
