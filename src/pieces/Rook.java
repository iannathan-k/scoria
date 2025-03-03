package src.pieces;

import java.util.ArrayList;

public abstract class Rook {

    public static ArrayList<Integer> getMoves(byte[] board, int pos) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();
        int color = board[pos] & PieceData.COLOR_MASK;

        for (int[] dir : PieceData.ROOK_DIRECTIONS) {
            int row = (pos >> 3) + dir[0];
            int col = (pos & 7) + dir[1];
            while (PieceHandler.inRange(row, col)) {
                int target = row << 3 | col;
                int move = pos << 8 | target;
                byte target_piece = board[target];
                if (target_piece == PieceData.EMPTY) {
                    if (!PieceHandler.kingCheck(board, move, color)) {
                        possible_moves.add(move);
                    }
                } else {
                    if ((target_piece & PieceData.COLOR_MASK) == color) {
                        break;
                    }
                    if (!PieceHandler.kingCheck(board, move, color)) {
                        possible_moves.add(move);
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
