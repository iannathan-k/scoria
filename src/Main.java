package src;
import java.util.ArrayDeque;
import java.util.Deque;

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

        // board[4][4] = new King(new int[] {4,4}, PieceColor.BLACK);
        // board[3][5] = new Pawn(new int[] {4,7}, PieceColor.BLACK, -1);
        // System.out.println(PieceHandler.kingInCheck(board, PieceColor.WHITE));

        // board[2][1] = new Pawn(new int[] {2,1}, PieceColor.BLACK, -1);

        // ArrayList<int[]> moves = board[4][4].getMoves(board);
        // for (int i = 0; i < moves.size(); i++) {
        //     int[] move = moves.get(i);
        //     for (int j = 0; j < 2; j++) {
        //         System.out.print(move[j] + " "); 
        //     }
        //     System.out.println();
        // }

        Setup.setUp(board, "r3k2r/p1ppqpb1/Bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPB1PPP/R3K2R");
        // Setup.setUp(board, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        // Setup.setUp(board, "8/8/8/1K6/8/8/5k2/8");
        // Setup.setUp(board, "4k3/8/8/8/8/8/8/R3K2R");

        Interface.printBoard(board);

        // MoveHandler.moveState(board, new int[] {7, 4}, new int[] {7, 2});

        // Interface.printBoard(board);

        // System.out.println(board[7][4].getMoves(board));


        long startTime = System.nanoTime();

        for (int i = 0; i < 1; i++) {
            int[][] scoria_move = Scoria.minimax(board, 2, -Integer.MAX_VALUE, Integer.MAX_VALUE, false);
        }

        long endTime = System.nanoTime();

        long elapsedTime = (endTime - startTime) / 1_000_000;

        // MoveHandler.moveState(board, scoria_move[1], scoria_move[2]);
        Interface.printBoard(board);
        System.out.println(move_count);
        System.out.println("Elapsed time: " + elapsedTime + " ms");
        // System.out.println("Average time: " + elapsedTime / 10 + " ms");
    }

}