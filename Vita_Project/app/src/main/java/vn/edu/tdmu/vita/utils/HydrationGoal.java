package vn.edu.tdmu.vita.utils;

public final class HydrationGoal {
    private HydrationGoal() {}

    public static int forUserGoal(int goal) { return goal > 0 ? goal : 2000; }
}
