package src.game;

import src.utils.GameStack;
import src.utils.GameState;

public class MoveHandler {
    /* 
     * Move
     * 00000000000 000000000 000000 000000
     *             flags     origin target
     * 
     * flags --> double move, en passant, castle K, castle Q,
     *           promo N, promo B, promo R, promo Q
     */

    private static final long SQUARE_F1_MAP = 1L << MoveGenerator.SQUARE_F1;
    private static final long SQUARE_H1_MAP = 1L << MoveGenerator.SQUARE_H1;
    private static final long SQUARE_D1_MAP = 1L << MoveGenerator.SQUARE_D1;
    private static final long SQUARE_A1_MAP = 1L << MoveGenerator.SQUARE_A1;
    private static final long SQUARE_F8_MAP = 1L << MoveGenerator.SQUARE_F8;
    private static final long SQUARE_H8_MAP = 1L << MoveGenerator.SQUARE_H8;
    private static final long SQUARE_D8_MAP = 1L << MoveGenerator.SQUARE_D8;
    private static final long SQUARE_A8_MAP = 1L << MoveGenerator.SQUARE_A8;

    public static final int POSITION_MASK   = 0x3F;
    public static final int PIECE_MASK      = 0xF;
    public static final int PROMOTED_MASK   = 0xF << 16;
    public static final int DOUBLE_FLAG     = 1 << 20;
    public static final int PASSANT_FLAG    = 1 << 21;
    public static final int CASTLE_FLAG     = 1 << 22;
    
    // The implementation for getPieceAt() and getPieceColor()
    // May be quite slow as they both make use of loops
    // Pseudo does not need Promotion, as it is no different for checks than a pawn
    // I.E only blocking or pawn capture can happen, which pawn is doing
    public static void doPseudoMove(int move) {
        int origin = (move >> 6) & POSITION_MASK;
        int target = move & POSITION_MASK;
        int piece = (move >> 12) & PIECE_MASK;
        int moving_side = BitBoard.getPieceColor(piece);
        int capture = BitBoard.getPieceAt(target, moving_side ^ 1);

        // En Passant
        if ((move & PASSANT_FLAG) != 0) {
            int passant_capture_square = (moving_side == BitBoard.WHITE) ? target - 8 : target + 8;
            capture = BitBoard.getPieceAt(passant_capture_square, moving_side ^ 1);
            long passant_capture_mask = 1L << passant_capture_square;
            BitBoard.piece_bitboards[capture] &= ~passant_capture_mask;
            BitBoard.color_bitboards[moving_side ^ 1] &= ~passant_capture_mask;
            BitBoard.occupancy_bitboard &= ~passant_capture_mask;
        }

        // Fix the zobrist hash later
        GameStack.push(move, moving_side, BitBoard.castle_rights, BitBoard.passant_rights, capture, -1);

        // Remove piece from origin
        long origin_mask = 1L << origin;
        BitBoard.piece_bitboards[piece] &= ~origin_mask;
        BitBoard.color_bitboards[moving_side] &= ~origin_mask;
        BitBoard.occupancy_bitboard &= ~origin_mask;

        // Place piece at target
        long target_mask = 1L << target;
        BitBoard.piece_bitboards[piece] |= target_mask;
        BitBoard.color_bitboards[moving_side] |= target_mask;
        BitBoard.occupancy_bitboard |= target_mask;

        // Remove capture from target
        if (capture != -1) {
            BitBoard.piece_bitboards[capture] &= ~target_mask;
            BitBoard.color_bitboards[moving_side ^ 1] &= ~target_mask;
        }
    }

