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

    public static boolean isUnderAttack(int pos, int victim_color) {
        int enemy_color = victim_color ^ 1;

        // Slider Attacks

        // Queen
        long enemy_queen_board = BitBoard.piece_bitboards[BitBoard.QUEEN | enemy_color];

        // Bishop
        long enemy_bishop_board = BitBoard.piece_bitboards[BitBoard.BISHOP | enemy_color];
        int index = (int) (((
            BitBoard.occupancy_bitboard 
            & Precomputer.BISHOP_MASKS[pos]) 
            * Precomputer.BISHOP_MAGIC_NUMBERS[pos]) 
            >>> Precomputer.BISHOP_SHIFTS[pos]);
        long moves = Precomputer.BISHOP_MOVE_TABLE[pos][index] & ~BitBoard.color_bitboards[victim_color];

        if ((moves & (enemy_bishop_board | enemy_queen_board)) != 0) return true;

        // Rook
        long enemy_rook_board = BitBoard.piece_bitboards[BitBoard.ROOK | enemy_color];
        index = (int) (((
            BitBoard.occupancy_bitboard 
            & Precomputer.ROOK_MASKS[pos]) 
            * Precomputer.ROOK_MAGIC_NUMBERS[pos]) 
            >>> Precomputer.ROOK_SHIFTS[pos]);
        moves = Precomputer.ROOK_MOVE_TABLE[pos][index] & ~BitBoard.color_bitboards[victim_color];

        if ((moves & (enemy_rook_board | enemy_queen_board)) != 0) return true;

        // Knight Attacks

        long enemy_knight_board = BitBoard.piece_bitboards[BitBoard.KNIGHT | enemy_color];
        if ((enemy_knight_board & Precomputer.KNIGHT_MOVE_TABLE[pos]) != 0) return true;

        // Pawn Attacks

        long pawn_attacks;
        long enemy_pawn_board = BitBoard.piece_bitboards[BitBoard.PAWN | enemy_color];
        if (victim_color == BitBoard.WHITE) {
            pawn_attacks = ((enemy_pawn_board >>> BitBoard.SE_SHIFT) & ~BitBoard.COL_A) 
                        | ((enemy_pawn_board >>> BitBoard.SW_SHIFT) & ~BitBoard.COL_H);
        } else {
            pawn_attacks = ((enemy_pawn_board << BitBoard.NW_SHIFT) & ~BitBoard.COL_H) 
                        | ((enemy_pawn_board << BitBoard.NE_SHIFT) & ~BitBoard.COL_A);
        }

        if ((pawn_attacks & (1L << pos)) != 0) return true;

        // King Attacks

        long enemy_king_board = BitBoard.piece_bitboards[BitBoard.KING | enemy_color];
        if ((enemy_king_board & Precomputer.KING_MOVE_TABLE[pos]) != 0) return true;

        return false;
    }

    public static boolean isKingInCheck(int color) {
        int square = Long.numberOfTrailingZeros(
            BitBoard.piece_bitboards[BitBoard.KING | color]
        );

        return isUnderAttack(square, color);
    }

    public static boolean verifyPseudoKiller(int move, int turn) {
        int origin = (move >> 6) & MoveHandler.POSITION_MASK;
        int target = move & MoveHandler.POSITION_MASK;
        int piece = (move >> 12) & MoveHandler.PIECE_MASK;

        if ((piece & BitBoard.COLOR_MASK) != turn) {
            return false;
        }

        if ((BitBoard.piece_bitboards[piece] & (1L << origin)) == 0) {
            return false;
        }

        if ((BitBoard.occupancy_bitboard & (1L << target)) != 0) {
            return false;
        }

        if ((move & MoveHandler.CASTLE_FLAG) != 0) {
            return false;
        }

        long target_mask = 1L << target;
        switch (piece & BitBoard.PIECE_MASK) {
            case BitBoard.PAWN:
                if ((move & MoveHandler.DOUBLE_FLAG) != 0) {
                    int single = (turn == BitBoard.WHITE) ? target + BitBoard.SOUTH : target + BitBoard.NORTH;
                    return (BitBoard.occupancy_bitboard & (1L << single)) == 0;
                }
                return true;

            case BitBoard.KNIGHT:
                return true;

            case BitBoard.BISHOP:
                int b_index = (int) (((
                    BitBoard.occupancy_bitboard 
                    & Precomputer.BISHOP_MASKS[origin]) 
                    * Precomputer.BISHOP_MAGIC_NUMBERS[origin]) 
                    >>> Precomputer.BISHOP_SHIFTS[origin]);
                return (Precomputer.BISHOP_MOVE_TABLE[origin][b_index] & target_mask) != 0;

            case BitBoard.ROOK:
                int r_index = (int) (((
                    BitBoard.occupancy_bitboard 
                    & Precomputer.ROOK_MASKS[origin]) 
                    * Precomputer.ROOK_MAGIC_NUMBERS[origin]) 
                    >>> Precomputer.ROOK_SHIFTS[origin]);
                return (Precomputer.ROOK_MOVE_TABLE[origin][r_index] & target_mask) != 0;

            case BitBoard.QUEEN:
                b_index = (int) (((
                    BitBoard.occupancy_bitboard 
                    & Precomputer.BISHOP_MASKS[origin]) 
                    * Precomputer.BISHOP_MAGIC_NUMBERS[origin]) 
                    >>> Precomputer.BISHOP_SHIFTS[origin]);

                r_index = (int) (((
                    BitBoard.occupancy_bitboard 
                    & Precomputer.ROOK_MASKS[origin]) 
                    * Precomputer.ROOK_MAGIC_NUMBERS[origin]) 
                    >>> Precomputer.ROOK_SHIFTS[origin]);

                long moves = Precomputer.BISHOP_MOVE_TABLE[origin][b_index] | Precomputer.ROOK_MOVE_TABLE[origin][r_index];
                return (moves & target_mask) != 0;

            case BitBoard.KING:
                return true;
        }

        return false;
    }

    public static MoveList generateAllMoves(int color) {
        MoveList noisy_list = new MoveList();
        MoveList quiet_list = new MoveList();

        generateNoisyMoves(color, noisy_list);
        generateQuietMoves(color, quiet_list);

        noisy_list.addAll(quiet_list);
        return noisy_list;
    }

    // FIXME: Consider Promotion Moves
    // FIXME: Consider Castling Moves
    public static void generateNoisyMoves(int color, MoveList move_list) {
        move_list.clear();
        
        // Pawn
        long pawn_board = BitBoard.piece_bitboards[BitBoard.PAWN | color];

        long left_capture = 0L;
        long right_capture = 0L;

        if (color == BitBoard.WHITE) {
            left_capture = 
                (pawn_board << BitBoard.NW_SHIFT) 
                & BitBoard.color_bitboards[BitBoard.BLACK] 
                & ~BitBoard.COL_H;

            right_capture = 
                (pawn_board << BitBoard.NE_SHIFT)
                & BitBoard.color_bitboards[BitBoard.BLACK] 
                & ~BitBoard.COL_A;

            if (BitBoard.passant_rights != BitBoard.NO_PASSANT) {
                long left_passant = 
                    (pawn_board << BitBoard.NW_SHIFT)
                    & (1L << BitBoard.passant_rights)
                    & ~BitBoard.COL_H;

                long right_passant =
                    (pawn_board << BitBoard.NE_SHIFT)
                    & (1L << BitBoard.passant_rights)
                    & ~BitBoard.COL_A;

                if (left_passant != 0) {
                    int target = Long.numberOfTrailingZeros(left_passant);
                    int origin = target + BitBoard.SOUTH_EAST;
                    
                    move_list.add(
                        encodeMove(origin, target, BitBoard.WHITE_PAWN, MoveHandler.PASSANT_FLAG)
                    );
                }
                if (right_passant != 0) {
                    int target = Long.numberOfTrailingZeros(right_passant);
                    int origin = target + BitBoard.SOUTH_WEST;
                    
                    move_list.add(
                        encodeMove(origin, target, BitBoard.WHITE_PAWN, MoveHandler.PASSANT_FLAG)
                    );
                }
            }
                
        } else {
            left_capture = 
                (pawn_board >>> BitBoard.SW_SHIFT) 
                & BitBoard.color_bitboards[BitBoard.WHITE] 
                & ~BitBoard.COL_H;

            right_capture = 
                (pawn_board >>> BitBoard.SE_SHIFT) 
                & BitBoard.color_bitboards[BitBoard.WHITE] 
                & ~BitBoard.COL_A;

            if (BitBoard.passant_rights != BitBoard.NO_PASSANT) {
                long left_passant =
                    (pawn_board >>> BitBoard.SW_SHIFT)
                    & (1L << BitBoard.passant_rights)
                    & ~BitBoard.COL_H;
                
                long right_passant =
                    (pawn_board >>> BitBoard.SE_SHIFT)
                    & (1L << BitBoard.passant_rights)
                    & ~BitBoard.COL_A;

                if (left_passant != 0) {
                    int target = Long.numberOfTrailingZeros(left_passant);
                    int origin = target + BitBoard.NORTH_EAST;

                    move_list.add(
                        encodeMove(origin, target, BitBoard.BLACK_PAWN, MoveHandler.PASSANT_FLAG)
                    );
                }
                if (right_passant != 0) {
                    int target = Long.numberOfTrailingZeros(right_passant);
                    int origin = target + BitBoard.NORTH_WEST;
                    
                    move_list.add(
                        encodeMove(origin, target, BitBoard.BLACK_PAWN, MoveHandler.PASSANT_FLAG)
                    );
                }
            }
        }

        while (left_capture != 0L) {
            int target = Long.numberOfTrailingZeros(left_capture);
            int origin = (color == BitBoard.WHITE) ? target + BitBoard.SOUTH_EAST : target + BitBoard.NORTH_EAST;

            if (target >= SQUARE_A8 || target <= SQUARE_H1) {
                int move = encodeMove(origin, target, BitBoard.PAWN | color);
                move_list.add(move | (BitBoard.QUEEN + color << 16));
                move_list.add(move | (BitBoard.ROOK + color << 16));
                move_list.add(move | (BitBoard.BISHOP + color << 16));
                move_list.add(move | (BitBoard.KNIGHT + color << 16));
            } else {
                move_list.add(
                    encodeMove(origin, target, BitBoard.PAWN | color)
                );
            }

            left_capture &= left_capture - 1;
        }

        while (right_capture != 0L) {
            int target = Long.numberOfTrailingZeros(right_capture);
            int origin = (color == BitBoard.WHITE) ? target + BitBoard.SOUTH_WEST : target + BitBoard.NORTH_WEST;
           
            if (target >= SQUARE_A8 || target <= SQUARE_H1) {
                int move = encodeMove(origin, target, BitBoard.PAWN | color);
                move_list.add(move | (BitBoard.QUEEN + color << 16));
                move_list.add(move | (BitBoard.ROOK + color << 16));
                move_list.add(move | (BitBoard.BISHOP + color << 16));
                move_list.add(move | (BitBoard.KNIGHT + color << 16));
            } else {
                move_list.add(
                    encodeMove(origin, target, BitBoard.PAWN | color)
                );
            }
            
            right_capture &= right_capture - 1;
        }

        // Knight
        long knight_board = BitBoard.piece_bitboards[BitBoard.KNIGHT | color];
        while (knight_board != 0L) {
            int square = Long.numberOfTrailingZeros(knight_board);
            long moves = Precomputer.KNIGHT_MOVE_TABLE[square] & BitBoard.color_bitboards[color ^ 1];

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);

                move_list.add(
                    encodeMove(square, target, BitBoard.KNIGHT | color)
                );

                moves &= moves - 1;
            }

            knight_board &= knight_board - 1;
        }

        // King
        long king_board = BitBoard.piece_bitboards[BitBoard.KING | color];

        while (king_board != 0L) {
            int square = Long.numberOfTrailingZeros(king_board);
            long moves = Precomputer.KING_MOVE_TABLE[square] & BitBoard.color_bitboards[color ^ 1];

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                
                move_list.add(
                    encodeMove(square, target, BitBoard.KING | color)
                );

                moves &= moves - 1;
            }
            
            king_board &= king_board - 1;
        }

        // Bishop
        long bishop_board = BitBoard.piece_bitboards[BitBoard.BISHOP | color];
        while (bishop_board != 0L) {
            int square = Long.numberOfTrailingZeros(bishop_board);
            int index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.BISHOP_MASKS[square]) 
                * Precomputer.BISHOP_MAGIC_NUMBERS[square]) 
                >>> Precomputer.BISHOP_SHIFTS[square]);
            long moves = Precomputer.BISHOP_MOVE_TABLE[square][index] & BitBoard.color_bitboards[color ^ 1];

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                
                move_list.add(
                    encodeMove(square, target, BitBoard.BISHOP | color)
                );

                moves &= moves - 1;
            }

            bishop_board &= bishop_board - 1;
        }

        // Rook
        long rook_board = BitBoard.piece_bitboards[BitBoard.ROOK | color];
        while (rook_board != 0L) {
            int square = Long.numberOfTrailingZeros(rook_board);
            int index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.ROOK_MASKS[square]) 
                * Precomputer.ROOK_MAGIC_NUMBERS[square]) 
                >>> Precomputer.ROOK_SHIFTS[square]);
            long moves = Precomputer.ROOK_MOVE_TABLE[square][index] & BitBoard.color_bitboards[color ^ 1];

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                
                move_list.add(
                    encodeMove(square, target, BitBoard.ROOK | color)
                );

                moves &= moves - 1;
            }

            rook_board &= rook_board - 1;
        }

        // Queen
        long queen_board = BitBoard.piece_bitboards[BitBoard.QUEEN | color];
        while (queen_board != 0L) {
            int square = Long.numberOfTrailingZeros(queen_board);

            // Diagonally
            int index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.BISHOP_MASKS[square]) 
                * Precomputer.BISHOP_MAGIC_NUMBERS[square]) 
                >>> Precomputer.BISHOP_SHIFTS[square]);
            long moves = Precomputer.BISHOP_MOVE_TABLE[square][index] & BitBoard.color_bitboards[color ^ 1];

            // Orthogonally
            index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.ROOK_MASKS[square]) 
                * Precomputer.ROOK_MAGIC_NUMBERS[square]) 
                >>> Precomputer.ROOK_SHIFTS[square]);
            moves |= Precomputer.ROOK_MOVE_TABLE[square][index] & BitBoard.color_bitboards[color ^ 1];

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                
                move_list.add(
                    encodeMove(square, target, BitBoard.QUEEN | color)
                );

                moves &= moves - 1;
            }

            queen_board &= queen_board - 1;
        }
    }

    public static void generateQuietMoves(int color, MoveList move_list) {
        move_list.clear();

        // Pawns
        long pawn_board = BitBoard.piece_bitboards[BitBoard.PAWN | color];
        long single_push = 0L;
        long double_push = 0L;

        if (color == BitBoard.WHITE) {
            single_push = (pawn_board << BitBoard.N_SHIFT) & ~BitBoard.occupancy_bitboard;
            double_push = ((single_push & BitBoard.ROW_3) << BitBoard.N_SHIFT) & ~BitBoard.occupancy_bitboard;
        } else {
            single_push = (pawn_board >>> BitBoard.S_SHIFT) & ~BitBoard.occupancy_bitboard;
            double_push = ((single_push & BitBoard.ROW_6) >>> BitBoard.S_SHIFT) & ~BitBoard.occupancy_bitboard;
        }

        while (single_push != 0L) {
            int target = Long.numberOfTrailingZeros(single_push);
            int origin = (color == BitBoard.WHITE) ? target + BitBoard.SOUTH : target + BitBoard.NORTH;

            if (target >= SQUARE_A8 || target <= SQUARE_H1) {
                int move = encodeMove(origin, target, BitBoard.PAWN | color);
                move_list.add(move | (BitBoard.QUEEN + color << 16));
                move_list.add(move | (BitBoard.ROOK + color << 16));
                move_list.add(move | (BitBoard.BISHOP + color << 16));
                move_list.add(move | (BitBoard.KNIGHT + color << 16));
            } else {
                move_list.add(
                    encodeMove(origin, target, BitBoard.PAWN | color)
                );
            }
           
            single_push &= single_push - 1;
        }

        while (double_push != 0L) {
            int target = Long.numberOfTrailingZeros(double_push);
            int origin = (color == BitBoard.WHITE) ? target + BitBoard.SOUTH_2 : target + BitBoard.NORTH_2;
            
            move_list.add(
                encodeMove(origin, target, BitBoard.PAWN | color, MoveHandler.DOUBLE_FLAG)
            );

            double_push &= double_push - 1;
        }

        // Knights
        long knight_board = BitBoard.piece_bitboards[BitBoard.KNIGHT | color];
        while (knight_board != 0L) {
            int square = Long.numberOfTrailingZeros(knight_board);
            long moves = Precomputer.KNIGHT_MOVE_TABLE[square] & ~BitBoard.occupancy_bitboard;

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);

                move_list.add(
                    encodeMove(square, target, BitBoard.KNIGHT | color)
                );

                moves &= moves - 1;
            }

            knight_board &= knight_board - 1;
        }

        // Kings
        long king_board = BitBoard.piece_bitboards[BitBoard.KING | color];

        while (king_board != 0L) {
            int square = Long.numberOfTrailingZeros(king_board);
            long moves = Precomputer.KING_MOVE_TABLE[square] & ~BitBoard.occupancy_bitboard;

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                
                move_list.add(
                    encodeMove(square, target, BitBoard.KING | color)
                );

                moves &= moves - 1;
            }
            
            king_board &= king_board - 1;
        }

        // Castling
        // FIXME: Could Remove more checks
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

        // Bishops
        long bishop_board = BitBoard.piece_bitboards[BitBoard.BISHOP | color];
        while (bishop_board != 0L) {
            int square = Long.numberOfTrailingZeros(bishop_board);
            int index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.BISHOP_MASKS[square]) 
                * Precomputer.BISHOP_MAGIC_NUMBERS[square]) 
                >>> Precomputer.BISHOP_SHIFTS[square]);
            long moves = Precomputer.BISHOP_MOVE_TABLE[square][index] & ~BitBoard.occupancy_bitboard;

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                
                move_list.add(
                    encodeMove(square, target, BitBoard.BISHOP | color)
                );

                moves &= moves - 1;
            }

            bishop_board &= bishop_board - 1;
        }

        // Rooks
        long rook_board = BitBoard.piece_bitboards[BitBoard.ROOK | color];
        while (rook_board != 0L) {
            int square = Long.numberOfTrailingZeros(rook_board);
            int index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.ROOK_MASKS[square]) 
                * Precomputer.ROOK_MAGIC_NUMBERS[square]) 
                >>> Precomputer.ROOK_SHIFTS[square]);
            long moves = Precomputer.ROOK_MOVE_TABLE[square][index] & ~BitBoard.occupancy_bitboard;

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                
                move_list.add(
                    encodeMove(square, target, BitBoard.ROOK | color)
                );

                moves &= moves - 1;
            }

            rook_board &= rook_board - 1;
        }

        // Queens
        long queen_board = BitBoard.piece_bitboards[BitBoard.QUEEN | color];
        while (queen_board != 0L) {
            int square = Long.numberOfTrailingZeros(queen_board);

            // Diagonally
            int index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.BISHOP_MASKS[square]) 
                * Precomputer.BISHOP_MAGIC_NUMBERS[square]) 
                >>> Precomputer.BISHOP_SHIFTS[square]);
            long moves = Precomputer.BISHOP_MOVE_TABLE[square][index] & ~BitBoard.occupancy_bitboard;

            // Orthogonally
            index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.ROOK_MASKS[square]) 
                * Precomputer.ROOK_MAGIC_NUMBERS[square]) 
                >>> Precomputer.ROOK_SHIFTS[square]);
            moves |= Precomputer.ROOK_MOVE_TABLE[square][index] & ~BitBoard.occupancy_bitboard;

            while (moves != 0L) {
                int target = Long.numberOfTrailingZeros(moves);
                
                move_list.add(
                    encodeMove(square, target, BitBoard.QUEEN | color)
                );

                moves &= moves - 1;
            }

            queen_board &= queen_board - 1;
        }
    }

    public static int getMobility(int color) {
        int mobility = 0;

        // Pawn
        long pawn_board = BitBoard.piece_bitboards[BitBoard.PAWN | color];

        long left_capture = 0L;
        long right_capture = 0L;

        if (color == BitBoard.WHITE) {
            left_capture = 
                (pawn_board << BitBoard.NW_SHIFT) 
                & BitBoard.color_bitboards[BitBoard.BLACK] 
                & ~BitBoard.COL_H;

            right_capture = 
                (pawn_board << BitBoard.NE_SHIFT)
                & BitBoard.color_bitboards[BitBoard.BLACK] 
                & ~BitBoard.COL_A;    
        } else {
            left_capture = 
                (pawn_board >>> BitBoard.SW_SHIFT) 
                & BitBoard.color_bitboards[BitBoard.WHITE] 
                & ~BitBoard.COL_H;

            right_capture = 
                (pawn_board >>> BitBoard.SE_SHIFT) 
                & BitBoard.color_bitboards[BitBoard.WHITE] 
                & ~BitBoard.COL_A;
        }
        
        mobility += Long.bitCount(left_capture);
        mobility += Long.bitCount(right_capture);

        // Knight
        long knight_board = BitBoard.piece_bitboards[BitBoard.KNIGHT | color];
        while (knight_board != 0L) {
            int square = Long.numberOfTrailingZeros(knight_board);
            long moves = Precomputer.KNIGHT_MOVE_TABLE[square] & ~BitBoard.color_bitboards[color];

            mobility += Long.bitCount(moves);

            knight_board &= knight_board - 1;
        }

        // King
        long king_board = BitBoard.piece_bitboards[BitBoard.KING | color];

        while (king_board != 0L) {
            int square = Long.numberOfTrailingZeros(king_board);
            long moves = Precomputer.KING_MOVE_TABLE[square] & ~BitBoard.color_bitboards[color];

            mobility += Long.bitCount(moves);
            
            king_board &= king_board - 1;
        }

        // Bishop
        long bishop_board = BitBoard.piece_bitboards[BitBoard.BISHOP | color];
        while (bishop_board != 0L) {
            int square = Long.numberOfTrailingZeros(bishop_board);
            int index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.BISHOP_MASKS[square]) 
                * Precomputer.BISHOP_MAGIC_NUMBERS[square]) 
                >>> Precomputer.BISHOP_SHIFTS[square]);
            long moves = Precomputer.BISHOP_MOVE_TABLE[square][index] & ~BitBoard.color_bitboards[color];

            mobility += Long.bitCount(moves);

            bishop_board &= bishop_board - 1;
        }

        // Rook
        long rook_board = BitBoard.piece_bitboards[BitBoard.ROOK | color];
        while (rook_board != 0L) {
            int square = Long.numberOfTrailingZeros(rook_board);
            int index = (int) (((
                BitBoard.occupancy_bitboard 
                & Precomputer.ROOK_MASKS[square]) 
                * Precomputer.ROOK_MAGIC_NUMBERS[square]) 
                >>> Precomputer.ROOK_SHIFTS[square]);
            long moves = Precomputer.ROOK_MOVE_TABLE[square][index] & ~BitBoard.color_bitboards[color];

            mobility += Long.bitCount(moves);

            rook_board &= rook_board - 1;
        }

        // Queen
        long queen_board = BitBoard.piece_bitboards[BitBoard.QUEEN | color];
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

            mobility += Long.bitCount(moves);

            queen_board &= queen_board - 1;
        }

        return mobility;
    }

    public static long getAttackers(int square) {
        long attackers = 0L;

        // Pawn Attacks
        long white_pawns = (((1L << square) >>> BitBoard.SE_SHIFT) & ~BitBoard.COL_A) 
                        | (((1L << square) >>> BitBoard.SW_SHIFT) & ~BitBoard.COL_H);
        long black_pawns = (((1L << square) << BitBoard.NW_SHIFT) & ~BitBoard.COL_H) 
                        | (((1L << square) << BitBoard.NE_SHIFT) & ~BitBoard.COL_A);

        attackers |= white_pawns & BitBoard.piece_bitboards[BitBoard.WHITE_PAWN];
        attackers |= black_pawns & BitBoard.piece_bitboards[BitBoard.BLACK_PAWN];

        // Knight Attacks
        attackers |= 
            (BitBoard.piece_bitboards[BitBoard.WHITE_KNIGHT] 
            | BitBoard.piece_bitboards[BitBoard.BLACK_KNIGHT])
            & Precomputer.KNIGHT_MOVE_TABLE[square];
 
        // Bishop + Queen
        long diagonals = 
            BitBoard.piece_bitboards[BitBoard.WHITE_BISHOP] 
            | BitBoard.piece_bitboards[BitBoard.WHITE_QUEEN]
            | BitBoard.piece_bitboards[BitBoard.BLACK_BISHOP]
            | BitBoard.piece_bitboards[BitBoard.BLACK_QUEEN];

        int index = (int) (((
            BitBoard.occupancy_bitboard 
            & Precomputer.BISHOP_MASKS[square]) 
            * Precomputer.BISHOP_MAGIC_NUMBERS[square]) 
            >>> Precomputer.BISHOP_SHIFTS[square]);

        attackers |= diagonals & Precomputer.BISHOP_MOVE_TABLE[square][index];

        // Rook + Queen
        long orthagonals = 
            BitBoard.piece_bitboards[BitBoard.WHITE_ROOK]
            | BitBoard.piece_bitboards[BitBoard.WHITE_QUEEN]
            | BitBoard.piece_bitboards[BitBoard.BLACK_ROOK]
            | BitBoard.piece_bitboards[BitBoard.BLACK_QUEEN];

        index = (int) (((
            BitBoard.occupancy_bitboard 
            & Precomputer.ROOK_MASKS[square]) 
            * Precomputer.ROOK_MAGIC_NUMBERS[square]) 
            >>> Precomputer.ROOK_SHIFTS[square]);

        attackers |= orthagonals & Precomputer.ROOK_MOVE_TABLE[square][index];

        // King Attacks

        attackers |= 
            (BitBoard.piece_bitboards[BitBoard.WHITE_KING]
            | BitBoard.piece_bitboards[BitBoard.BLACK_KING])
            & Precomputer.KING_MOVE_TABLE[square];

        return attackers;
    }

    public static long getXRayAttackers(int square, long occupancy) {
        long attackers = 0L;

        // Bishop + Queen
        long diagonal_board = 
            (BitBoard.piece_bitboards[BitBoard.WHITE_BISHOP] 
            | BitBoard.piece_bitboards[BitBoard.WHITE_QUEEN]
            | BitBoard.piece_bitboards[BitBoard.BLACK_BISHOP]
            | BitBoard.piece_bitboards[BitBoard.BLACK_QUEEN])
            & occupancy;

        int index = (int) (((
            occupancy
            & Precomputer.BISHOP_MASKS[square]) 
            * Precomputer.BISHOP_MAGIC_NUMBERS[square]) 
            >>> Precomputer.BISHOP_SHIFTS[square]);

        attackers |= diagonal_board & Precomputer.BISHOP_MOVE_TABLE[square][index];

        // Rook + Queen
        long orthagonal_board = 
            (BitBoard.piece_bitboards[BitBoard.WHITE_ROOK]
            | BitBoard.piece_bitboards[BitBoard.WHITE_QUEEN]
            | BitBoard.piece_bitboards[BitBoard.BLACK_ROOK]
            | BitBoard.piece_bitboards[BitBoard.BLACK_QUEEN])
            & occupancy;

        index = (int) (((
            occupancy
            & Precomputer.ROOK_MASKS[square]) 
            * Precomputer.ROOK_MAGIC_NUMBERS[square]) 
            >>> Precomputer.ROOK_SHIFTS[square]);

        attackers |= orthagonal_board & Precomputer.ROOK_MOVE_TABLE[square][index];

        return attackers;
    }
}