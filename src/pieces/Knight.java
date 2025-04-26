package src.pieces;

import src.utils.MoveList;

public abstract class Knight {

    public static MoveList getMoves(byte[] board, int pos, int color) {
        MoveList possible_moves = new MoveList(8);

        for (int target : PreComputer.KNIGHT_PREMOVES[pos]) {
            byte target_piece = board[target];
            int move = pos << 6 | target;

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

    public static MoveList getCaptures(byte[] board, int pos, int color) {
       MoveList capture_moves = new MoveList(4);

        for (int target : PreComputer.KNIGHT_PREMOVES[pos]) {
            byte target_piece = board[target];
            int move = pos << 6 | target;

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
                if (!PieceHandler.kingCheck(board, pos << 6 | target, color)) {
                    return true;
                }
            } else if ((target_piece & PieceData.COLOR_MASK) != color) {
                if (!PieceHandler.kingCheck(board, pos << 6 | target, color)) {
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
                if (!PieceHandler.kingCheck(board, pos << 6 | target, color)) {
                    mobility++;
                }
            } else if ((target_piece & PieceData.COLOR_MASK) != color) {
                if (!PieceHandler.kingCheck(board, pos << 6 | target, color)) {
                    mobility++;
                }
            }
        }

        return mobility;

    }
    
}
