package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class SleepEvaluatorTest {

    @Test
    public void under6_isThieuNgu() {
        SleepEvaluator.SleepResult r = SleepEvaluator.classify(5.5f);
        assertEquals("Thiếu ngủ", r.label);
        assertEquals(HealthEvaluator.STATUS_WARNING, r.severity);
    }

    @Test
    public void sixToSeven_isHoiThieu() {
        assertEquals("Hơi thiếu ngủ", SleepEvaluator.classify(6.0f).label);
        assertEquals("Hơi thiếu ngủ", SleepEvaluator.classify(6.9f).label);
    }

    @Test
    public void sevenToNine_isDuGiac() {
        SleepEvaluator.SleepResult r = SleepEvaluator.classify(7.5f);
        assertEquals("Đủ giấc", r.label);
        assertEquals(HealthEvaluator.STATUS_GOOD, r.severity);
        assertEquals("Đủ giấc", SleepEvaluator.classify(7.0f).label);
        assertEquals("Đủ giấc", SleepEvaluator.classify(9.0f).label);
    }

    @Test
    public void over9_isNguNhieu() {
        SleepEvaluator.SleepResult r = SleepEvaluator.classify(10.5f);
        assertEquals("Ngủ nhiều", r.label);
        assertEquals(SleepEvaluator.STATUS_OVERSLEEP, r.severity);
    }
}
