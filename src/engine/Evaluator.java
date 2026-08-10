package src.engine;

import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.utils.GameStack;

public class Evaluator {
    public static final int MATE_SCORE = 100000;
    public static final int MATE_BOUND = 99000;
    public static final int DRAW_SCORE = 0;
    public static final int NULL_EVAL = -1;
    public static final int NULL_PHASE = -1;

    private static final int KNIGHT_PHASE = 1;
    private static final int BISHOP_PHASE = 1;
    private static final int ROOK_PHASE = 2;
    private static final int QUEEN_PHASE = 4;
    private static final int TOTAL_PHASE = 24;

    private static final int FILE_E = 4;
    private static final int FILE_D = 3;

    private static final long WHITE_SHIELD = BitBoard.ROW_2 | BitBoard.ROW_3;
    private static final long BLACK_SHIELD = BitBoard.ROW_6 | BitBoard.ROW_7;

    private static final long[] NEIGHBOR_COL = {
        BitBoard.COL_B,
        BitBoard.COL_A | BitBoard.COL_C,
        BitBoard.COL_B | BitBoard.COL_D,
        BitBoard.COL_C | BitBoard.COL_E,
        BitBoard.COL_D | BitBoard.COL_F,
        BitBoard.COL_E | BitBoard.COL_G,
        BitBoard.COL_F | BitBoard.COL_H,
        BitBoard.COL_G
    };

    private static final long[] SELF_COL = {
        BitBoard.COL_A,
        BitBoard.COL_B,
        BitBoard.COL_C,
        BitBoard.COL_D,
        BitBoard.COL_E,
        BitBoard.COL_F,
        BitBoard.COL_G,
        BitBoard.COL_H
    };

    private static final int[] PHASE_TABLE = {
        0, KNIGHT_PHASE, BISHOP_PHASE, ROOK_PHASE, QUEEN_PHASE, 0
    };

    private static int mg_eval;
    private static int eg_eval;
    private static int phase;

    // Texel's Tuning Method

    public static final int MISSING_PAWN = -30;
    public static final int UNCASTLED_KING = -47;
    public static final int BISHOP_MG = 47;
    public static final int BISHOP_EG = 61;
    public static final int ROOK_OPEN = 46;
    public static final int ROOK_SEMI = 27;
    public static final int ISOLATED_PAWN = -15;
    public static final int DOUBLED_PAWN = -24;

    public static final int[] PASSED_MG = {0, -21, -31, -16, -6, 27, 10, 0};
    public static final int[] PASSED_EG = {0, 17, 24, 44, 68, 111, 128, 0};

    public static final int[] PIECE_VALUES = {
        65,    -65,
        391,   -391,
        398,   -398,
        605,   -605,
        1225,  -1225,
        20000, -20000
    };

    public static final int[][] PSQT_MG = {
        {
            0,    0,    0,    0,    0,    0,    0,    0, 
            161,  151,  178,  125,  168,  151,   79,   78, 
            86,   77,  125,  104,  109,  154,  139,   78, 
            48,   59,   72,   76,   98,   88,   70,   66, 
            42,   43,   52,   73,   75,   70,   67,   51, 
            43,   37,   45,   41,   63,   47,   65,   37, 
            37,   33,   31,   21,   48,   57,   70,   25, 
            0,    0,    0,    0,    0,    0,    0,    0, 
        },

        {
            -129,   -3,  -25,  -49,  -18,  -46,  -50,  -64, 
            -34,    4,   46,   53,   42,   98,   -7,    9, 
            12,   46,   64,   95,  124,  163,   66,   42, 
            14,   37,   76,  113,   90,  112,   51,   50, 
            1,   20,   43,   42,   66,   60,   63,   10, 
            -35,    6,   29,   35,   46,   34,   37,  -11, 
            -44,    6,    7,   21,   10,   25,   -5,  -12, 
            -71,  -37,  -33,  -26,  -21,   16,  -21,  -81, 
        },

        {
            -29,    5,   31,   17,   14,   47,   10,  -16, 
            27,   24,   22,   12,   52,   54,   13,   38, 
            18,   49,   88,  102,   69,   87,   77,   83, 
            17,   52,   55,   76,   74,   60,   43,   13, 
            12,   24,   53,   56,   77,   58,   37,   29, 
            42,   50,   48,   55,   49,   43,   44,   43, 
            26,   33,   57,   23,   33,   54,   53,   39, 
            2,   31,    6,    5,    9,   -2,    2,  -22, 
        },

        {
            33,   23,   43,   59,   46,   40,   19,   63, 
            24,  -12,   12,   66,   24,   10,   22,  116, 
            23,   29,   15,    0,   39,   52,  125,   52, 
            21,  -15,  -10,  -32,   13,    8,   37,   19, 
            4,  -42,    9,   -4,  -20,  -45,   26,   21, 
            -12,  -11,  -45,  -34,   -1,   -8,   37,    5, 
            -18,  -28,  -34,   -6,  -14,    5,   18,  -16, 
            -16,  -18,   -8,    3,    4,    4,   10,  -10, 
        },

        {
            59,   95,  119,  100,   88,  116,  155,   68, 
            69,   23,   61,   88,   36,   74,   44,  125, 
            80,   74,   95,  134,  130,  101,  141,  154, 
            52,   53,   74,   63,   64,  106,   73,   93, 
            57,   55,   68,   69,   57,   65,   62,   81, 
            43,   65,   50,   74,   54,   71,   84,  101, 
            57,   79,   72,   75,   69,   73,   71,   89, 
            74,   19,   42,   61,   61,   55,   41,   32, 
        },

        {
            -91,   54,   48,   88,  -13,   80, -144,  -10, 
            -168,   54,   91,  -31,  -10,  -61,  -95, -211, 
            -198,  -91,  -65,  -96,  -90,  -38,  -29, -144, 
            -177, -184,  -68,  -10,  -57, -107, -218, -140, 
            -53, -123,  -77,  -88, -103,  -96, -112, -193, 
            -25,  -66,  -91,  -27,   -9,  -72,  -66,  -68, 
            42,  -15,   -3,   -7,  -24,  -20,    6,   40, 
            23,   48,   30,  -28,   42,  -54,   40,   69, 
        }
    };

