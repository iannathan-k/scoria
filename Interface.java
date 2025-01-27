import pieces.Piece;
import pieces.enums.*;

public abstract class Interface {

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

    public static String posToSquare(int[] pos) {
        return (char) (97 + pos[1]) + Integer.toString(8 - pos[0]);
    }

    public static int[] squareToPos(String square) {
        return new int[] {8 - (square.charAt(1) - '0'), square.charAt(0) - 'a'};
    }

    public static String moveToUci(int[] origin_pos, int[] target_pos) {
        return posToSquare(origin_pos) + posToSquare(target_pos);
    }

    public static int[][] uciToMove(String uci) {
        return new int[][] {squareToPos(uci.substring(0, 2)), squareToPos(uci.substring(2, 4))};
    }
}