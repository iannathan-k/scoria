package src.pieces;

import java.util.*;

import src.core.MoveHandler;

public abstract class Pawn{

    private static boolean passantCheck(byte[] board, int color, int pos, int target, int move, int passant_rights) {
        int new_col = target & 7;
        if (passant_rights != new_col + color) {
            return false;
        }

        int capture = (pos & MoveHandler.ROW_MASK) | new_col;
        if (board[capture] != (PieceData.PASSANT_PAWN ^ color)) {
            return false;
        }
        if (board[target & MoveHandler.POS_MASK] != PieceData.EMPTY) {
            return false;
        }
        
        return !PieceHandler.kingCheck(board, move | MoveHandler.PASSANT_MASK, color);
    }

    public static ArrayList<Integer> getMoves(byte[] board, int pos, int color) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();

        int[] straight_moves = (color == PieceData.WHITE) 
            ? PreComputer.WHITE_PAWN_PREMOVES[pos] 
            : PreComputer.BLACK_PAWN_PREMOVES[pos];

        for (int target : straight_moves) {
            int move = pos << 8 | target;
            if (board[target & MoveHandler.POS_MASK] != PieceData.EMPTY) {
                break;
            }
            if (!PieceHandler.kingCheck(board, move, color)) {
                possible_moves.add(move);
            }
        }

        int passant_rights = PieceHandler.peekPassantRights();
        int[] capture_moves = (color == PieceData.WHITE)
            ? PreComputer.WHITE_PAWN_PRECAPTURES[pos]
            : PreComputer.BLACK_PAWN_PRECAPTURES[pos];

        for (int target : capture_moves) {
            byte piece = board[target & MoveHandler.POS_MASK];
            int move = pos << 8 | target;

            if (passantCheck(board, color, pos, target, move, passant_rights)) {
                possible_moves.add(move | MoveHandler.PASSANT_MASK);
                continue;
            }

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

    public static boolean hasMove(byte[] board, int pos, int color) {
        int[] straight_moves = (color == PieceData.WHITE) 
            ? PreComputer.WHITE_PAWN_PREMOVES[pos] 
            : PreComputer.BLACK_PAWN_PREMOVES[pos];

        for (int target : straight_moves) {
            int move = pos << 8 | target;
            if (board[target & MoveHandler.POS_MASK] != PieceData.EMPTY) {
                break;
            }
            if (!PieceHandler.kingCheck(board, move, color)) {
                return true;
            }
        }

        int passant_rights = PieceHandler.peekPassantRights();
        int[] capture_moves = (color == PieceData.WHITE)
            ? PreComputer.WHITE_PAWN_PRECAPTURES[pos]
            : PreComputer.BLACK_PAWN_PRECAPTURES[pos];

        for (int target : capture_moves) {
            byte piece = board[target & MoveHandler.POS_MASK];

            if (passantCheck(board, color, pos, target, pos << 8 | target, passant_rights)) {
                return true;
            }

            if (piece == PieceData.EMPTY) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) == color) {
                continue;
            }
            if (!PieceHandler.kingCheck(board, pos << 8 | target, color)) {
                return true;
            }
        }

        return false;
    }
}