    public static final int[][] PSQT_EG = {
        {
            0,    0,    0,    0,    0,    0,    0,    0, 
            219,  160,  185,  138,   92,  161,  171,  122, 
            120,  111,   83,   51,   57,   77,   89,   91, 
            85,   75,   67,   50,   44,   59,   73,   72, 
            65,   58,   50,   43,   46,   55,   53,   54, 
            57,   57,   49,   54,   52,   55,   51,   52, 
            63,   56,   58,   45,   51,   54,   46,   51, 
            0,    0,    0,    0,    0,    0,    0,    0, 
        },

        {
            -96,   37,   38,   48,  -14,  -53,  -11, -171, 
            -37,    7,   26,   31,  -22,    8,   -4,  -63, 
            -28,   34,   51,   60,   40,   16,   37,   27, 
            33,   43,   49,   45,   49,   43,   36,  -11, 
            13,   47,   65,   69,   46,   41,   -5,   10, 
            -3,   29,   31,   42,   38,   37,   21,    7, 
            17,  -13,   13,   24,   26,   31,   25,   35, 
            -7,  -13,   18,    0,   14,    7,  -21,   19, 
        },

        {
            18,   41,    3,   15,   25,  -15,  -13,   21, 
            -9,   17,   43,   33,   31,   15,   32,  -11, 
            26,   17,   32,   13,   32,   48,    1,    9, 
            24,   56,   51,   67,   42,   54,   49,   64, 
            35,   32,   59,   64,   34,   30,   51,   19, 
            -7,   33,   38,   48,   51,   39,   40,   -1, 
            30,   12,   10,   25,   47,   14,   24,  -11, 
            -13,   40,  -12,   17,   19,   33,   33,   46, 
        },

        {
            105,  104,   85,   80,   77,  100,   55,   85, 
            96,  118,  111,   75,   92,  123,  117,   70, 
            89,  105,   95,   82,   63,   66,   59,   55, 
            78,   92,   86,   93,   53,   70,   70,   63, 
            91,   88,   65,   73,   79,   81,   41,   71, 
            61,   69,   81,   71,   61,   73,   42,   48, 
            65,   77,   80,   61,   70,   52,   71,   51, 
            86,   84,   80,   58,   66,   80,   71,   72, 
        },

        {
            59,   75,  110,  123,  118,  202,  118,  104, 
            37,  125,  144,  126,  202,  202,  155,  106, 
            53,  102,   90,   95,  101,  187,  111,   48, 
            116,  158,  143,  175,  181,  143,  167,  123, 
            102,  148,   95,  137,  154,  123,  161,  112, 
            128,   78,  126,   77,  118,  127,  120,   47, 
            70,   61,   71,   78,   74,   84,   58,   68, 
            14,  140,  153,   59,   84,   79,   12,   33, 
        },

        {
            -82,  -50,  -13,  -56,  -21,  -53,  -36, -117, 
            -19,   47,   45,   95,   53,   48,   69,   12, 
            -1,   72,   75,   93,   78,   62,   82,   46, 
            -11,   65,   68,   69,   77,   80,   73,   20, 
            -4,   44,   59,   71,   69,   50,   34,    2, 
            -57,    6,   39,   40,   36,   19,   10,  -12, 
            -34,  -17,    5,   16,   24,    1,  -27,  -62, 
            -61,  -74,  -52,  -17,  -59,  -20,  -74, -109, 
        }
    };
    
