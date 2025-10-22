package src.game;

import src.utils.MoveList;

public class MoveGenerator {

    public static final int SQUARE_A1 = 0;
    public static final int SQUARE_C1 = 2;
    public static final int SQUARE_D1 = 3;
    public static final int SQUARE_E1 = 4;
    public static final int SQUARE_F1 = 5;
    public static final int SQUARE_G1 = 6;
    public static final int SQUARE_H1 = 7;
    public static final int SQUARE_A8 = 56;
    public static final int SQUARE_C8 = 58;
    public static final int SQUARE_D8 = 59;
    public static final int SQUARE_E8 = 60;
    public static final int SQUARE_F8 = 61;
    public static final int SQUARE_G8 = 62;
    public static final int SQUARE_H8 = 63;

    private static final int SQUARE_B1 = 1;
    private static final int SQUARE_B8 = 57;
    
    public static int encodeMove(int origin, int target, int piece) {
        return (piece << 12) | (origin << 6) | target;
    }

    public static int encodeMove(int origin, int target, int piece, int flag) {
        return flag | (piece << 12) | (origin << 6) | target;
    }

    public static boolean isUnderAttack(int pos, int color) {
        int enemy_offset = (color ^ 1) * 6;

        // Slider Attacks

        // Queen
        long enemy_queen_board = BitBoard.piece_bitboards[BitBoard.WHITE_QUEEN + enemy_offset];

        // Bishop
        long enemy_bishop_board = BitBoard.piece_bitboards[BitBoard.WHITE_BISHOP + enemy_offset];
        int index = (int) (((
            BitBoard.occupancy_bitboard 
            & Precomputer.BISHOP_MASKS[pos]) 
            * Precomputer.BISHOP_MAGIC_NUMBERS[pos]) 
            >>> Precomputer.BISHOP_SHIFTS[pos]);
        long moves = Precomputer.BISHOP_MOVE_TABLE[pos][index] & ~BitBoard.color_bitboards[color];

        if ((moves & (enemy_bishop_board | enemy_queen_board)) != 0) return true;

        // Rook
        long enemy_rook_board = BitBoard.piece_bitboards[BitBoard.WHITE_ROOK + enemy_offset];
        index = (int) (((
            BitBoard.occupancy_bitboard 
            & Precomputer.ROOK_MASKS[pos]) 
            * Precomputer.ROOK_MAGIC_NUMBERS[pos]) 
            >>> Precomputer.ROOK_SHIFTS[pos]);
        moves = Precomputer.ROOK_MOVE_TABLE[pos][index] & ~BitBoard.color_bitboards[color];

        if ((moves & (enemy_rook_board | enemy_queen_board)) != 0) return true;

        // Knight Attacks

        long enemy_knight_board = BitBoard.piece_bitboards[BitBoard.WHITE_KNIGHT + enemy_offset];
        if ((enemy_knight_board & Precomputer.KNIGHT_MOVE_TABLE[pos]) != 0) return true;

        // Pawn Attacks

        long pawn_attacks;
        long enemy_pawn_board = BitBoard.piece_bitboards[BitBoard.WHITE_PAWN + enemy_offset];
        if (color == BitBoard.WHITE) {
            pawn_attacks = ((enemy_pawn_board >>> 7) & ~BitBoard.COL_A) 
                        | ((enemy_pawn_board >>> 9) & ~BitBoard.COL_H);
        } else {
            pawn_attacks = ((enemy_pawn_board << 7) & ~BitBoard.COL_H) 
                        | ((enemy_pawn_board << 9) & ~BitBoard.COL_A);
        }

        if ((pawn_attacks & (1L << pos)) != 0) return true;

        // King Attacks

        long enemy_king_board = BitBoard.piece_bitboards[BitBoard.WHITE_KING + enemy_offset];
        if ((enemy_king_board & Precomputer.KING_MOVE_TABLE[pos]) != 0) return true;

        return false;
    }

