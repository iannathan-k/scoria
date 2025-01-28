package src;
import pieces.*;
import pieces.enums.*;

public abstract class Setup {

    public static Piece makePieceObj(char piece, int index) {

        PieceColor color = PieceColor.WHITE;
        int dir = -1;
        int king_index = 0;

        if (Character.isLowerCase(piece)) {
            color = PieceColor.BLACK;
            dir = 1;
            piece = Character.toUpperCase(piece);
            king_index = 1;
        }

        switch (piece) {
            case 'P':
                return new Pawn(new int[] {index / 8, index % 8}, color, dir);
            case 'N':
                return new Knight(new int[] {index / 8, index % 8}, color);
            case 'B':
                return new Bishop(new int[] {index / 8, index % 8}, color);
            case 'R':
                return new Rook(new int[] {index / 8, index % 8}, color);
            case 'Q':
                return new Queen(new int[] {index / 8, index % 8}, color);
            case 'K':
                PieceHandler.setKingPiece(new King(new int[] {index / 8, index % 8}, color), king_index); 
                return PieceHandler.getKingPiece(king_index);
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
