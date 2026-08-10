package src.engine;

public class Timer {
    private static long start_time;
    private static long hardstop_time;
    private static long softstop_time;
    private static int max_depth;
    private static boolean abort_search;

    private static boolean ponder =  false;
    private static int overhead = 0;

    private static void reset() {
        abort_search = false;
        hardstop_time = Long.MAX_VALUE;
        softstop_time = Long.MAX_VALUE;
        max_depth = Search.MAX_DEPTH;
        start_time = System.currentTimeMillis();
    }

    public static void initDepthSearch(int depth) {
        reset();
        max_depth = depth;
    }

    public static void initMovetimeSearch(int duration) {
        reset();
        softstop_time = start_time + duration - overhead;
        hardstop_time = start_time + duration - overhead;
    }

    public static void initInfiniteSearch() {
        reset();
    }

    public static void initControlledSearch(int m_time, int o_time, int m_inc, int mtg) {
        reset();
        
        // Remaining time w/ all increments and overheads
        int r_time = m_time + m_inc * mtg - overhead * mtg;
        r_time = Math.max(0, r_time);

        // Optimal time is 2.5% Remaining time or 20% my time.
        int soft = Math.min(r_time / 40, m_time / 5);

        // Max time is 4x Optimal or 75% my time
        int hard = Math.min(4 * soft, m_time * 3 / 4);

        soft = Math.max(1, soft);
        hard = Math.max(1, hard);

        softstop_time = start_time + soft;
        hardstop_time = start_time + hard;
    }

    public static void setMoveOverhead(int value) {
        overhead = value;
    }

    public static void abort() {
        abort_search = true;
    }

    public static boolean hitSoftLimit(int depth) {
        // Depth based search and optimal time exit
        return abort_search 
            || System.currentTimeMillis() >= softstop_time
            || depth > max_depth;
    }

    public static boolean hitHardLimit() {
        // Maximum time exit
        return abort_search || System.currentTimeMillis() >= hardstop_time;
    }

    public static long elapsed() {
        return System.currentTimeMillis() - start_time;
    }

    public static boolean doPonder() {
        return ponder;
    }

    public static void setPonder(boolean value) {
        ponder = value;
    }
}
