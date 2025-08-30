package src.game;

import src.utils.MoveList;

public class MoveGenerator {
    public static MoveList generateAllMoves(int color) {
        MoveList move_list = new MoveList(20);
        int offset = (color == BitBoard.WHITE) ? 0 : 6;

        // Pawn
        long pawn_board = BitBoard.piece_bitboards[BitBoard.WHITE_PAWN + offset];

        long single_push = 0L;
        long double_push = 0L;
        long left_capture = 0L;
        long right_capture = 0L;
        if (color == BitBoard.WHITE) {
            single_push = (pawn_board << 8) & ~BitBoard.occupancy_bitboard;
            double_push = ((single_push & BitBoard.ROW_3) << 8) & ~BitBoard.occupancy_bitboard;
            left_capture  = (pawn_board << 7) & BitBoard.occupancy_bitboard & ~BitBoard.color_bitboards[color] & ~BitBoard.COL_H;
            right_capture = (pawn_board << 9) & BitBoard.occupancy_bitboard & ~BitBoard.color_bitboards[color] & ~BitBoard.COL_A;
        } else {
            single_push = (pawn_board >>> 8) & ~BitBoard.occupancy_bitboard;
            double_push = ((single_push & BitBoard.ROW_6) >>> 8) & ~BitBoard.occupancy_bitboard;
            left_capture  = (pawn_board >>> 9) & BitBoard.occupancy_bitboard & ~BitBoard.color_bitboards[color] & ~BitBoard.COL_H;
            right_capture = (pawn_board >>> 7) & BitBoard.occupancy_bitboard & ~BitBoard.color_bitboards[color] & ~BitBoard.COL_A;
        }

        while (single_push != 0L) {
            int target = Long.numberOfTrailingZeros(single_push);
            int origin = (color == BitBoard.WHITE) ? target - 8 : target + 8;

            // promotion check can be added here later
            
            move_list.add(origin << 6 | target);
            single_push &= single_push - 1;
        }

        while (double_push != 0L) {
            int target = Long.numberOfTrailingZeros(double_push);
            int origin = (color == BitBoard.WHITE) ? target - 16 : target + 16;
            move_list.add(origin << 6 | target);
            double_push &= double_push - 1;
        }

        while (left_capture != 0L) {
            int target = Long.numberOfTrailingZeros(left_capture);
            int origin = (color == BitBoard.WHITE) ? target - 7 : target + 7;
            move_list.add(origin << 6 | target);
            left_capture &= left_capture - 1;
        }

        while (right_capture != 0L) {
            int target = Long.numberOfTrailingZeros(right_capture);
            int origin = (color == BitBoard.WHITE) ? target - 9 : target + 9;
            move_list.add(origin << 6 | target);
            right_capture &= right_capture - 1;
        }

        // Knight
        long knight_board = BitBoard.piece_bitboards[BitBoard.WHITE_KNIGHT + offset];
        while (knight_board != 0L) {
            int square = Long.numberOfTrailingZeros(knight_board);
            long moves = Precomputer.KNIGHT_MOVE_TABLE[square] & ~BitBoard.color_bitboards[color];

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                move_list.add(square << 6 | target);
                moves &= moves - 1;
            }

            knight_board &= knight_board - 1;
        }

        // King
        long king_board = BitBoard.piece_bitboards[BitBoard.WHITE_KING + offset];
        while (king_board != 0L) {
            int square = Long.numberOfTrailingZeros(king_board);
            long moves = Precomputer.KING_MOVE_TABLE[square] & ~BitBoard.color_bitboards[color];

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                move_list.add(square << 6 | target);
                moves &= moves - 1;
            }
            
            king_board &= king_board - 1;
        }

        // Bishop
        long bishop_board = BitBoard.piece_bitboards[BitBoard.WHITE_BISHOP + offset];
        while (bishop_board != 0L) {
            int square = Long.numberOfTrailingZeros(bishop_board);
            int index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.BISHOP_MASKS[square]) 
                * Precomputer.BISHOP_MAGIC_NUMBERS[square]) 
                >>> Precomputer.BISHOP_SHIFTS[square]);
            long moves = Precomputer.BISHOP_MOVE_TABLE[square][index] & ~BitBoard.color_bitboards[color];

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                move_list.add(square << 6 | target);
                moves &= moves - 1;
            }

            bishop_board &= bishop_board - 1;
        }

        // Rook
        long rook_board = BitBoard.piece_bitboards[BitBoard.WHITE_ROOK + offset];
        while (rook_board != 0L) {
            int square = Long.numberOfTrailingZeros(rook_board);
            int index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.ROOK_MASKS[square]) 
                * Precomputer.ROOK_MAGIC_NUMBERS[square]) 
                >>> Precomputer.ROOK_SHIFTS[square]);
            long moves = Precomputer.ROOK_MOVE_TABLE[square][index] & ~BitBoard.color_bitboards[color];

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                move_list.add(square << 6 | target);
                moves &= moves - 1;
            }

            rook_board &= rook_board - 1;
        }

        // Queen
        long queen_board = BitBoard.piece_bitboards[BitBoard.WHITE_QUEEN + offset];
        while (queen_board != 0L) {
            int square = Long.numberOfTrailingZeros(queen_board);

            // Diagonally
            int index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.BISHOP_MASKS[square]) 
                * Precomputer.BISHOP_MAGIC_NUMBERS[square]) 
                >>> Precomputer.BISHOP_SHIFTS[square]);
            long moves = Precomputer.BISHOP_MOVE_TABLE[square][index] & ~BitBoard.color_bitboards[color];

            // Orthogonally
            index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.ROOK_MASKS[square]) 
                * Precomputer.ROOK_MAGIC_NUMBERS[square]) 
                >>> Precomputer.ROOK_SHIFTS[square]);
            moves |= Precomputer.ROOK_MOVE_TABLE[square][index] & ~BitBoard.color_bitboards[color];

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                move_list.add(square << 6 | target);
                moves &= moves - 1;
            }

            queen_board &= queen_board - 1;
        }

        return move_list;
    }
}