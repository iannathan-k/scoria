package src.pieces;

import java.util.ArrayList;

public abstract class Bishop {

    public static ArrayList<Integer> getMoves(byte[] board, int pos) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();

        for (int[] dir : PieceData.BISHOP_DIRECTIONS) {
            int row = (pos >> 3) + dir[0];
            int col = (pos & 7) + dir[1];
            while (PieceHandler.inRange(row, col)) {
                int target = row << 3 | col;
                if (board[target] == PieceData.EMPTY) {
                    if (!PieceHandler.kingCheck(board, pos, target)) {
                        possible_moves.add(pos << 8 | target);
                    }
                } else if ((board[target] & PieceData.COLOR_MASK) == (board[pos] & PieceData.COLOR_MASK)) {
                    break;
                } else {
                    if (!PieceHandler.kingCheck(board, pos, target)) {
                        possible_moves.add(pos << 8 | target);
                    }
                    break;
                }

                row += dir[0];
                col += dir[1];
            }
        }

        return possible_moves;
    }
    
}
