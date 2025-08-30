package src.game;

import src.utils.GameStack;

public class MoveHandler {
    /* 
     * Move
     * 00000000000 000000000 000000 000000
     *             flags     origin target
     * 
     * flags --> double move, en passant, castle K, castle Q,
     *           promo N, promo B, promo R, promo Q
     */

    public static final int POSITION_MASK           = 0x3F;
    public static final int DOUBLE_MOVE_FLAG        = 1 << 12;
    public static final int EN_PASSANT_FLAG         = 1 << 13;
    public static final int CASTLE_KING_FLAG        = 1 << 14;
    public static final int CASTLE_QUEEN_FLAG       = 1 << 15;
    public static final int PROMOTION_KNIGHT_FLAG   = 1 << 16;
    public static final int PROMOTION_BISHOP_FLAG   = 1 << 17;
    public static final int PROMOTION_ROOK_FLAG     = 1 << 18;
    public static final int PROMOTION_QUEEN_FLAG    = 1 << 19;

    // The implementation for getPieceAt() and getPieceColor()
    // May be quite slow as they both make use of loops
    public static void doMove(int move) {
        int origin = (move >> 6) & POSITION_MASK;
        int target = move & POSITION_MASK;
        int piece = BitBoard.getPieceAt(origin);
        int capture = BitBoard.getPieceAt(target);
        int moving_side = BitBoard.getPieceColor(piece);

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

    public static void undoMove() {
        GameState state = GameStack.pop();
        int origin = (state.next_move >> 6) & POSITION_MASK;
        int target = state.next_move & POSITION_MASK;
        int piece = BitBoard.getPieceAt(target);

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
}