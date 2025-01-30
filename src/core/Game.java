package src.core;

import src.pieces.Empty;
import src.pieces.Piece;
import src.scoria.Zobrist;

public class Game {
    
    public static Piece[][] board = new Piece[8][8];
    public final static long THINK_TIME = 2500;
    public static int move_count;
    private static int move_number;
    private static int last_think_time;
    private static int last_depth;

    public static void initGame(String fen) {
        // initialize board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = new Empty();
            }
        }

        // setup board
        Setup.setUp(board, fen);

        // initialize zobrist
        Zobrist.initTable();

        // initialize game info
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

    public static void setLastThinkTime(int time) {
        last_think_time = time;
    }

    public static int getLastThinkTime() {
        return last_think_time;
    }

    public static void setLastThinkDepth(int depth){
        last_depth = depth;
    }

    public static int getLastThinkDepth() {
        return last_depth;
    }

}
