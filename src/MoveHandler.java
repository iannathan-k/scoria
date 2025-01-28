package src;

import pieces.Empty;
import pieces.Piece;

public class MoveHandler {
    public static Piece[] moveState(Piece[][] board, int[] origin_pos, int[] target_pos) {
        Piece piece = board[origin_pos[0]][origin_pos[1]];
        Piece captured = board[target_pos[0]][target_pos[1]];

        board[target_pos[0]][target_pos[1]] = piece;
        board[origin_pos[0]][origin_pos[1]] = new Empty();
        piece.setPosition(target_pos);

        return new Piece[] {captured, piece};
    }

    public static void undoState(Piece[][] board, int[] origin_pos, int[] target_pos, Piece[] board_info) {
        board[origin_pos[0]][origin_pos[1]] = board_info[1];
        board[target_pos[0]][target_pos[1]] = board_info[0];
        board_info[1].setPosition(origin_pos);
    }
}
