package src.utils;

import src.game.MoveHandler;

public class GameStack {
    public static final int CMH_INDEX = 0;
    public static final int FMH_INDEX = 1;

    private static final int MAX_LENGTH = 512;

    private static final int[] MOVE_STACK       = new int[MAX_LENGTH];
    private static final int[] TURN_STACK       = new int[MAX_LENGTH];
    private static final int[] CASTLE_STACK     = new int[MAX_LENGTH];
    private static final int[] PASSANT_STACK    = new int[MAX_LENGTH];
    private static final int[] CAPTURED_STACK   = new int[MAX_LENGTH];
    private static final long[] HASH_STACK      = new long[MAX_LENGTH];
    private static final int[] MG_STACK         = new int[MAX_LENGTH];
    private static final int[] EG_STACK         = new int[MAX_LENGTH];
    private static final int[] PHASE_STACK      = new int[MAX_LENGTH];
    private static final int[] HALFMOVE_STACK   = new int[MAX_LENGTH];

    private static int top_pointer = 0;

    public static void push(
        int move, 
        int turn, 
        int castle_rights, 
        int passant_rights, 
        int captured, 
        long hash, 
        int mg_eval,
        int eg_eval,
        int phase,
        int halfmoves) {

        MOVE_STACK[top_pointer]     = move;
        TURN_STACK[top_pointer]     = turn;
        CASTLE_STACK[top_pointer]   = castle_rights;
        PASSANT_STACK[top_pointer]  = passant_rights;
        CAPTURED_STACK[top_pointer] = captured;
        HASH_STACK[top_pointer]     = hash;
        MG_STACK[top_pointer]       = mg_eval;
        EG_STACK[top_pointer]       = eg_eval;
        PHASE_STACK[top_pointer]    = phase;
        HALFMOVE_STACK[top_pointer] = halfmoves;

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

    public static int peekHalfmoves() {
        return HALFMOVE_STACK[top_pointer - 1];
    }

    public static int peekPassantRights() {
        return PASSANT_STACK[top_pointer - 1];
    }

    public static int peekCaptured() {
        return CAPTURED_STACK[top_pointer - 1];
    }

    public static int peekMGEval() {
        return MG_STACK[top_pointer - 1];
    }

    public static int peekEGEval() {
        return EG_STACK[top_pointer - 1];
    }

    public static int peekPhase() {
        return PHASE_STACK[top_pointer - 1];
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

    public static int getMGEvalAt(int index) {
        return MG_STACK[index];
    }

    public static int getHalfmovesAt(int index) {
        return HALFMOVE_STACK[index];
    }

    public static int getEGEvalAt(int index) {
        return EG_STACK[index];
    }

    public static int getPhaseAt(int index) {
        return PHASE_STACK[index];
    }

    public static boolean hasContinuation(int from_back) {
        return top_pointer > from_back 
            && MOVE_STACK[top_pointer - 1 - from_back] != MoveHandler.NULL_MOVE;
    }

    public static int getMoveFromBack(int from_back) {
        return MOVE_STACK[top_pointer - 1 - from_back];
    }
    
    public static int size() {
        return top_pointer;
    }

    public static void clear() {
        top_pointer = 0;
    }
}