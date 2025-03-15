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

    public static ArrayList<Integer> getMoves(byte[] board, int pos) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();
        int color = board[pos] & PieceData.COLOR_MASK;

        for (int target : PreComputer.KING_PREMOVES[pos]) {
            int move = pos << 8 | target;
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

        // for (int[] dir : PieceData.ALL_DIRECTIONS) {
        //     int row = (pos >> 3) + dir[0];
        //     int col = (pos & 7) + dir[1];
        //     if (!PieceHandler.inRange(row, col)) {
        //         continue;
        //     }

        //     int target = row << 3 | col;
        //     int move = pos << 8 | target;
        //     byte target_piece = board[target];

        //     if (target_piece == PieceData.EMPTY) {
        //         if (!PieceHandler.kingCheck(board, move, color)) {
        //             possible_moves.add(move);
        //         }
        //     } else if ((target_piece & PieceData.COLOR_MASK) != color) {
        //         if (!PieceHandler.kingCheck(board, move, color)) {
        //             possible_moves.add(move);
        //         }
        //     }
        // }

        if (!PieceHandler.underAttack(board, color, pos)) {
            if (canCastle(board, 0b01, color, pos, -1)) {
                possible_moves.add((pos << 8) | (pos - 2) | MoveHandler.CASTLE_MASK);
            }
            if (canCastle(board, 0b10, color, pos, 1)) {
                possible_moves.add((pos << 8) | (pos + 2) | MoveHandler.CASTLE_MASK);
            }
        }

        return possible_moves;
    }
    
}
