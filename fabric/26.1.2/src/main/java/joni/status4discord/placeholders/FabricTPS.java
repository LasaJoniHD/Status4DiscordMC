package joni.status4discord.placeholders;

public class FabricTPS {

    private static final int MAX_SAMPLES = 20 * 60 * 15; // 15 minutes at 20 TPS

    private static final long[] tickTimes = new long[MAX_SAMPLES];

    private static int index = 0;
    private static int samples = 0;

    private FabricTPS() {
    }

    /**
     * Called once every server tick.
     */
    public static void recordTick() {
        tickTimes[index] = System.nanoTime();

        index = (index + 1) % MAX_SAMPLES;

        if (samples < MAX_SAMPLES) {
            samples++;
        }
    }

    public static double getTPS(int m) {
        long windowNanos = switch (m) {
            case 0 -> 60L * 1_000_000_000L;        // 1 minute
            case 1 -> 5L * 60L * 1_000_000_000L;  // 5 minutes
            case 2 -> 15L * 60L * 1_000_000_000L; // 15 minutes
            default -> throw new IllegalArgumentException("Invalid TPS interval: " + m);
        };

        if (samples < 2) {
            return 20.0;
        }

        long now = System.nanoTime();
        long oldest = now - windowNanos;

        int count = 0;

        for (int i = 0; i < samples; i++) {
            int position = (index - 1 - i + MAX_SAMPLES) % MAX_SAMPLES;

            long time = tickTimes[position];

            if (time < oldest) {
                break;
            }

            count++;
        }

        if (count < 2) {
            return 20.0;
        }

        long newestTime = tickTimes[(index - 1 + MAX_SAMPLES) % MAX_SAMPLES];

        int oldestPosition = (index - count + MAX_SAMPLES) % MAX_SAMPLES;
        long oldestTime = tickTimes[oldestPosition];

        double elapsedSeconds =
                (newestTime - oldestTime) / 1_000_000_000.0;

        if (elapsedSeconds <= 0) {
            return 20.0;
        }

        double tps = (count - 1) / elapsedSeconds;

        return Math.min(20.0, Math.max(0.0, tps));
    }
}
