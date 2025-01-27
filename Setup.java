import pieces.*;
import pieces.enums.*;

public abstract class Setup {

    public static Piece makePieceObj(char piece, int index) {
        switch (piece) {
            case 'P':
                return new Pawn(new int[] {index / 8, index % 8}, PieceColor.WHITE, -1);
            case 'N':
                return new Knight(new int[] {index / 8, index % 8}, PieceColor.WHITE);
            case 'B':
                return new Bishop(new int[] {index / 8, index % 8}, PieceColor.WHITE);
            case 'R':
                return new Rook(new int[] {index / 8, index % 8}, PieceColor.WHITE);
            case 'Q':
                return new Queen(new int[] {index / 8, index % 8}, PieceColor.WHITE);
            case 'K':
                return new King(new int[] {index / 8, index % 8}, PieceColor.WHITE);  
            case 'p':
                return new Pawn(new int[] {index / 8, index % 8}, PieceColor.BLACK, 1);
            case 'n':
                return new Knight(new int[] {index / 8, index % 8}, PieceColor.BLACK);
            case 'b':
                return new Bishop(new int[] {index / 8, index % 8}, PieceColor.BLACK);
            case 'r':
                return new Rook(new int[] {index / 8, index % 8}, PieceColor.BLACK);
            case 'q':
                return new Queen(new int[] {index / 8, index % 8}, PieceColor.BLACK);
            case 'k':
                return new King(new int[] {index / 8, index % 8}, PieceColor.BLACK);
        }

        throw new UnsupportedOperationException("!! UNSUPPORTED PIECETYPE !!");
    }
    
    public static void setUp(Piece[][] board, String fen) {
        int index = 0;
        for (int i = 0; i < fen.length(); i++) {
            char piece = fen.charAt(i);

            if (piece == '/' || piece == ' ') {
                continue;
            }
            if (Character.isDigit(piece)) {
                index += Character.getNumericValue(piece);
                continue;
            }

            board[index / 8][index % 8] = makePieceObj(piece, index);
            index += 1;
        }
    }
}
