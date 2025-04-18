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

    public static ArrayList<Integer> getCaptures(byte[] board, int pos, int color) {
        ArrayList<Integer> capture_moves = new ArrayList<Integer>();

        for (int target : PreComputer.KNIGHT_PREMOVES[pos]) {
            byte target_piece = board[target];
            int move = pos << 8 | target;

            if (target_piece == PieceData.EMPTY) {
                continue;
            }
            if ((target_piece & PieceData.COLOR_MASK) != color) {
                if (!PieceHandler.kingCheck(board, move, color)) {
                    capture_moves.add(move);
                }
            }
        }

        return capture_moves;

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

    public static int getMobility(byte[] board, int pos, int color) {
        int mobility = 0;
        for (int target : PreComputer.KNIGHT_PREMOVES[pos]) {
            byte target_piece = board[target];

            if (target_piece == PieceData.EMPTY) {
                if (!PieceHandler.kingCheck(board, pos << 8 | target, color)) {
                    mobility++;
                }
            } else if ((target_piece & PieceData.COLOR_MASK) != color) {
                if (!PieceHandler.kingCheck(board, pos << 8 | target, color)) {
                    mobility++;
                }
            }
        }

        return mobility;

    }
    
}
