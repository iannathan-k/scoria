package src.core;

import src.scoria.Evaluator;
import src.scoria.Scoria;
import src.scoria.Zobrist;

public class Game {
    
    // Game Attributes

    public static byte[] board = new byte[64];
    public static int turn = 1;

    // Game Information

    public static int move_number;
    public static int node_count;

    public static void initGame(String fen) {
        Setup.setUp(board, fen);
        Zobrist.initTable();
        Scoria.clearHistoryTable();
        Evaluator.clearPositionTable();

        node_count = 0;
        move_number = 0;
    }

    public static int getNodeCount() {
        int count = node_count;
        node_count = 0;
        return count;
    }

}
