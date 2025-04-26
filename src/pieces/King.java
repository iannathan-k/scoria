package src.pieces;

import java.util.*;

import src.core.MoveHandler;

public abstract class King {

    public static boolean canCastle(byte[] board, int side, int color, int pos, int dir) {
        int offset = (color == PieceData.WHITE) ? 2 : 0;

        if ((PieceHandler.peekCastleRights() & (side << offset)) == 0) {
            return false;
        }

        int row = pos & MoveHandler.ROW_MASK;
        int col = (pos & 7) + dir;

        while (col > 0 && col < 7) {
            if (board[row | col] != PieceData.EMPTY) {
                return false;
            }
            if (PieceHandler.underAttack(board, color, row | col) && col != 1) {
                return false;
            }
            col += dir;
        }

        if ((board[row | col] & PieceData.TYPE_MASK) != PieceData.ROOK) {
            return false;
        }

        return true;

    }

    public static ArrayList<Integer> getMoves(byte[] board, int pos, int color) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>(8);

        for (int target : PreComputer.KING_PREMOVES[pos]) {
            int move = pos << 6 | target;
            byte target_piece = board[target];

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

        if (!PieceHandler.underAttack(board, color, pos)) {
            if (canCastle(board, 0b01, color, pos, -1)) {
                possible_moves.add((pos << 6) | (pos - 2) | MoveHandler.CASTLE_MASK);
            }
            if (canCastle(board, 0b10, color, pos, 1)) {
                possible_moves.add((pos << 6) | (pos + 2) | MoveHandler.CASTLE_MASK);
            }
        }

        return possible_moves;
    }

    public static ArrayList<Integer> getCaptures(byte[] board, int pos, int color) {
        ArrayList<Integer> capture_moves = new ArrayList<Integer>(4);

        for (int target : PreComputer.KING_PREMOVES[pos]) {
            int move = pos << 6 | target;
            byte target_piece = board[target];

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
        for (int target : PreComputer.KING_PREMOVES[pos]) {
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

        if (!PieceHandler.underAttack(board, color, pos)) {
            if (canCastle(board, 0b01, color, pos, -1)) {
                return true;
            }
            if (canCastle(board, 0b10, color, pos, 1)) {
                return true;
            }
        }

        return false;
    }

    public static int getMobility(byte[] board, int pos, int color) {
        int mobility = 0;
        for (int target : PreComputer.KING_PREMOVES[pos]) {
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

        if (!PieceHandler.underAttack(board, color, pos)) {
            if (canCastle(board, 0b01, color, pos, -1)) {
                mobility++;
            }
            if (canCastle(board, 0b10, color, pos, 1)) {
                mobility++;
            }
        }

        return mobility;
    }
    
}