     public static void undoPseudoMove() {
        GameState state = GameStack.pop();
        int origin = (state.next_move >> 6) & POSITION_MASK;
        int target = state.next_move & POSITION_MASK;
        int piece = (state.next_move >> 12) & PIECE_MASK;

        // En Passant
        if ((state.next_move & PASSANT_FLAG) != 0) {
            int passant_capture_square = (state.moving_side == BitBoard.WHITE) ? target - 8 : target + 8;
            long passant_capture_mask = 1L << passant_capture_square;
            BitBoard.piece_bitboards[state.captured_piece] |= passant_capture_mask;
            BitBoard.color_bitboards[state.moving_side ^ 1] |= passant_capture_mask;
            BitBoard.occupancy_bitboard |= passant_capture_mask;
            state.captured_piece = -1;
        }

        // Remove piece from target
        long target_mask = 1L << target;
        BitBoard.piece_bitboards[piece] &= ~target_mask;
        BitBoard.color_bitboards[state.moving_side] &= ~target_mask;
        BitBoard.occupancy_bitboard &= ~target_mask;

        // Replace piece at origin
        long origin_mask = 1L << origin;
        BitBoard.piece_bitboards[piece] |= origin_mask;
        BitBoard.color_bitboards[state.moving_side] |= origin_mask;
        BitBoard.occupancy_bitboard |= origin_mask;

        // Replace capture at target
        if (state.captured_piece != -1) {
            BitBoard.piece_bitboards[state.captured_piece] |= target_mask;
            BitBoard.color_bitboards[state.moving_side ^ 1] |= target_mask;
            BitBoard.occupancy_bitboard |= target_mask;
        }
    }

