import pieces.Piece;
import pieces.enums.*;

public class Interface {

    private static String getChar(Piece piece) {
        PieceType type = piece.getType();
        switch (type) {
            case EMPTY:
                return " ";
            case PAWN:
                return "P";
            case KNIGHT:
                return "N";
            case BISHOP:
                return "B";
            case ROOK:
                return "R";
            case QUEEN:
                return "Q";
            case KING:
                return "K";
        }
        
        throw new UnsupportedOperationException("!! UNSUPPORTED PIECETYPE !!");
    }

    public static void printBoard(Piece[][] board) {
        System.out.println("    0   1   2   3   4   5   6   7");
        System.out.println("  +---+---+---+---+---+---+---+---+");

        for (int i = 0; i < 8; i++) {
            String line = i + " | ";

            for (Piece col : board[i]) {
                
                String piece = " ";
                PieceColor color = col.getColor();

                piece = getChar(col);

                if (color == PieceColor.BLACK) {
                    piece = piece.toLowerCase();
                }

                line += piece + " | ";

            }

            System.out.println(line);
            System.out.println("  +---+---+---+---+---+---+---+---+");
        }
    }
}