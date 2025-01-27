package pieces;
import pieces.enums.*;

public class PieceHandler {
    public static boolean in_range(int[] pos) {
        return 0 <= pos[0] && pos[0] <= 7 && 0 <= pos[1] && pos[1] <= 7;
    }

    public static boolean king_check(Piece[][] board, int[] origin_pos, int[] final_pos, PieceColor color) {
        return false;
    }
}
