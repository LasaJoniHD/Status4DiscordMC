package joni.status4discordmc.placeholders;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

/**
 * Determines the JVM process CPU usage as a percentage, relative to all
 * available CPU cores. Works equally on Spigot, Paper, and Folia because
 * only standard JVM APIs are used (no dependency on the Bukkit/Folia scheduler).
 *
 * <p>Data source priority for each {@link #getCPU()} call:
 * <ol>
 *   <li>{@code OperatingSystemMXBean.getProcessCpuLoad()} - preferred, but may
 *       return -1 before the first sample or on unsupported platforms.</li>
 *   <li>Manual delta calculation using process CPU time and wall-clock time -
 *       fallback if (1) does not provide a valid value.</li>
 *   <li>Summed CPU time of all threads via {@link ThreadMXBean} - final fallback
 *       for JVMs that do not expose process CPU time.</li>
 * </ol>
 * </p>
 *
 * <p>Thread safety is not critical in the sense of parallel region access -
 * a global {@code synchronized} access is sufficient for display/command
 * purposes. With periodic polling (e.g. every few seconds using a scheduler),
 * {@link #getCPU()} provides reliable delta values because its internal state
 * is preserved between calls.</p>
 */
public final class CpuMonitor {

    private final OperatingSystemMXBeanExtended osBean;
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    private long previousCpuTime = -1L;
    private long previousWallTime = -1L;
    private double lastCpuPercent = 0.0;

    public CpuMonitor() {
        this.osBean = resolveOperatingSystemBean();
    }

    /**
     * Returns the current JVM process CPU usage as a percentage (0.0 - 100.0),
     * rounded to one decimal place.
     *
     * <p>The returned value is based on the last successful sample if the
     * current measurement does not provide a valid value (for example, shortly
     * after startup before enough time has passed to calculate a delta).</p>
     */
    public synchronized double getCPU() {
        double processLoad = -1.0;

        if (osBean != null) {
            try {
                processLoad = osBean.getProcessCpuLoad();
            } catch (RuntimeException ignored) {
                // Fall back to CPU-time sampling below.
            }
        }

        boolean hasProcessLoad = Double.isFinite(processLoad) && processLoad >= 0.0;
        double sampledCpu = sampleCpuPercent(!hasProcessLoad);

        if (hasProcessLoad && processLoad > 0.0) {
            lastCpuPercent = processLoad * 100.0;
        } else if (sampledCpu >= 0.0) {
            // Some JVMs expose a valid but permanently zero process load.
            lastCpuPercent = sampledCpu;
        } else if (hasProcessLoad) {
            lastCpuPercent = processLoad * 100.0;
        }

        return roundCpuPercent(lastCpuPercent);
    }

    private static OperatingSystemMXBeanExtended resolveOperatingSystemBean() {
        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

        return bean instanceof com.sun.management.OperatingSystemMXBean
                ? new OperatingSystemMXBeanExtended(
                (com.sun.management.OperatingSystemMXBean) bean)
                : null;
    }

    private double sampleCpuPercent(boolean allowThreadFallback) {
        long cpuTime = -1L;

        if (osBean != null) {
            try {
                cpuTime = osBean.getProcessCpuTime();
            } catch (RuntimeException ignored) {
                // Try the standard ThreadMXBean fallback below.
            }
        }

        if (cpuTime < 0L && allowThreadFallback) {
            cpuTime = getThreadCpuTime();
        }

        if (cpuTime < 0L) {
            previousCpuTime = -1L;
            previousWallTime = -1L;
            return -1.0;
        }

        long wallTime = System.nanoTime();
        double cpuPercent = -1.0;

        if (previousCpuTime >= 0L && previousWallTime >= 0L) {
            long cpuDelta = cpuTime - previousCpuTime;
            long wallDelta = wallTime - previousWallTime;

            if (cpuDelta >= 0L && wallDelta > 0L) {
                int processors = getAvailableProcessors();

                cpuPercent = (cpuDelta * 100.0) / wallDelta / processors;

                if (!Double.isFinite(cpuPercent)) {
                    cpuPercent = -1.0;
                }
            }
        }

        previousCpuTime = cpuTime;
        previousWallTime = wallTime;

        return cpuPercent;
    }

    private long getThreadCpuTime() {
        try {
            if (!threadBean.isThreadCpuTimeSupported()) {
                return -1L;
            }

            if (!threadBean.isThreadCpuTimeEnabled()) {
                threadBean.setThreadCpuTimeEnabled(true);
            }

            long totalCpuTime = 0L;
            boolean hasCpuTime = false;

            for (long threadId : threadBean.getAllThreadIds()) {
                long threadCpuTime = threadBean.getThreadCpuTime(threadId);

                if (threadCpuTime >= 0L) {
                    totalCpuTime += threadCpuTime;
                    hasCpuTime = true;
                }
            }

            return hasCpuTime ? totalCpuTime : -1L;
        } catch (RuntimeException ignored) {
            return -1L;
        }
    }

    private int getAvailableProcessors() {
        try {
            if (osBean != null) {
                return Math.max(1, osBean.getAvailableProcessors());
            }
        } catch (RuntimeException ignored) {
            // Use the Runtime fallback below.
        }

        return Math.max(1, Runtime.getRuntime().availableProcessors());
    }

    private static double roundCpuPercent(double cpuPercent) {
        cpuPercent = Math.max(0.0, Math.min(100.0, cpuPercent));
        return Math.round(cpuPercent * 10.0) / 10.0;
    }

    /**
     * Thin wrapper around {@link com.sun.management.OperatingSystemMXBean},
     * so the rest of the class does not directly depend on the com.sun type.
     * If this internal API changes in a future JDK version, only this wrapper
     * needs to be adjusted.
     */
    private static final class OperatingSystemMXBeanExtended {

        private final com.sun.management.OperatingSystemMXBean delegate;

        private OperatingSystemMXBeanExtended(
                com.sun.management.OperatingSystemMXBean delegate) {
            this.delegate = delegate;
        }

        double getProcessCpuLoad() {
            return delegate.getProcessCpuLoad();
        }

        long getProcessCpuTime() {
            return delegate.getProcessCpuTime();
        }

        int getAvailableProcessors() {
            return delegate.getAvailableProcessors();
        }
    }
}
