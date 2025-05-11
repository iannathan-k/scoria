package src.core;

public class Game {
    
    // Game Attributes

    public static byte[] board = new byte[64];
    public static int turn = 1;

    // Game Information

    public static int move_number;
    public static int node_count;

    public static void initGame(String fen) {
        Setup.setUp(board, fen);

        node_count = 0;
        move_number = 0;
    }
}
