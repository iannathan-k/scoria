package src;

import java.util.Random;

import pieces.*;
import scoria.Scoria;
import scoria.Zobrist;

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

        Zobrist.initTable();

        // System.out.println(PieceType.KING.ordinal() + PieceColor.BLACK.ordinal() * 6);

        // final int TEST_RUNS = 1_000_000;
        // boolean turn = new Random().nextBoolean();
        // long startTime = System.nanoTime();
        // for (int i = 0; i < TEST_RUNS; i++) {
        //     Zobrist.manualHash(board, turn);
        // }
        // long endTime = System.nanoTime();
        // long totalTime = endTime - startTime;
        // double avgTimePerHash = (double) totalTime / TEST_RUNS;
        // System.out.printf("Total time for %d hashes: %.3f ms%n", TEST_RUNS, totalTime / 1_000_000.0);
        // System.out.printf("Average time per hash: %.3f ns%n", avgTimePerHash);

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

        // Interface.printBoard(board);

        long startTime = System.nanoTime();

        // // for (int i = 0; i < 1; i++) {
        // //     int[][] scoria_move = Scoria.minimax(board, 5, -Integer.MAX_VALUE, Integer.MAX_VALUE, true);
        // // }

        int[][] scoria_move = Scoria.minimax(board, 6, -Integer.MAX_VALUE, Integer.MAX_VALUE, true);

        long endTime = System.nanoTime();

        long elapsedTime = (endTime - startTime) / 1_000_000;

        System.out.println("Elapsed time: " + elapsedTime + " ms");
        System.out.println("Average time: " + elapsedTime / 1 + " ms");
        System.out.println("Move Count: " + move_count);

        startTime = System.nanoTime();

        scoria_move = Scoria.minimax(board, 7, -Integer.MAX_VALUE, Integer.MAX_VALUE, true);

        endTime = System.nanoTime();

        elapsedTime = (endTime - startTime) / 1_000_000;

        System.out.println("Elapsed time: " + elapsedTime + " ms");
        System.out.println("Average time: " + elapsedTime / 1 + " ms");
        System.out.println("Move Count: " + move_count);

    }

}