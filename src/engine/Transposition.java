package src.engine;

import java.util.Arrays;

public class Transposition {
    public static final byte NULL_NODE  = 0b00;
    public static final byte ALPHA_NODE = 0b01; // Upper Bound
    public static final byte BETA_NODE  = 0b10; // Lower Bound
    public static final byte EXACT_NODE = 0b11; // Exact Bound
    private static final int TYPE_MASK  = 0b11;

    public static final int NULL_ENTRY = -1;
    private static final int BUCKET_SIZE = 2;
    private static final long ENTRY_SIZE = 16L;
    private static final long MB_CONVERSION = 1024L * 1024L;

    private static int tt_size = 1 << 20;
    private static int tt_mask = tt_size - 1;
    private static int current_gen = 0;

    private static final int MAX_GENERATION = 63;
    private static final int GENERATION_BOUND = 24;

    private static long[] tt_hash   = new long[tt_size * BUCKET_SIZE];
    private static byte[] tt_depth  = new byte[tt_size * BUCKET_SIZE];
    private static byte[] tt_info   = new byte[tt_size * BUCKET_SIZE];
    private static short[] tt_score = new short[tt_size * BUCKET_SIZE];
    private static int[] tt_move    = new int[tt_size * BUCKET_SIZE];

    /* tt_info
     * 000000 00
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

    public static int countHashFull() {
        int count = 0;

        for (int i = 0; i < 1000; i++) {
            if (tt_hash[i] == 0) {
                continue;
            }
            if (relativeAge(tt_info[i] >>> 2) > GENERATION_BOUND) {
                continue;
            }

            count++;
        }

        return count;
    }

    public static void addTransposition(long hash, int depth, int score, byte type, int best_move, int ply) {
        int i = (int) (hash & tt_mask) * BUCKET_SIZE;

        if (score < -Evaluator.MATE_BOUND) {
            score -= ply;
        } else if (score > Evaluator.MATE_BOUND) {
            score += ply;
        }

        if (relativeAge(tt_info[i] >>> 2) > GENERATION_BOUND 
            || depth > tt_depth[i]) {
            
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

    public static int probe(long hash) {
        int i = (int) (hash & tt_mask) * BUCKET_SIZE;

        if (tt_hash[i] == hash) {
            return i;
        }

        if (tt_hash[i + 1] == hash) {
            return i + 1;
        }

        return NULL_ENTRY;
    }

    public static int getDepth(int index) {
        return tt_depth[index];
    }

    public static int getScore(int index, int ply) {
        int score = tt_score[index];
        if (score < -Evaluator.MATE_BOUND) {
            score += ply;
        } else if (score > Evaluator.MATE_BOUND) {
            score -= ply;
        }

        return score;
    }

    public static int getBestMove(int index) {
        return tt_move[index];
    }

    public static int getNodeType(int index) {
        return tt_info[index] & TYPE_MASK;
    }

    public static boolean isInformative(int bound, int tt_score, int eval) {
        int r_bound = (tt_score >= eval) ? BETA_NODE : ALPHA_NODE;
        return (bound & r_bound) != 0;
    }
}
