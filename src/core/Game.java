package src.core;

import src.scoria.Evaluator;
import src.scoria.Scoria;
import src.scoria.Zobrist;

public class Game {
    
    public static byte[] board = new byte[64];
    public static long THINK_TIME = 1000;
    public static int move_count;
    private static int move_number;
    private static long last_think_time;
    private static int last_depth;
    private static boolean turn;
    private static boolean human_side = true;

    public static void initGame(String fen) {
        Setup.setUp(board, fen);
        Zobrist.initTable();

        move_count = 0;
        move_number = 0;
        last_think_time = 0;
        last_depth = 0;
    }

    public static void nextMoveNumber() {
        move_number++;
    }

    public static void lastMoveNumber() {
        move_number--;
    }

    public static int currentMoveNumber() {
        return move_number;
    }

    public static void setLastThinkTime(long time) {
        last_think_time = time;
    }

    public static long getLastThinkTime() {
        return last_think_time;
    }

    public static void setLastThinkDepth(int depth){
        last_depth = depth;
    }

    public static int getLastThinkDepth() {
        return last_depth;
    }

    public static int[] getScoriaMove(boolean turn) {
        return Scoria.iterativeDeepener(board, turn);
    }

    public static boolean getTurn() {
        return turn;
    }

    public static void notTurn() {
        turn = !turn;
    }

    public static void setTurn(boolean set_turn) {
        turn = set_turn;
    }

    public static int getMoveCount() {
        int count = move_count;
        move_count = 0;
        return count;
    }

    public static boolean isGameOver() {
        long hash = Zobrist.manualHash(board, turn);
        return Evaluator.gameWinner(board, turn, hash) != -1;
    }

    public static void setPlayerSide(boolean side) {
        human_side = side;
    }

    public static String getPlayerColor() {
        return human_side ? "white" : "black";
    }

    public static boolean isHumanTurn() {
        return turn == human_side;
    }

}