    public static boolean doesMoveCauseCheck(int move, int color) {
        MoveHandler.doPseudoMove(move);

        int pos = Long.numberOfTrailingZeros(BitBoard.piece_bitboards[BitBoard.WHITE_KING + color * 6]);
        boolean result = isUnderAttack(pos, color);

        MoveHandler.undoPseudoMove();

        return result;
    }

    public static MoveList generateAllMoves(int color) {
        MoveList move_list = new MoveList(20);
        int offset = color * 6; // 0 or 1

        // Pawn
        long pawn_board = BitBoard.piece_bitboards[BitBoard.WHITE_PAWN + offset];

        long single_push = 0L;
        long double_push = 0L;
        long left_capture = 0L;
        long right_capture = 0L;

        if (color == BitBoard.WHITE) {
            single_push = (pawn_board << 8) & ~BitBoard.occupancy_bitboard;
            double_push = ((single_push & BitBoard.ROW_3) << 8) & ~BitBoard.occupancy_bitboard;

            left_capture = 
                (pawn_board << 7) 
                & BitBoard.color_bitboards[BitBoard.BLACK] 
                & ~BitBoard.COL_H;

            right_capture = 
                (pawn_board << 9)
                & BitBoard.color_bitboards[BitBoard.BLACK] 
                & ~BitBoard.COL_A;

            // FIXME: Could be changed to range check SQUARE_A5 to SQUARE_H5
            if (BitBoard.passant_rights != -1) {
                long left_passant = 
                    (pawn_board << 7)
                    & (1L << BitBoard.passant_rights)
                    & ~BitBoard.COL_H;

                long right_passant =
                    (pawn_board << 9)
                    & (1L << BitBoard.passant_rights)
                    & ~BitBoard.COL_A;

                if (left_passant != 0) {
                    int target = Long.numberOfTrailingZeros(left_passant);
                    int origin = target - 7;
                    int move = encodeMove(origin, target, BitBoard.WHITE_PAWN, MoveHandler.PASSANT_FLAG);
                    
                    if (!doesMoveCauseCheck(move, BitBoard.WHITE)) {
                        move_list.add(move);
                    }
                }
                if (right_passant != 0) {
                    int target = Long.numberOfTrailingZeros(right_passant);
                    int origin = target - 9;
                    int move = encodeMove(origin, target, BitBoard.WHITE_PAWN, MoveHandler.PASSANT_FLAG);
                    
                    if (!doesMoveCauseCheck(move, BitBoard.WHITE)) {
                        move_list.add(move);
                    }
                }
            }
                
        } else {
            single_push = (pawn_board >>> 8) & ~BitBoard.occupancy_bitboard;
            double_push = ((single_push & BitBoard.ROW_6) >>> 8) & ~BitBoard.occupancy_bitboard;
            
            left_capture = 
                (pawn_board >>> 9) 
                & BitBoard.color_bitboards[BitBoard.WHITE] 
                & ~BitBoard.COL_H;

            right_capture = 
                (pawn_board >>> 7) 
                & BitBoard.color_bitboards[BitBoard.WHITE] 
                & ~BitBoard.COL_A;

            if (BitBoard.passant_rights != 0) {
                long left_passant =
                    (pawn_board >>> 9)
                    & (1L << BitBoard.passant_rights)
                    & ~BitBoard.COL_H;
                
                long right_passant =
                    (pawn_board >>> 7)
                    & (1L << BitBoard.passant_rights)
                    & ~BitBoard.COL_A;

                if (left_passant != 0) {
                    int target = Long.numberOfTrailingZeros(left_passant);
                    int origin = target + 9;
                    int move = encodeMove(origin, target, BitBoard.BLACK_PAWN, MoveHandler.PASSANT_FLAG);

                    if (!doesMoveCauseCheck(move, BitBoard.BLACK)) {
                        move_list.add(move);
                    }
                }
                if (right_passant != 0) {
                    int target = Long.numberOfTrailingZeros(right_passant);
                    int origin = target + 7;
                    int move = encodeMove(origin, target, BitBoard.BLACK_PAWN, MoveHandler.PASSANT_FLAG);

                    if (!doesMoveCauseCheck(move, BitBoard.BLACK)) {
                        move_list.add(move);
                    }
                }
            }
        }

        while (single_push != 0L) {
            int target = Long.numberOfTrailingZeros(single_push);
            int origin = (color == BitBoard.WHITE) ? target - 8 : target + 8;

            if (target >= SQUARE_A8 || target <= SQUARE_H1) {
                int move = encodeMove(origin, target, BitBoard.WHITE_PAWN + offset);
                if (!doesMoveCauseCheck(move, color)) {
                    move_list.add(move | (BitBoard.WHITE_QUEEN + offset << 16));
                    move_list.add(move | (BitBoard.WHITE_ROOK + offset << 16));
                    move_list.add(move | (BitBoard.WHITE_BISHOP + offset << 16));
                    move_list.add(move | (BitBoard.WHITE_KNIGHT + offset << 16));
                }
            } else {
                int move = encodeMove(origin, target, BitBoard.WHITE_PAWN + offset);
                if (!doesMoveCauseCheck(move, color)) {
                    move_list.add(move);
                }
            }
           
            single_push &= single_push - 1;
        }

        while (double_push != 0L) {
            int target = Long.numberOfTrailingZeros(double_push);
            int origin = (color == BitBoard.WHITE) ? target - 16 : target + 16;
            
            int move = encodeMove(origin, target, BitBoard.WHITE_PAWN + offset, MoveHandler.DOUBLE_FLAG);
            if (!doesMoveCauseCheck(move, color)) {
                move_list.add(move);
            }

            double_push &= double_push - 1;
        }

        while (left_capture != 0L) {
            int target = Long.numberOfTrailingZeros(left_capture);
            int origin = (color == BitBoard.WHITE) ? target - 7 : target + 9;

            if (target >= SQUARE_A8 || target <= SQUARE_H1) {
                int move = encodeMove(origin, target, BitBoard.WHITE_PAWN + offset);
                if (!doesMoveCauseCheck(move, color)) {
                    move_list.add(move | (BitBoard.WHITE_QUEEN + offset << 16));
                    move_list.add(move | (BitBoard.WHITE_ROOK + offset << 16));
                    move_list.add(move | (BitBoard.WHITE_BISHOP + offset << 16));
                    move_list.add(move | (BitBoard.WHITE_KNIGHT + offset << 16));
                }
            } else {
                int move = encodeMove(origin, target, BitBoard.WHITE_PAWN + offset);
                if (!doesMoveCauseCheck(move, color)) {
                    move_list.add(move);
                }
            }

            left_capture &= left_capture - 1;
        }

        while (right_capture != 0L) {
            int target = Long.numberOfTrailingZeros(right_capture);
            int origin = (color == BitBoard.WHITE) ? target - 9 : target + 7;
           
            if (target >= SQUARE_A8 || target <= SQUARE_H1) {
                int move = encodeMove(origin, target, BitBoard.WHITE_PAWN + offset);
                if (!doesMoveCauseCheck(move, color)) {
                    move_list.add(move | (BitBoard.WHITE_QUEEN + offset << 16));
                    move_list.add(move | (BitBoard.WHITE_ROOK + offset << 16));
                    move_list.add(move | (BitBoard.WHITE_BISHOP + offset << 16));
                    move_list.add(move | (BitBoard.WHITE_KNIGHT + offset << 16));
                }
            } else {
                int move = encodeMove(origin, target, BitBoard.WHITE_PAWN + offset);
                if (!doesMoveCauseCheck(move, color)) {
                    move_list.add(move);
                }
            }
            
            right_capture &= right_capture - 1;
        }

        // Knight
        long knight_board = BitBoard.piece_bitboards[BitBoard.WHITE_KNIGHT + offset];
        while (knight_board != 0L) {
            int square = Long.numberOfTrailingZeros(knight_board);
            long moves = Precomputer.KNIGHT_MOVE_TABLE[square] & ~BitBoard.color_bitboards[color];

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);

                int move = encodeMove(square, target, BitBoard.WHITE_KNIGHT + offset);
                if (!doesMoveCauseCheck(move, color)) {
                    move_list.add(move);
                }

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
                
                int move = encodeMove(square, target, BitBoard.WHITE_KING + offset);
                if (!doesMoveCauseCheck(move, color)) {
                    move_list.add(move);
                }

                moves &= moves - 1;
            }
            
