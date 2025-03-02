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
            int move = pos << 8 | target;

            if (board[target] == PieceData.EMPTY) {
                if (!PieceHandler.kingCheck(board, move, board[target] & PieceData.COLOR_MASK)) {
                    possible_moves.add(move);
                }
            } else if ((board[target] & PieceData.COLOR_MASK) != (board[pos] & PieceData.COLOR_MASK)) {
                if (!PieceHandler.kingCheck(board, move, board[target] & PieceData.COLOR_MASK)) {
                    possible_moves.add(move);
                }
            }
        }

        return possible_moves;

    }
    
}
