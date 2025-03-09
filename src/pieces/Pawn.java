package src.pieces;

import java.util.*;

import src.core.MoveHandler;

public abstract class Pawn{

    private static boolean passantCheck(byte[] board, int color, int row, int new_row, int new_col, int passant_rights, int pos) {
        if (passant_rights != new_col + color) {
            return false;
        }

        int capture = row << 3 | new_col;
        if ((board[capture] & PieceData.TYPE_MASK) != PieceData.PAWN) {
            return false;
        }
        if ((board[capture] & PieceData.COLOR_MASK) == color) {
            return false;
        }
        
        int target = new_row << 3 | new_col;
        if (board[target] != PieceData.EMPTY) {
            return false;
        }
        
        int move = (pos << 8 | target) | MoveHandler.PASSANT_MASK;
        if (PieceHandler.kingCheck(board, move, color)) {
            return false;
        }

        return true;
    }

    public static ArrayList<Integer> getMoves(byte[] board, int pos) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();

        int row = pos >> 3;
        int col = pos & 7;
        int color = board[pos] & PieceData.COLOR_MASK;
        int dir = (color == PieceData.WHITE) ? -1 : 1;

        for (int i = 1; i < 3; i++) {
            int new_row = row + dir * i;
            if (!PieceHandler.inRange(new_row, col)) {
                continue;
            }

            int target = new_row << 3 | col; 
            int move = pos << 8 | target; 
            if (board[target] != PieceData.EMPTY) {
                continue;
            }
            if (PieceHandler.kingCheck(board, move, color)) {
                continue;
            }

            if (i == 1) {
                // promotion flag
                if (new_row == 0 || new_row == 7) {
                    move |= MoveHandler.PROMO_MASK;
                }
                possible_moves.add(move);
                continue;
            }

            // double move
            if (board[row + dir << 3 | col] != PieceData.EMPTY) {
                continue;
            }
            if (row == 1 || row == 6) {
                move |= MoveHandler.DOUBLE_MASK;
                possible_moves.add(move);
            }
        }

        // capture moves & passant
        int passant_rights = PieceHandler.peekPassantRights();
        int new_row = row + dir;

        // -1 for left, 1 for right
        for (int i = -1; i < 2; i+= 2) {
            int new_col = col + i;
            if (!PieceHandler.inRange(new_row, new_col)) {
                continue;
            }

            // sneaky passant
            int target = new_row << 3 | new_col;
            int move = pos << 8 | target;
            if (passantCheck(board, color, row, new_row, new_col, passant_rights, pos)) {
                possible_moves.add(move | MoveHandler.PASSANT_MASK);
                continue;
            }

            byte piece = board[target];
            if (piece == PieceData.EMPTY) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) == color) {
                continue;
            }
            if (!PieceHandler.kingCheck(board, move, color)) {
                possible_moves.add(move);
            }
        }

        return possible_moves;
    }
}
