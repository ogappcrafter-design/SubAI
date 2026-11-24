package com.ogapp.subai.engine

import com.ogapp.subai.domain.engine.ImpedanceCalculator
import com.ogapp.subai.domain.model.Connection
import com.ogapp.subai.domain.model.CoilType
import com.ogapp.subai.domain.model.MatchVerdict
import com.ogapp.subai.domain.model.SubStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImpedanceCalculatorTest {

    private fun totalFor(
        stage: SubStage, coil: Connection?, sub: Connection?
    ): Double = ImpedanceCalculator.options(stage)
        .first { it.coilConnection == coil && it.subConnection == sub }
        .totalOhms

    @Test fun svc_single_is_coil_value() {
        val opts = ImpedanceCalculator.options(SubStage(1, CoilType.SVC, 4.0))
        assertEquals(1, opts.size)
        assertEquals(4.0, opts[0].totalOhms, 0.001)
    }

    @Test fun dvc2_single_series_is_4_parallel_is_1() {
        val stage = SubStage(1, CoilType.DVC, 2.0)
        assertEquals(4.0, totalFor(stage, Connection.SERIES, null), 0.001)
        assertEquals(1.0, totalFor(stage, Connection.PARALLEL, null), 0.001)
    }

    @Test fun dvc4_single_series_is_8_parallel_is_2() {
        val stage = SubStage(1, CoilType.DVC, 4.0)
        assertEquals(8.0, totalFor(stage, Connection.SERIES, null), 0.001)
        assertEquals(2.0, totalFor(stage, Connection.PARALLEL, null), 0.001)
    }

    // Research-confirmed: two dual-4Ω subs, coils parallel (2Ω each), subs parallel = 1Ω
    @Test fun two_dvc4_coils_parallel_subs_parallel_is_1() {
        val stage = SubStage(2, CoilType.DVC, 4.0)
        assertEquals(1.0, totalFor(stage, Connection.PARALLEL, Connection.PARALLEL), 0.001)
    }

    // Research-confirmed: two dual-2Ω subs, coils series (4Ω each), subs parallel = 2Ω
    @Test fun two_dvc2_coils_series_subs_parallel_is_2() {
        val stage = SubStage(2, CoilType.DVC, 2.0)
        assertEquals(2.0, totalFor(stage, Connection.SERIES, Connection.PARALLEL), 0.001)
    }

    @Test fun two_dvc2_coils_series_subs_series_is_8() {
        val stage = SubStage(2, CoilType.DVC, 2.0)
        assertEquals(8.0, totalFor(stage, Connection.SERIES, Connection.SERIES), 0.001)
    }

    @Test fun four_subs_offer_series_parallel_option() {
        val stage = SubStage(4, CoilType.DVC, 2.0)
        val hasSeriesParallel = ImpedanceCalculator.options(stage)
            .any { it.subConnection == Connection.SERIES_PARALLEL }
        assertTrue("4-sub stage must offer a series-parallel option", hasSeriesParallel)
    }

    @Test fun match_optimal_is_exact_load() {
        // 1-ohm-stable mono amp, DVC2 single -> parallel (1Ω) should be OPTIMAL.
        val matched = ImpedanceCalculator.matchToAmp(SubStage(1, CoilType.DVC, 2.0), ampMinOhms = 1.0)
        val one = matched.first { it.option.totalOhms == 1.0 }
        assertEquals(MatchVerdict.OPTIMAL, one.verdict)
        val four = matched.first { it.option.totalOhms == 4.0 }
        assertEquals(MatchVerdict.SAFE, four.verdict)
    }

    @Test fun match_flags_below_minimum_as_risky() {
        // 2-ohm-min amp fed a 1Ω option must be flagged RISKY_LOW.
        val matched = ImpedanceCalculator.matchToAmp(SubStage(1, CoilType.DVC, 2.0), ampMinOhms = 2.0)
        val one = matched.first { it.option.totalOhms == 1.0 }
        assertEquals(MatchVerdict.RISKY_LOW, one.verdict)
        // and the 4Ω (only safe one) is promoted to OPTIMAL as the best achievable
        val four = matched.first { it.option.totalOhms == 4.0 }
        assertEquals(MatchVerdict.OPTIMAL, four.verdict)
    }

    @Test fun no_duplicate_total_topologies() {
        val opts = ImpedanceCalculator.options(SubStage(2, CoilType.DVC, 4.0))
        val keys = opts.map { "${it.totalOhms}|${it.coilConnection}|${it.subConnection}" }
        assertEquals(keys.size, keys.toSet().size)
    }
}