    public static void doMove(int move) {
        int origin = (move >> 6) & POSITION_MASK;
        int target = move & POSITION_MASK;
        int piece = (move >> 12) & PIECE_MASK;
        int moving_side = BitBoard.getPieceColor(piece);
        int capture = BitBoard.getPieceAt(target, moving_side ^ 1);

        Zobrist.updateZobristHash(move);

        // En Passant
        if ((move & PASSANT_FLAG) != 0) {
            int passant_capture_square = (moving_side == BitBoard.WHITE) ? target - 8 : target + 8;
            capture = BitBoard.getPieceAt(passant_capture_square, moving_side ^ 1);
            long passant_capture_mask = 1L << passant_capture_square;
            BitBoard.piece_bitboards[capture] &= ~passant_capture_mask;
            BitBoard.color_bitboards[moving_side ^ 1] &= ~passant_capture_mask;
            BitBoard.occupancy_bitboard &= ~passant_capture_mask;
        }

        // Fix the zobrist hash later
        GameStack.push(move, moving_side, BitBoard.castle_rights, BitBoard.passant_rights, capture, -1);

        // Clear Passant Rights
        BitBoard.passant_rights = -1;

        // Remove piece from origin
        long origin_mask = 1L << origin;
        BitBoard.piece_bitboards[piece] &= ~origin_mask;
        BitBoard.color_bitboards[moving_side] &= ~origin_mask;
        BitBoard.occupancy_bitboard &= ~origin_mask;

        // Place piece at target
        long target_mask = 1L << target;
        BitBoard.piece_bitboards[piece] |= target_mask;
        BitBoard.color_bitboards[moving_side] |= target_mask;
        BitBoard.occupancy_bitboard |= target_mask;

        if (capture != -1) {
            // Captures
            BitBoard.piece_bitboards[capture] &= ~target_mask;
            BitBoard.color_bitboards[moving_side ^ 1] &= ~target_mask;
        }

         if ((move & DOUBLE_FLAG) != 0) {
            BitBoard.passant_rights = (moving_side == BitBoard.WHITE) ? target - 8 : target + 8;
        }
        
        if ((move & CASTLE_FLAG) != 0) {
            // Castling
            if (target == MoveGenerator.SQUARE_G1) {
                BitBoard.piece_bitboards[BitBoard.WHITE_ROOK] |= SQUARE_F1_MAP;
                BitBoard.piece_bitboards[BitBoard.WHITE_ROOK] &= ~SQUARE_H1_MAP;
                BitBoard.color_bitboards[BitBoard.WHITE] |= SQUARE_F1_MAP;
                BitBoard.color_bitboards[BitBoard.WHITE] &= ~SQUARE_H1_MAP;
                BitBoard.occupancy_bitboard |= SQUARE_F1_MAP;
                BitBoard.occupancy_bitboard &= ~SQUARE_H1_MAP;
            } else if (target == MoveGenerator.SQUARE_C1) {
                BitBoard.piece_bitboards[BitBoard.WHITE_ROOK] |= SQUARE_D1_MAP;
                BitBoard.piece_bitboards[BitBoard.WHITE_ROOK] &= ~SQUARE_A1_MAP;
                BitBoard.color_bitboards[BitBoard.WHITE] |= SQUARE_D1_MAP;
                BitBoard.color_bitboards[BitBoard.WHITE] &= ~SQUARE_A1_MAP;
                BitBoard.occupancy_bitboard |= SQUARE_D1_MAP;
                BitBoard.occupancy_bitboard &= ~SQUARE_A1_MAP;
            } else if (target == MoveGenerator.SQUARE_G8) {
                BitBoard.piece_bitboards[BitBoard.BLACK_ROOK] |= SQUARE_F8_MAP;
                BitBoard.piece_bitboards[BitBoard.BLACK_ROOK] &= ~SQUARE_H8_MAP;
                BitBoard.color_bitboards[BitBoard.BLACK] |= SQUARE_F8_MAP;
                BitBoard.color_bitboards[BitBoard.BLACK] &= ~SQUARE_H8_MAP;
                BitBoard.occupancy_bitboard |= SQUARE_F8_MAP;
                BitBoard.occupancy_bitboard &= ~SQUARE_H8_MAP;
            } else {
                BitBoard.piece_bitboards[BitBoard.BLACK_ROOK] |= SQUARE_D8_MAP;
                BitBoard.piece_bitboards[BitBoard.BLACK_ROOK] &= ~SQUARE_A8_MAP;
                BitBoard.color_bitboards[BitBoard.BLACK] |= SQUARE_D8_MAP;
                BitBoard.color_bitboards[BitBoard.BLACK] &= ~SQUARE_A8_MAP;
                BitBoard.occupancy_bitboard |= SQUARE_D8_MAP;
                BitBoard.occupancy_bitboard &= ~SQUARE_A8_MAP;
            }
        }
        
        if ((move & PROMOTED_MASK) != 0) {
            int promoted_piece = (move & PROMOTED_MASK) >>> 16;
            BitBoard.piece_bitboards[piece] &= ~target_mask;
            BitBoard.piece_bitboards[promoted_piece] |= target_mask;
        }

        // Castle Rights
        if (origin == MoveGenerator.SQUARE_H1 || target == MoveGenerator.SQUARE_H1) {
            BitBoard.castle_rights &= ~BitBoard.WHITE_KING_ROOK_MASK;
        } else if (origin == MoveGenerator.SQUARE_A1 || target == MoveGenerator.SQUARE_A1) {
            BitBoard.castle_rights &= ~BitBoard.WHITE_QUEEN_ROOK_MASK;
        } else if (origin == MoveGenerator.SQUARE_H8 || target == MoveGenerator.SQUARE_H8) {
            BitBoard.castle_rights &= ~BitBoard.BLACK_KING_ROOK_MASK;
        } else if (origin == MoveGenerator.SQUARE_A8 || target == MoveGenerator.SQUARE_A8) {
            BitBoard.castle_rights &= ~BitBoard.BLACK_QUEEN_ROOK_MASK;
        } else if (origin == MoveGenerator.SQUARE_E1) {
            BitBoard.castle_rights &= ~BitBoard.WHITE_KING_CASTLE_MASK;
        } else if (origin == MoveGenerator.SQUARE_E8) {
            BitBoard.castle_rights &= ~BitBoard.BLACK_KING_CASTLE_MASK;
        }
    }

