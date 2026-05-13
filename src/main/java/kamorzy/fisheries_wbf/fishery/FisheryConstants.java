package kamorzy.fisheries_wbf.fishery;

public final class FisheryConstants {
    private static final int TICKS_PER_SECOND = 20;

    public static final int INITIAL_BAIT_DELAY_TICKS = 2 * TICKS_PER_SECOND;

    public static final int ACTIVE_ATTRACTION_INTERVAL_TICKS = TICKS_PER_SECOND;

    public static final double FISH_NOTICE_RANGE = 8.0D;
    public static final double BITE_DISTANCE = 1.35D;
    public static final double ATTRACT_SPEED = 1.1D;

    public static final int FISH_COOLDOWN_TICKS = 10 * 60 * TICKS_PER_SECOND;
    public static final int COMPLETE_RETRY_DELAY_TICKS = 5 * TICKS_PER_SECOND;

    private FisheryConstants() {
    }
}