    public static boolean isThreeFoldRepetition(long hash) {
        if (BitBoard.halfmoves < 4) {
            return false;
        }

        int count = 1;
        int limit = Math.max(0, GameStack.size() - BitBoard.halfmoves);
        for (int i = GameStack.size() - 2; i >= limit; i -= 2) {
            if (GameStack.getHashAt(i) == hash) {
                count++;

                if (count >= 3) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isFiftyMoves() {
        return BitBoard.halfmoves >= 100;
    }

    public static boolean isInsufficientMaterial() {
        // General Material
        if (BitBoard.piece_bitboards[BitBoard.WHITE_PAWN] != 0
            || BitBoard.piece_bitboards[BitBoard.WHITE_ROOK] != 0
            || BitBoard.piece_bitboards[BitBoard.WHITE_QUEEN] != 0
            || BitBoard.piece_bitboards[BitBoard.BLACK_PAWN] != 0
            || BitBoard.piece_bitboards[BitBoard.BLACK_ROOK] != 0
            || BitBoard.piece_bitboards[BitBoard.BLACK_QUEEN] != 0) {
            return false;
        }

        // Bishop Pair
        boolean whitePair = (BitBoard.piece_bitboards[BitBoard.WHITE_BISHOP] & BitBoard.LIGHT_SQUARES) != 0
            && (BitBoard.piece_bitboards[BitBoard.WHITE_BISHOP] & BitBoard.DARK_SQUARES) != 0;
        boolean blackPair = (BitBoard.piece_bitboards[BitBoard.BLACK_BISHOP] & BitBoard.LIGHT_SQUARES) != 0
            && (BitBoard.piece_bitboards[BitBoard.BLACK_BISHOP] & BitBoard.DARK_SQUARES) != 0;

        if (whitePair || blackPair) {
            return false;
        }

        int w_knights = Long.bitCount(BitBoard.piece_bitboards[BitBoard.WHITE_KNIGHT]);
        int b_knights = Long.bitCount(BitBoard.piece_bitboards[BitBoard.BLACK_KNIGHT]);
        int w_bishops = Long.bitCount(BitBoard.piece_bitboards[BitBoard.WHITE_BISHOP]);
        int b_bishops = Long.bitCount(BitBoard.piece_bitboards[BitBoard.BLACK_BISHOP]);

        // 3 Minor Pieces
        if (w_knights + w_bishops >= 3 || b_knights + b_bishops >= 3) {
            return false;
        }

        // Knight Bishop
        if (w_bishops >= 1 && w_knights >= 1
            || b_bishops >= 1 && b_knights >= 1) {
            return false;
        }

        return true;
    }

    public static int getMateScore(int ply) {
        return -MATE_SCORE + ply;
    }

    public static int getMGWeights(int piece, int square) {
        if ((piece & BitBoard.COLOR_MASK) == BitBoard.WHITE) {
            return PSQT_MG[(piece & BitBoard.PIECE_MASK) >> 1][square ^ 56];
        } else {
            return -PSQT_MG[(piece & BitBoard.PIECE_MASK) >> 1][square];
        }
    }

    private static int getEGWeights(int piece, int square) {
        if ((piece & BitBoard.COLOR_MASK) == BitBoard.WHITE) {
            return PSQT_EG[(piece & BitBoard.PIECE_MASK) >> 1][square ^ 56];
        } else {
            return -PSQT_EG[(piece & BitBoard.PIECE_MASK) >> 1][square];
        }
    }

    public static void manualEvaluation() {
        mg_eval = 0;
        eg_eval = 0;
        phase = 0;

        for (int i = 0; i < 12; i++) {
            long bitboard = BitBoard.piece_bitboards[i];

            while (bitboard != 0) {
                int square = Long.numberOfTrailingZeros(bitboard);

                mg_eval += PIECE_VALUES[i] + getMGWeights(i, square);
                eg_eval += PIECE_VALUES[i] + getEGWeights(i, square);
                phase += PHASE_TABLE[(i & BitBoard.PIECE_MASK) >> 1];

                bitboard &= bitboard - 1;
            }
        }
    }

    public static void updateEvaluation(int move) {
        int origin = (move >> 6) & MoveHandler.POSITION_MASK;
        int target = move & MoveHandler.POSITION_MASK;
        int piece = (move >> 12) & MoveHandler.PIECE_MASK;
        int side = piece & BitBoard.COLOR_MASK;
        int capture = BitBoard.getPieceAt(target, side ^ 1);

        mg_eval += getMGWeights(piece, target) - getMGWeights(piece, origin);
        eg_eval += getEGWeights(piece, target) - getEGWeights(piece, origin);

        // Captures
        if (capture != BitBoard.NO_PIECE) {
            mg_eval -= PIECE_VALUES[capture] + getMGWeights(capture, target);
            eg_eval -= PIECE_VALUES[capture] + getEGWeights(capture, target);
            phase -= PHASE_TABLE[(capture & BitBoard.PIECE_MASK) >> 1];
        }

        // Passant
        if ((move & MoveHandler.PASSANT_FLAG) != 0) {
            int ep_square = (side == BitBoard.WHITE) ? target + BitBoard.SOUTH : target + BitBoard.NORTH;
            int ep_capture = BitBoard.PAWN | side ^ 1;
            mg_eval -= PIECE_VALUES[ep_capture] + getMGWeights(ep_capture, ep_square);
            eg_eval -= PIECE_VALUES[ep_capture] + getEGWeights(ep_capture, ep_square);
        }

        // Promotion
        if ((move & MoveHandler.PROMOTED_MASK) != 0) {
            int p_piece = (move & MoveHandler.PROMOTED_MASK) >>> 16;
            mg_eval -= PIECE_VALUES[piece] + getMGWeights(piece, target);
            eg_eval -= PIECE_VALUES[piece] + getEGWeights(piece, target);
            mg_eval += PIECE_VALUES[p_piece] + getMGWeights(p_piece, target);
            eg_eval += PIECE_VALUES[p_piece] + getEGWeights(p_piece, target);

            phase += PHASE_TABLE[(p_piece & BitBoard.PIECE_MASK) >> 1];
        }

        // Castling
        if ((move & MoveHandler.CASTLE_FLAG) != 0) {
            int r_origin;
            int r_target;
            int r_piece = BitBoard.ROOK | side;

            if (target == MoveGenerator.SQUARE_G1) {
                r_origin = MoveGenerator.SQUARE_H1;
                r_target = MoveGenerator.SQUARE_F1;
            } else if (target == MoveGenerator.SQUARE_C1) {
                r_origin = MoveGenerator.SQUARE_A1;
                r_target = MoveGenerator.SQUARE_D1;
            } else if (target == MoveGenerator.SQUARE_G8) {
                r_origin = MoveGenerator.SQUARE_H8;
                r_target = MoveGenerator.SQUARE_F8;
            } else {
                r_origin = MoveGenerator.SQUARE_A8;
                r_target = MoveGenerator.SQUARE_D8;
            }

            mg_eval += getMGWeights(r_piece, r_target) - getMGWeights(r_piece, r_origin);
            eg_eval += getEGWeights(r_piece, r_target) - getEGWeights(r_piece, r_origin);
        }
    }

    private static long getPawnEval(int side) {
        long m_pawns = BitBoard.piece_bitboards[BitBoard.PAWN | side];
        long pawns = m_pawns;
        int mg = 0;
        int eg = 0;

        while (pawns != 0) {
            int square = Long.numberOfTrailingZeros(pawns);
            int col = square & 7;

            // Isolated Pawns
            if ((m_pawns & NEIGHBOR_COL[col]) == 0) {
                mg += ISOLATED_PAWN;
                eg += ISOLATED_PAWN;
            }

            long forward_mask = (side == BitBoard.WHITE)
                ? SELF_COL[col] & (~0L << (square + 1))
                : SELF_COL[col] & ~((~0L) << square);

            // Doubled Pawns
            if ((m_pawns & forward_mask) != 0) {
                mg += DOUBLED_PAWN;
                eg += DOUBLED_PAWN;
            }

            // Passed Pawn
            long passed_mask = (side == BitBoard.WHITE)
                ? (SELF_COL[col] | NEIGHBOR_COL[col]) & (~0L << (square + 1))
                : (SELF_COL[col] | NEIGHBOR_COL[col]) & ~((~0L) << square);

            int row = (side == BitBoard.WHITE)
                ? square >> 3
                : 7 - (square >> 3);

            if ((BitBoard.piece_bitboards[BitBoard.PAWN | side ^ 1] & passed_mask) == 0) {
                mg += PASSED_MG[row];
                eg += PASSED_EG[row];
            }

            pawns &= pawns - 1;
        }

        return (long) mg << 32| (eg & 0xFFFFFFFFL);
    }

    private static long getBasicPieceEval(int side) {
        int mg = 0;
        int eg = 0;

        // Rook Files
        long m_pawns = BitBoard.piece_bitboards[BitBoard.PAWN | side];
        long e_pawns = BitBoard.piece_bitboards[BitBoard.PAWN | side ^ 1];

        long rooks = BitBoard.piece_bitboards[BitBoard.ROOK | side];
        while (rooks != 0) {
            int square = Long.numberOfTrailingZeros(rooks);
            int col = square & 7;

            if ((m_pawns & SELF_COL[col]) == 0) {
                if ((e_pawns & SELF_COL[col]) == 0) {
                    mg += ROOK_OPEN;
                    eg += ROOK_OPEN;
                } else {
                    mg += ROOK_SEMI;
                    eg += ROOK_SEMI;
                }
            }

            rooks &= rooks - 1;
        }

        // Bishop Pairs
        if (Long.bitCount(BitBoard.piece_bitboards[BitBoard.BISHOP | side]) >= 2) {
            mg += BISHOP_MG;
            eg += BISHOP_EG;
        }

        return (long) mg << 32 | (eg & 0xFFFFFFFFL);
    }

    private static int getKingSafetyMG(int side) {
        int mg = 0;

        long king = BitBoard.piece_bitboards[BitBoard.KING | side];
        int square = Long.numberOfTrailingZeros(king);
        int col = square & 7;

        long pawns = BitBoard.piece_bitboards[BitBoard.PAWN | side];
        long file_mask = SELF_COL[col] | NEIGHBOR_COL[col];

        long shield_mask = (side == BitBoard.WHITE)
            ? file_mask & WHITE_SHIELD
            : file_mask & BLACK_SHIELD;

        // Missing Pawns
        int shield_pawns = Long.bitCount(pawns & shield_mask);
        int missing_pawns = Math.max(3 - shield_pawns, 0);

        mg += missing_pawns * MISSING_PAWN;

        if (col == FILE_D || col == FILE_E) {
            mg += UNCASTLED_KING;
        }

        return mg;
    }

    public static int getRelativeEvaluation(int side) {
        int p = Math.min(phase, TOTAL_PHASE);
        int mg = mg_eval;
        int eg = eg_eval;

        // Pawn Scoring
        long wp_eval = getPawnEval(BitBoard.WHITE);
        long bp_eval = getPawnEval(BitBoard.BLACK);
        mg += (int) (wp_eval >>> 32) - (int) (bp_eval >>> 32);
        eg += (int) wp_eval - (int) bp_eval;

        // Rooks and Bishops
        long w_eval = getBasicPieceEval(BitBoard.WHITE);
        long b_eval = getBasicPieceEval(BitBoard.BLACK);
        mg += (int) (w_eval >>> 32) - (int) (b_eval >>> 32);
        eg += (int) w_eval - (int) b_eval;

        // King Pawn Shield
        mg += getKingSafetyMG(BitBoard.WHITE);
        mg -= getKingSafetyMG(BitBoard.BLACK);

        // Tapered Eval
        int eval = ((mg * p) + (eg * (TOTAL_PHASE - p))) / TOTAL_PHASE;
        
        // Mobility
        eval += MoveGenerator.getMobility(BitBoard.WHITE);
        eval -= MoveGenerator.getMobility(BitBoard.BLACK);

        // Fifty Move Tapering
        if (Math.abs(eval) < MATE_BOUND && BitBoard.halfmoves > 80) {
            eval = eval * (100 - BitBoard.halfmoves) / 20;
        }

        return (side == BitBoard.WHITE) ? eval : -eval;
    }

    public static int getDrawScore(long nodes) {
        return Evaluator.DRAW_SCORE - 1 + (int) (nodes & 2);
    }

    public static int getMGEval() {
        return mg_eval;
    }

    public static int getEGEval() {
        return eg_eval;
    }

    public static int getPhase() {
        return phase;
    }

    public static void setMGEval(int eval) {
        mg_eval = eval;
    }

    public static void setEGEval(int eval) {
        eg_eval = eval;
    }

    public static void setPhase(int p) {
        phase = p;
    }
}