    public static void undoMove() {
        GameState state = GameStack.pop();
        int origin = (state.next_move >> 6) & POSITION_MASK;
        int target = state.next_move & POSITION_MASK;
        int piece = (state.next_move >> 12) & PIECE_MASK;

        // En Passant
        if ((state.next_move & PASSANT_FLAG) != 0) {
            int passant_capture_square = (state.moving_side == BitBoard.WHITE) ? target - 8 : target + 8;
            long passant_capture_mask = 1L << passant_capture_square;
            BitBoard.piece_bitboards[state.captured_piece] |= passant_capture_mask;
            BitBoard.color_bitboards[state.moving_side ^ 1] |= passant_capture_mask;
            BitBoard.occupancy_bitboard |= passant_capture_mask;
            state.captured_piece = -1;
        }

        // Remove piece from target
        long target_mask = 1L << target;
        BitBoard.piece_bitboards[piece] &= ~target_mask;
        BitBoard.color_bitboards[state.moving_side] &= ~target_mask;
        BitBoard.occupancy_bitboard &= ~target_mask;

        // Replace piece at origin
        long origin_mask = 1L << origin;
        BitBoard.piece_bitboards[piece] |= origin_mask;
        BitBoard.color_bitboards[state.moving_side] |= origin_mask;
        BitBoard.occupancy_bitboard |= origin_mask;

        if (state.captured_piece != -1) {
            // Uncapturing
            BitBoard.piece_bitboards[state.captured_piece] |= target_mask;
            BitBoard.color_bitboards[state.moving_side ^ 1] |= target_mask;
            BitBoard.occupancy_bitboard |= target_mask;
        } 
        
        if ((state.next_move & CASTLE_FLAG) != 0) {
            // Uncastling
            if (target == MoveGenerator.SQUARE_G1) {
                BitBoard.piece_bitboards[BitBoard.WHITE_ROOK] |= SQUARE_H1_MAP;
                BitBoard.piece_bitboards[BitBoard.WHITE_ROOK] &= ~SQUARE_F1_MAP;
                BitBoard.color_bitboards[BitBoard.WHITE] |= SQUARE_H1_MAP;
                BitBoard.color_bitboards[BitBoard.WHITE] &= ~SQUARE_F1_MAP;
                BitBoard.occupancy_bitboard |= SQUARE_H1_MAP;
                BitBoard.occupancy_bitboard &= ~SQUARE_F1_MAP;
            } else if (target == MoveGenerator.SQUARE_C1) {
                BitBoard.piece_bitboards[BitBoard.WHITE_ROOK] |= SQUARE_A1_MAP;
                BitBoard.piece_bitboards[BitBoard.WHITE_ROOK] &= ~SQUARE_D1_MAP;
                BitBoard.color_bitboards[BitBoard.WHITE] |= SQUARE_A1_MAP;
                BitBoard.color_bitboards[BitBoard.WHITE] &= ~SQUARE_D1_MAP;
                BitBoard.occupancy_bitboard |= SQUARE_A1_MAP;
                BitBoard.occupancy_bitboard &= ~SQUARE_D1_MAP;
            } else if (target == MoveGenerator.SQUARE_G8) {
                BitBoard.piece_bitboards[BitBoard.BLACK_ROOK] |= SQUARE_H8_MAP;
                BitBoard.piece_bitboards[BitBoard.BLACK_ROOK] &= ~SQUARE_F8_MAP;
                BitBoard.color_bitboards[BitBoard.BLACK] |= SQUARE_H8_MAP;
                BitBoard.color_bitboards[BitBoard.BLACK] &= ~SQUARE_F8_MAP;
                BitBoard.occupancy_bitboard |= SQUARE_H8_MAP;
                BitBoard.occupancy_bitboard &= ~SQUARE_F8_MAP;
            } else {
                BitBoard.piece_bitboards[BitBoard.BLACK_ROOK] |= SQUARE_A8_MAP;
                BitBoard.piece_bitboards[BitBoard.BLACK_ROOK] &= ~SQUARE_D8_MAP;
                BitBoard.color_bitboards[BitBoard.BLACK] |= SQUARE_A8_MAP;
                BitBoard.color_bitboards[BitBoard.BLACK] &= ~SQUARE_D8_MAP;
                BitBoard.occupancy_bitboard |= SQUARE_A8_MAP;
                BitBoard.occupancy_bitboard &= ~SQUARE_D8_MAP;
            }
        } 
        
        if ((state.next_move & PROMOTED_MASK) != 0) {
            int promoted_piece = (state.next_move & PROMOTED_MASK) >>> 16;
            BitBoard.piece_bitboards[promoted_piece] &= ~target_mask;
        }

        BitBoard.castle_rights = state.castle_rights;
        BitBoard.passant_rights = state.passant_rights;

        Zobrist.updateZobristHash(state.next_move);
    }
}