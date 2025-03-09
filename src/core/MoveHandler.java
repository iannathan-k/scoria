package src.core;

import src.pieces.*;
import src.scoria.Evaluator;

public class MoveHandler {
    /* 
     * Move
     * 00000000 00000000 00000000 00000000
     *          flags    origin   target
     * 
     * flags --> double move, promotion, castle, passant
     */

    public static final int POS_MASK     = 0b00111111;
    public static final int PASSANT_MASK = 1 << 16;
    public static final int CASTLE_MASK  = 1 << 17;
    public static final int PROMO_MASK   = 1 << 18;
    public static final int DOUBLE_MASK  = 1 << 19;

    public static final int ROW_MASK = 0b00111000;

    public static byte pseudoMoveState(byte[] board, int move) {
        int origin_pos = (move >> 8) & POS_MASK;
        int target_pos = move & POS_MASK;
        byte piece = board[origin_pos];
        byte captured = board[target_pos];

        int type = piece & PieceData.TYPE_MASK;
        int color = piece & PieceData.COLOR_MASK;

        if (type == PieceData.KING) {
            PieceHandler.setKingPosition(target_pos, color);
        }

        if (type == PieceData.PAWN) {
            if ((move & PASSANT_MASK) != 0) {
                // en passant
                int passant_pos = target_pos + (color == PieceData.WHITE ? 8 : -8);
                captured = board[passant_pos];
                board[passant_pos] = PieceData.EMPTY;
            } else if ((move & PROMO_MASK) != 0) {
                // promotion logic
                board[target_pos] = (byte) (PieceData.QUEEN | color);
                board[origin_pos] = PieceData.EMPTY;
                return captured;
            }    
        }

        board[target_pos] = piece;
        board[origin_pos] = PieceData.EMPTY;
        return captured;
    }

    public static void pseudoUndoState(byte[] board, int move, byte captured) {
        int origin_pos = (move >> 8) & POS_MASK;
        int target_pos = move & POS_MASK;
        byte piece = board[target_pos];

        int type = piece & PieceData.TYPE_MASK;
        int color = piece & PieceData.COLOR_MASK;

        if (type == PieceData.KING) {
            PieceHandler.setKingPosition(origin_pos, color);
        }

        if ((move & PASSANT_MASK) != 0) {
            // unpassant logic
            int passant_pos = target_pos + (color == PieceData.WHITE ? 8 : -8);
            board[passant_pos] = captured;
            captured = PieceData.EMPTY;
        } else if ((move & PROMO_MASK) != 0) {
            // promotion logic
            board[origin_pos] = (byte) (PieceData.PAWN | color);
            board[target_pos] = captured;
            return;
        }

        board[origin_pos] = piece;
        board[target_pos] = captured;
    }

    public static byte moveState(byte[] board, int move, long hash) {
        Game.nextMoveNumber();
        Evaluator.incrementPositionTable(hash);
        PieceHandler.clearPassantRights();

        // add a line to automatically push an empty on the passant 

        int origin_pos = (move >> 8) & POS_MASK;
        int target_pos = move & POS_MASK;
        byte piece = board[origin_pos];
        byte captured = board[target_pos];

        int type = piece & PieceData.TYPE_MASK;
        int color = piece & PieceData.COLOR_MASK;

        // Note to self, recheck whether these conditions can be used as such.
        // moved logic
        if (type == PieceData.ROOK) {

        } else if (type == PieceData.KING) {
            PieceHandler.setKingPosition(target_pos, color);
        }
        
        if (type == PieceData.PAWN) {
            if ((move & DOUBLE_MASK) != 0 ) {
                // passant setting logic
                PieceHandler.popPassantRights();
                PieceHandler.setPassantRights(origin_pos & 7, color);
            } else if ((move & PASSANT_MASK) != 0) {
                // en passant
                int passant_pos = target_pos + (color == PieceData.WHITE ? 8 : -8);
                captured = board[passant_pos];
                board[passant_pos] = PieceData.EMPTY;
            } else if ((move & PROMO_MASK) != 0) {
                // promotion logic
                board[target_pos] = (byte) (PieceData.QUEEN | color);
                board[origin_pos] = PieceData.EMPTY;
                return captured;
            }

        } else if ((move & CASTLE_MASK) != 0) {
            // castle logic
            int rook_col = (target_pos > origin_pos) ? 7 : 0;
            int rook_pos = (origin_pos & ROW_MASK) | rook_col; // recheck this part as well
            captured = board[rook_pos];

            // move rook
            int rook_offset = (target_pos > origin_pos) ? -1 : 1; // and this part
            board[rook_pos] = PieceData.EMPTY;
            board[(origin_pos & ROW_MASK) + rook_offset] = captured;

            // Update Rights
            int side = (origin_pos & 7) % 2;
            int offset = (color == PieceData.WHITE) ? 0 : 2;
            PieceHandler.setCastleRights(side + offset);
        }

        board[target_pos] = piece;
        board[origin_pos] = PieceData.EMPTY;
        return captured;
    }

    public static void undoState(byte[] board, int move, byte captured, long hash) {
        Game.lastMoveNumber();
        Evaluator.decrementPositionTable(hash);
        PieceHandler.popPassantRights();

        // add a line to automatically pop the empty from passant

        int origin_pos = (move >> 8) & POS_MASK;
        int target_pos = move & POS_MASK;
        byte piece = board[target_pos];

        int type = piece & PieceData.TYPE_MASK;
        int color = piece & PieceData.COLOR_MASK;

        // unmoved logic for castling rights
        // definitely go over this
        if (type == PieceData.ROOK) {

        } else if (type == PieceData.KING) {
            PieceHandler.setKingPosition(origin_pos, color);
        }
        
        if ((move & PASSANT_MASK) != 0) {
            // unpassant logic
            int passant_pos = target_pos + (color == PieceData.WHITE ? 8 : -8);
            board[passant_pos] = captured;
            captured = PieceData.EMPTY;
        } else if ((move & PROMO_MASK) != 0) {
            // promotion logic
            board[origin_pos] = (byte) (PieceData.PAWN | color);
            board[target_pos] = captured;
            return;
        } else if ((move & CASTLE_MASK) != 0) {
            // uncastle logic
            int rook_col = (target_pos > origin_pos) ? 7 : 0;
            int rook_pos = (origin_pos & ROW_MASK) | rook_col; // recheck this part as well

            // unmove rook
            int rook_offset = (target_pos > origin_pos) ? -1 : 1; // and this part
            board[rook_pos] = captured;
            board[(origin_pos & ROW_MASK) + rook_offset] = PieceData.EMPTY;
            captured = PieceData.EMPTY;

            // Update Rights
            int side = (origin_pos & 7) % 2;
            int offset = (color == PieceData.WHITE) ? 0 : 2;
            PieceHandler.removeCastleRights(side + offset); // and definitely this
        }

        board[origin_pos] = piece;
        board[target_pos] = captured;
    }
}