package src;

import pieces.*;
import scoria.Scoria;

public class Main {

    public static Piece[][] board = new Piece[8][8];
    public static int move_count = 0;

    public static void main(String args[]) {
        
        System.out.println("Starting...");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = new Empty();
            }
        }

        // Setup.setUp(board, "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R");
        Setup.setUp(board, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        // Setup.setUp(board, "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1");

        // ArrayList<int[][]> moves = PieceHandler.getAllMoves(board, PieceColor.WHITE);
        // // // ((King) board[0][5]).pushMove();
        // for (int i = 0; i < moves.size(); i++) {
        //     int[][] move = moves.get(i);
        //     for (int j = 0; j < 2; j++) {
        //         for (int k = 0; k < 2; k++) {
        //             System.out.print(move[j][k] + " "); 
        //         }
        //         System.out.println("   ");
        //     }
        //     System.out.println();
        // }

        long startTime = System.nanoTime();

        for (int i = 0; i < 1; i++) {
            int[][] scoria_move = Scoria.minimax(board, 5, -Integer.MAX_VALUE, Integer.MAX_VALUE, true);
        }

        long endTime = System.nanoTime();

        long elapsedTime = (endTime - startTime) / 1_000_000;

        System.out.println("Elapsed time: " + elapsedTime + " ms");
        System.out.println("Average time: " + elapsedTime / 1 + " ms");
        System.out.println("Move Count: " + move_count);
    }

}