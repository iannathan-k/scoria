package src;
import pieces.*;
import scoria.Scoria;

class Main {

    public static Piece[][] board = new Piece[8][8];

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
        Interface.printBoard(board);
        int[][] scoria_move = Scoria.minimax(board, 3, false);
        MoveHandler.moveState(board, scoria_move[1], scoria_move[2]);
        Interface.printBoard(board);
    }

}