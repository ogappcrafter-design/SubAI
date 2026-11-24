package com.ogapp.subai.engine

import com.ogapp.subai.domain.engine.PowerWiringCalculator
import com.ogapp.subai.domain.model.AmpClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PowerWiringCalculatorTest {

    @Test fun current_from_rms_class_d_1000w() {
        // 1000 / 0.85 / 13.8 = 85.2 -> 86A
        assertEquals(86, PowerWiringCalculator.currentFromRms(1000, AmpClass.CLASS_D))
    }

    @Test fun current_from_rms_class_ab_higher_than_class_d() {
        val d = PowerWiringCalculator.currentFromRms(1000, AmpClass.CLASS_D)
        val ab = PowerWiringCalculator.currentFromRms(1000, AmpClass.CLASS_AB)
        assertTrue("Class A/B must draw more current than Class D", ab > d)
    }

    @Test fun thousand_watt_class_d_recommends_4awg() {
        val current = PowerWiringCalculator.currentFromRms(1000, AmpClass.CLASS_D) // 86A
        val rec = PowerWiringCalculator.recommend(current, runLengthFt = 17.0, isCca = false)
        assertEquals("4 AWG", rec.gauge)
        assertEquals("4 AWG", rec.groundGauge)
        assertTrue(rec.voltageDropVolts <= 0.5)
    }

    @Test fun small_system_50a_short_run_is_8awg() {
        val rec = PowerWiringCalculator.recommend(50, runLengthFt = 10.0, isCca = false)
        assertEquals("8 AWG", rec.gauge)
        assertEquals(50, rec.mainFuseAmps)
    }

    @Test fun cca_is_bumped_one_size_heavier() {
        val ofc = PowerWiringCalculator.recommend(50, 10.0, isCca = false) // 8 AWG
        val cca = PowerWiringCalculator.recommend(50, 10.0, isCca = true)  // -> 6 AWG
        assertEquals("8 AWG", ofc.gauge)
        assertEquals("6 AWG", cca.gauge)
    }

    @Test fun fuse_never_exceeds_wire_ampacity() {
        // Deliberately high current: must land on 1/0 and a fuse within its rating.
        val rec = PowerWiringCalculator.recommend(200, 17.0, isCca = false)
        assertEquals("1/0 AWG", rec.gauge)
        assertTrue("fuse must not exceed 1/0 ampacity", rec.mainFuseAmps <= 275)
        assertTrue("fuse must cover design current", rec.mainFuseAmps >= 200)
    }

    @Test fun long_run_steps_up_gauge_for_voltage_drop() {
        // 50A over a very long 30ft run should force a thicker wire than the short run.
        val short = PowerWiringCalculator.recommend(50, 6.0, isCca = false)
        val long = PowerWiringCalculator.recommend(50, 30.0, isCca = false)
        assertTrue(
            "Long run must be same or thicker gauge",
            gaugeRank(long.gauge) >= gaugeRank(short.gauge)
        )
        assertTrue(long.voltageDropVolts <= 0.5)
    }

    private fun gaugeRank(g: String): Int =
        listOf("10 AWG", "8 AWG", "6 AWG", "4 AWG", "2 AWG", "1/0 AWG").indexOf(g)
}
