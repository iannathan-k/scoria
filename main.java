import pieces.*;

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

        Setup.setUp(board, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        Interface.printBoard(board);
    }

}