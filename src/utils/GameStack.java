package src.utils;

public class GameStack {

    private static final int MAX_DEPTH = 512;

    private static final int[] MOVE_STACK       = new int[MAX_DEPTH];
    private static final int[] TURN_STACK       = new int[MAX_DEPTH];
    private static final int[] CASTLE_STACK     = new int[MAX_DEPTH];
    private static final int[] PASSANT_STACK    = new int[MAX_DEPTH];
    private static final int[] CAPTURED_STACK   = new int[MAX_DEPTH];
    private static final long[] HASH_STACK      = new long[MAX_DEPTH];
    private static final int[] EVAL_STACK       = new int[MAX_DEPTH];

    private static int top_pointer = 0;

    public static void push(int move, int turn, int castle_rights, int passant_rights, int captured, long hash, int evaluation) {
        MOVE_STACK[top_pointer] = move;
        TURN_STACK[top_pointer] = turn;
        CASTLE_STACK[top_pointer] = castle_rights;
        PASSANT_STACK[top_pointer] = passant_rights;
        CAPTURED_STACK[top_pointer] = captured;
        HASH_STACK[top_pointer] = hash;
        EVAL_STACK[top_pointer] = evaluation;

        top_pointer++;
    }

    public static void pop() {
        top_pointer--;
    }

    public static int peekMove() {
        return MOVE_STACK[top_pointer - 1];
    }

    public static int peekTurn() {
        return TURN_STACK[top_pointer - 1];
    }

    public static int peekCastlingRights() {
        return CASTLE_STACK[top_pointer - 1];
    }

    public static int peekPassantRights() {
        return PASSANT_STACK[top_pointer - 1];
    }

    public static int peekCaptured() {
        return CAPTURED_STACK[top_pointer - 1];
    }

    public static int peekEvaluation() {
        return EVAL_STACK[top_pointer - 1];
    }

    public static long peekHash() {
        return HASH_STACK[top_pointer - 1];
    }

    public static int getMoveAt(int index) {
        return MOVE_STACK[index];
    }

    public static int getTurnAt(int index) {
        return TURN_STACK[index];
    }

    public static int getCastlingRightsAt(int index) {
        return CASTLE_STACK[index];
    }

    public static int getPassantRightsAt(int index) {
        return PASSANT_STACK[index];
    }

    public static int getCapturedAt(int index) {
        return CAPTURED_STACK[index];
    }

    public static long getHashAt(int index) {
        return HASH_STACK[index];
    }

    public static int getEvaluationAt(int index) {
        return EVAL_STACK[index];
    }

    public static int size() {
        return top_pointer;
    }

    public static void clear() {
        top_pointer = 0;
    }
}