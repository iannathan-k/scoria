package src.pieces;

import java.util.ArrayList;

public abstract class Knight {

    public static ArrayList<Integer> getMoves(byte[] board, int pos) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();

        for (int[] dir : PieceData.KNIGHT_DIRECTIONS) {
            int row = (pos >> 3) + dir[0];
            int col = (pos & 7) + dir[1];
            if (!PieceHandler.inRange(row, col)) {
                continue;
            }

            int target = row << 3 | col;
            if ((board[target] & PieceData.COLOR_MASK) == (board[pos] & PieceData.COLOR_MASK)) {
                continue;
            }
            if (!PieceHandler.kingCheck(board, pos, target)) {
                possible_moves.add(pos << 8 | target);
            }
        }

        return possible_moves;

    }
    
}