            king_board &= king_board - 1;
        }

        // Castling
        if (color == BitBoard.WHITE) {
            if (!isUnderAttack(SQUARE_E1, BitBoard.WHITE)) {
                if ((BitBoard.castle_rights & BitBoard.WHITE_KING_ROOK_MASK) != 0
                    && BitBoard.isEmpty(SQUARE_F1)
                    && BitBoard.isEmpty(SQUARE_G1)
                    && !isUnderAttack(SQUARE_F1, BitBoard.WHITE)
                    && !isUnderAttack(SQUARE_G1, BitBoard.WHITE)) {

                    int move = encodeMove(SQUARE_E1, SQUARE_G1, BitBoard.WHITE_KING, MoveHandler.CASTLE_FLAG);
                    move_list.add(move);
                }
                if ((BitBoard.castle_rights & BitBoard.WHITE_QUEEN_ROOK_MASK) != 0
                    && BitBoard.isEmpty(SQUARE_B1)
                    && BitBoard.isEmpty(SQUARE_C1)
                    && BitBoard.isEmpty(SQUARE_D1)
                    && !isUnderAttack(SQUARE_C1, BitBoard.WHITE)
                    && !isUnderAttack(SQUARE_D1, BitBoard.WHITE)) {

                    int move = encodeMove(SQUARE_E1, SQUARE_C1, BitBoard.WHITE_KING, MoveHandler.CASTLE_FLAG);
                    move_list.add(move);
                }
            }
        } else {
            if (!isUnderAttack(SQUARE_E8, BitBoard.BLACK)) {
                if ((BitBoard.castle_rights & BitBoard.BLACK_KING_ROOK_MASK) != 0
                    && BitBoard.isEmpty(SQUARE_F8)
                    && BitBoard.isEmpty(SQUARE_G8)
                    && !isUnderAttack(SQUARE_F8, BitBoard.BLACK)
                    && !isUnderAttack(SQUARE_G8, BitBoard.BLACK)) {

                    int move = encodeMove(SQUARE_E8, SQUARE_G8, BitBoard.BLACK_KING, MoveHandler.CASTLE_FLAG);
                    move_list.add(move);
                }
                if ((BitBoard.castle_rights & BitBoard.BLACK_QUEEN_ROOK_MASK) != 0
                    && BitBoard.isEmpty(SQUARE_B8)
                    && BitBoard.isEmpty(SQUARE_C8)
                    && BitBoard.isEmpty(SQUARE_D8)
                    && !isUnderAttack(SQUARE_C8, BitBoard.BLACK)
                    && !isUnderAttack(SQUARE_D8, BitBoard.BLACK)) {

                    int move = encodeMove(SQUARE_E8, SQUARE_C8, BitBoard.BLACK_KING, MoveHandler.CASTLE_FLAG);
                    move_list.add(move);
                }
            }
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
                
                int move = encodeMove(square, target, BitBoard.WHITE_BISHOP + offset);
                if (!doesMoveCauseCheck(move, color)) {
                    move_list.add(move);
                }

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
                
                int move = encodeMove(square, target, BitBoard.WHITE_ROOK + offset);
                if (!doesMoveCauseCheck(move, color)) {
                    move_list.add(move);
                }

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
                
                int move = encodeMove(square, target, BitBoard.WHITE_QUEEN + offset);
                if (!doesMoveCauseCheck(move, color)) {
                    move_list.add(move);
                }

                moves &= moves - 1;
            }

            queen_board &= queen_board - 1;
        }

        return move_list;
    }
}