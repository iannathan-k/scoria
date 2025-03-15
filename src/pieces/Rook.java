package src.pieces;

import java.util.ArrayList;

public abstract class Rook {

    public static ArrayList<Integer> getMoves(byte[] board, int pos) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();
        int color = board[pos] & PieceData.COLOR_MASK;

        for (int[] dir : PreComputer.ROOK_PREMOVES[pos]) {
            for (int target : dir) {
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
            }
        }

        return possible_moves;
    }
    
}
