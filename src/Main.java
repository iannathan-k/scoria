package src;

import java.util.*;
import pieces.*;
import pieces.enums.PieceColor;
import scoria.Scoria;

public class Main {

    public static Piece[][] board = new Piece[8][8];
    public static int move_count = 0;
    public static int capture_count = 0;
    public static int castle_count = 0;
    public static int ep_count = 0;
    public static int promo_count = 0;

    public static void main(String args[]) {
        
        System.out.println("Starting...");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = new Empty();
            }
        }

        // Setup.setUp(board, "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R");

        // Setup.setUp(board, "rnbqkbnr/ppppp1pp/5p2/7Q/8/4P3/PPPP1PPP/RNB1KBNR");
        // Setup.setUp(board, "8/8/8/1K6/8/8/5k2/8");
        // Setup.setUp(board, "4k3/8/8/8/8/8/8/R3K2R");
        // Setup.setUp(board, "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8");
        // Setup.setUp(board, "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R");
        // Setup.setUp(board, "8/3p3k/8/4P3/8/4K3/8/8");
        // Setup.setUp(board, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        Setup.setUp(board, "8/2p5/3p4/1P5r/KR3p1k/8/4P1P1/8");

        // Interface.printBoard(board);

        // ((Pawn) board[4][5]).pushLeft(PieceHandler.currentMoveNumber());

        // MoveHandler.moveState(board, new int[] {6, 4}, new int[] {4, 4});

        // Interface.printBoard(board);

        // board[4][4] = new King(new int[] {4,4}, PieceColor.BLACK);
        // board[3][5] = new Pawn(new int[] {4,7}, PieceColor.BLACK, -1);
        // System.out.println(PieceHandler.kingInCheck(board, PieceColor.WHITE));

        // board[2][1] = new Pawn(new int[] {2,1}, PieceColor.BLACK, -1);

        // Setup.setUp(board, "rnbq1k1r/pp1Pbppp/2p5/8/2B1n3/1P6/P1P1N1PP/RNBQK2R");


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


        // MoveHandler.moveState(board, new int[] {7, 4}, new int[] {7, 2});

        // Interface.printBoard(board);

        // System.out.println(board[7][4].getMoves(board));


        // long startTime = System.nanoTime();

        for (int i = 0; i < 1; i++) {
            int[][] scoria_move = Scoria.minimax(board, 2, -Integer.MAX_VALUE, Integer.MAX_VALUE, false);
        }

        // System.out.println(PieceHandler.getAllMoves(board, PieceColor.BLACK).size());

        // long endTime = System.nanoTime();

        // long elapsedTime = (endTime - startTime) / 1_000_000;

        // MoveHandler.moveState(board, scoria_move[1], scoria_move[2]);
        // Interface.printBoard(board);
        System.out.println("Moves: " + move_count);
        System.out.println("Captures: " + capture_count);
        System.out.println("En passants: " + ep_count);
        System.out.println("Castles: " + castle_count);
        System.out.println("Promotions: " + promo_count);
        // System.out.println("Elapsed time: " + elapsedTime + " ms");
        // System.out.println("Average time: " + elapsedTime / 10 + " ms");
    }

}