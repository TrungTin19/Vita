package vn.edu.tdmu.vita.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HydrationGoalTest {
    @Test public void goalUsesChosenAmountAndFallsBackWhenUnset() {
        assertEquals(1500, HydrationGoal.forUserGoal(1500));
        assertEquals(2000, HydrationGoal.forUserGoal(0));
        assertEquals(2000, HydrationGoal.forUserGoal(-1));
    }
}
