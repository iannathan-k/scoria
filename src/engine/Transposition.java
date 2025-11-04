package src.engine;

import java.util.Arrays;

public class Transposition {
    public static final byte EXACT_NODE = 0;
    public static final byte ALPHA_NODE = 1;
    public static final byte BETA_NODE = 2;

    private static final long ENTRY_SIZE = 16L;
    private static final long MB_CONVERSION = 1024L * 1024L;

    private static int tt_size = 1 << 20;
    private static int tt_mask = tt_size - 1;
    private static int current_gen = 0;

    private static final int MAX_GENERATION = 63;
    
    private static final int TYPE_MASK = 0b11;

    private static final int BUCKET_SIZE = 2;

    private static long[] tt_hash   = new long[tt_size * BUCKET_SIZE];
    private static byte[] tt_depth  = new byte[tt_size * BUCKET_SIZE]; // FIXME: MAY BE AN ISSUE WITH OVERFLOW
    private static byte[] tt_info   = new byte[tt_size * BUCKET_SIZE];
    private static short[] tt_score = new short[tt_size * BUCKET_SIZE];
    private static int[] tt_move    = new int[tt_size * BUCKET_SIZE];

    /* tt_info
     * 111111 11
     * gen    type
     */

    public static void resizeTranspositionTable(long mb_size) {
        long ideal_size = mb_size * MB_CONVERSION / ENTRY_SIZE / BUCKET_SIZE;
        tt_size = (int) Long.highestOneBit(ideal_size);
        tt_mask = tt_size - 1;

        tt_hash     = new long[tt_size * BUCKET_SIZE];
        tt_depth    = new byte[tt_size * BUCKET_SIZE];
        tt_info     = new byte[tt_size * BUCKET_SIZE];
        tt_score    = new short[tt_size * BUCKET_SIZE];
        tt_move     = new int[tt_size * BUCKET_SIZE];
    }

    public static void nextGeneration() {
        current_gen = (current_gen + 1) & MAX_GENERATION;
    }

    private static int relativeAge(int entry_gen) {
        return (current_gen - entry_gen) & MAX_GENERATION;
    }

    // FIXME: Figure out a better replacement policy
    public static void addTransposition(long hash, int depth, int score, byte type, int best_move, int ply) {
        int i = (int) (hash & tt_mask) * BUCKET_SIZE;

        if (score < -Evaluator.MATE_BOUND) {
            score -= ply;
        } else if (score > Evaluator.MATE_BOUND) {
            score += ply;
        }

        if (relativeAge(tt_info[i] >>> 2) > 24 || depth > tt_depth[i]) {
            tt_hash[i] = hash;
            tt_depth[i] = (byte) depth;
            tt_info[i] = (byte) (current_gen << 2 | type);
            tt_score[i] = (short) score;
            tt_move[i] = best_move;
        } else {
            tt_hash[i + 1] = hash;
            tt_depth[i + 1] = (byte) depth;
            tt_info[i + 1] = (byte) (current_gen << 2 | type);
            tt_score[i + 1] = (short) score;
            tt_move[i + 1] = best_move;
        }
    }

    public static void clearTranspositionTable() {
        Arrays.fill(tt_hash, 0L);
        Arrays.fill(tt_depth, (byte) 0);
        Arrays.fill(tt_info, (byte) 0);
        Arrays.fill(tt_score, (short) 0);
        Arrays.fill(tt_move, 0);
    }

    public static boolean doesExist(long hash) {
        int i = (int) (hash & tt_mask) * BUCKET_SIZE;
        return tt_hash[i] == hash || tt_hash[i + 1] == hash;
    }

    public static int getDepth(long hash) {
        int i = (int) (hash & tt_mask) * BUCKET_SIZE;
        return (tt_hash[i] == hash) ? tt_depth[i] : tt_depth[i + 1];
    }

    // FIXME: DOUBLE CHECK MATE BOUNDS
    public static int getScore(long hash, int ply) {
        int i = (int) (hash & tt_mask) * BUCKET_SIZE;

        int score = (tt_hash[i] == hash) ? tt_score[i] : tt_score[i + 1];
        if (score < -Evaluator.MATE_BOUND) {
            score += ply;
        } else if (score > Evaluator.MATE_BOUND) {
            score -= ply;
        }

        return score;
    }

    public static int getBestMove(long hash) {
        int i = (int) (hash & tt_mask) * BUCKET_SIZE;
        return (tt_hash[i] == hash) ? tt_move[i] : tt_move[i + 1];
    }

    public static boolean isExact(long hash) {
        int i = (int) (hash & tt_mask) * BUCKET_SIZE;
        return (tt_hash[i] == hash) ? 
            (tt_info[i] & TYPE_MASK) == EXACT_NODE : 
            (tt_info[i + 1] & TYPE_MASK) == EXACT_NODE;
    }

    public static boolean isAlpha(long hash) {
        int i = (int) (hash & tt_mask) * BUCKET_SIZE;
        return (tt_hash[i] == hash) ? 
            (tt_info[i] & TYPE_MASK) == ALPHA_NODE : 
            (tt_info[i + 1] & TYPE_MASK) == ALPHA_NODE;
    }

    public static boolean isBeta(long hash) {
        int i = (int) (hash & tt_mask) * BUCKET_SIZE;
        return (tt_hash[i] == hash) ? 
            (tt_info[i] & TYPE_MASK) == BETA_NODE : 
            (tt_info[i + 1] & TYPE_MASK) == BETA_NODE;
    }
}
