package src.pieces;

import java.util.ArrayList;

public abstract class Knight {

    public static ArrayList<Integer> getMoves(byte[] board, int pos, int color) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();

        for (int target : PreComputer.KNIGHT_PREMOVES[pos]) {
            byte target_piece = board[target];
            int move = pos << 8 | target;

            if (target_piece == PieceData.EMPTY) {
                if (!PieceHandler.kingCheck(board, move, color)) {
                    possible_moves.add(move);
                }
            } else if ((target_piece & PieceData.COLOR_MASK) != color) {
                if (!PieceHandler.kingCheck(board, move, color)) {
                    possible_moves.add(move);
                }
            }
        }

        return possible_moves;

    }

    public static boolean hasMove(byte[] board, int pos, int color) {
        for (int target : PreComputer.KNIGHT_PREMOVES[pos]) {
            byte target_piece = board[target];

            if (target_piece == PieceData.EMPTY) {
                if (!PieceHandler.kingCheck(board, pos << 8 | target, color)) {
                    return true;
                }
            } else if ((target_piece & PieceData.COLOR_MASK) != color) {
                if (!PieceHandler.kingCheck(board, pos << 8 | target, color)) {
                    return true;
                }
            }
        }

        return false;

    }
    
}
