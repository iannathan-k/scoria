package src.engine;

import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.utils.GameStack;

public class Evaluator {
    public static final int MATE_SCORE = 10000;
    public static final int MATE_BOUND = 9000;
    public static final int DRAW_SCORE = 0;
    public static final int NULL_EVAL = -1;
    public static final int NULL_PHASE = -1;

    private static final int KNIGHT_PHASE = 1;
    private static final int BISHOP_PHASE = 1;
    private static final int ROOK_PHASE = 2;
    private static final int QUEEN_PHASE = 4;
    private static final int TOTAL_PHASE = 24;

    private static final int MG_INDEX = 0;
    private static final int EG_INDEX = 1;
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

    private static int mg_eval;
    private static int eg_eval;
    private static int phase;

    // Texel's Tuning Method

    public static int MISSING_PAWN      = -25;
    public static int UNCASTLED_KING    = -40;
    public static int BISHOP_MG         =  38;
    public static int BISHOP_EG         =  50;
    public static int ROOK_OPEN         =  34;
    public static int ROOK_SEMI         =  20;
    public static int ISOLATED_PAWN     = -17;
    public static int DOUBLED_PAWN      = -12;
    public static int[] PASSED_MG       = {0, -15, -17, -6, 7, 23, 41, 0};
    public static int[] PASSED_EG       = {0, 39, 37, 49, 71, 114, 154, 0};

    public static final int[] PIECE_VALUES = {
            0,      -0,
          342,    -342,
          347,    -347,
          534,    -534,
         1059,   -1059,
        20000,  -20000
    };

    public static final int[][] POSITIONAL_WEIGHTS = {
        // Pawns
        {
              0,    0,    0,    0,    0,    0,    0,    0, 
            169,  134,  131,  122,  124,  146,  107,  106, 
            130,  119,  126,  101,  108,  134,  123,  109, 
            103,   92,   98,   93,  106,  102,   95,   93, 
             88,   80,   83,   95,   96,   91,   84,   86, 
             85,   78,   82,   79,   91,   81,   85,   78, 
             83,   75,   74,   66,   80,   86,   84,   72, 
              0,    0,    0,    0,    0,    0,    0,    0, 
        },
        
        // Knights
        {
            -102,   14,   17,    2,  -12,  -17,   -3, -113, 
             -31,   -1,   29,   26,   11,   29,  -25,  -16, 
             -11,   34,   37,   56,   72,   75,   37,   11, 
              11,   29,   43,   60,   51,   64,   31,   28, 
               3,   10,   31,   30,   43,   36,   23,    0, 
             -22,    3,   17,   27,   28,   21,   17,  -19, 
             -44,  -21,   -6,    8,    3,    3,  -10,  -14, 
             -37,  -32,  -40,  -26,  -17,   -5,  -31,  -50, 
        },

        // Bishops
        {
            -13,   21,    2,   22,    2,  -10,  -12,   -4, 
             12,   14,   25,    3,   24,   32,   21,    6, 
             21,   36,   37,   33,   33,   50,   39,   37, 
             12,   34,   31,   56,   43,   34,   32,   32, 
              4,   13,   30,   35,   37,   29,   19,    6, 
              7,   25,   27,   34,   30,   28,   26,   18, 
             15,   14,   26,   10,   19,   25,   28,   13, 
             -5,   27,   -5,    8,    1,   -6,    6,   -9, 
        },

        // Rooks
        {
            41,   43,   50,   51,   43,   29,   30,   63, 
            41,   51,   42,   36,   29,   50,   43,   59, 
            40,   42,   34,   27,   45,   49,   54,   33, 
            23,   34,   31,   23,   27,   16,   27,   35, 
            19,    5,   24,    7,    9,    3,   17,   20, 
            13,    3,   -6,    8,    9,   12,   21,    9, 
            10,   10,   13,   12,   11,   13,   12,    5, 
            13,   13,   15,   15,   17,   22,   17,   16, 
        },

        // Queens
        {
            62,   52,   87,   72,   70,  124,   90,   82, 
            32,   44,   63,   79,   68,   97,   90,   97, 
            45,   60,   65,  101,  102,  115,  120,  104, 
            51,   64,   80,   77,   80,   82,   81,   79, 
            50,   61,   70,   72,   70,   68,   77,   77, 
            41,   59,   52,   62,   59,   65,   76,   68, 
            42,   59,   61,   64,   61,   59,   43,   56, 
            47,   33,   52,   51,   45,   48,   18,   45, 
        },

        // Kings MG
        {
             -82,   48,    9,   17,  -35,    3, -152,  -42, 
             -40,    8,   70,  -17,    9,  -22,  -45, -122, 
            -146, -118,    7,  -73,  -14,  -15,  -29, -181, 
            -128, -129, -126,  -14,  -27, -104, -178,  -87, 
             -33,  -73,  -97,  -55, -112,  -70,  -99, -130, 
             -62,  -57,  -74,  -26,  -26,  -77,  -51,  -71, 
              21,   -6,   15,    6,  -21,  -23,    1,   40, 
              11,   41,   33,  -23,   42,  -46,   34,   48, 
        }
    };

    public static final int[] KINGWEIGHTS_EG = {
        -62,  -33,   10,  -30,  -10,  -50,  -38,  -80, 
         -2,   56,   40,   60,   32,   36,   37,  -10, 
         17,   60,   66,   57,   59,   45,   58,   31, 
          6,   59,   50,   53,   59,   63,   54,    5, 
        -34,   23,   56,   59,   46,   35,   23,   16, 
        -39,   -5,   36,   28,   30,   18,    5,  -14, 
        -29,    3,  -12,   20,   28,    5,  -17,  -49, 
        -65,  -58,  -43,    2,  -50,  -18,  -56,  -80, 
    };

    // FIXME: Implement fifty move rule
    // FIXME: Recheck threefold repetition
    public static boolean isThreeFoldRepetition(long hash) {
        int count = 1;
        for (int i = GameStack.size() - 2; i >= 0; i--) {
            if (GameStack.getHashAt(i) == hash) {
                count++;

                if (count >= 3) return true;
            }

            if (GameStack.getCapturedAt(i) != BitBoard.NO_PIECE) {
                return false;
            }

            int piece = (GameStack.getMoveAt(i) >> 12) & MoveHandler.PIECE_MASK;
            if ((piece & BitBoard.PIECE_MASK) == BitBoard.PAWN) {
                return false;
            }
        }

        return false;
    }

    public static int getMateScore(int turn, int ply) {
        int square = Long.numberOfTrailingZeros(
            BitBoard.piece_bitboards[BitBoard.KING | turn]
        );

        if (MoveGenerator.isUnderAttack(square, turn)) {
            return -MATE_SCORE + ply;
        }
        return DRAW_SCORE;
    }

    public static int getMGWeights(int piece, int square) {
        if ((piece & BitBoard.COLOR_MASK) == BitBoard.WHITE) {
            return POSITIONAL_WEIGHTS[(piece & BitBoard.PIECE_MASK) >> 1][square ^ 56];
        } else {
            return -POSITIONAL_WEIGHTS[(piece & BitBoard.PIECE_MASK) >> 1][square];
        }
    }

    private static int getEGWeights(int piece, int square) {
        if (piece == BitBoard.WHITE_KING) {
            return KINGWEIGHTS_EG[square ^ 56];
        } else if (piece == BitBoard.BLACK_KING) {
            return -KINGWEIGHTS_EG[square];
        }

        if ((piece & BitBoard.COLOR_MASK) == BitBoard.WHITE) {
            return POSITIONAL_WEIGHTS[(piece & BitBoard.PIECE_MASK) >> 1][square ^ 56];
        } else {
            return -POSITIONAL_WEIGHTS[(piece & BitBoard.PIECE_MASK) >> 1][square];
        }
    }

    // FIXME: Could use an array instead
    private static int getPiecePhase(int piece) {
        switch (piece & BitBoard.PIECE_MASK) {
            case BitBoard.KNIGHT -> { return KNIGHT_PHASE; }
            case BitBoard.BISHOP -> { return BISHOP_PHASE; }
            case BitBoard.ROOK   -> { return ROOK_PHASE; }
            case BitBoard.QUEEN  -> { return QUEEN_PHASE; }
            default -> { return 0; }
        }
    }

    // FIXME: Weights based on game phase
    // FIXME: King shield, Passed Pawn, Doubled Pawns
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
                phase += getPiecePhase(i);

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
            phase -= getPiecePhase(capture);
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

            phase += getPiecePhase(p_piece);
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

    private static int[] getPawnEval(int side) {
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

        return new int[] {mg, eg};
    }

    private static int[] getBasicPieceEval(int side) {
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

        return new int[] {mg, eg};
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
        int[] wp_eval = getPawnEval(BitBoard.WHITE);
        int[] bp_eval = getPawnEval(BitBoard.BLACK);
        mg += wp_eval[MG_INDEX] - bp_eval[MG_INDEX];
        eg += wp_eval[EG_INDEX] - bp_eval[EG_INDEX];

        // Rooks and Bishops
        int[] w_eval = getBasicPieceEval(BitBoard.WHITE);
        int[] b_eval = getBasicPieceEval(BitBoard.BLACK);
        mg += w_eval[MG_INDEX] - b_eval[MG_INDEX];
        eg += w_eval[EG_INDEX] - b_eval[EG_INDEX];

        // King Pawn Shield
        mg += getKingSafetyMG(BitBoard.WHITE);
        mg -= getKingSafetyMG(BitBoard.BLACK);

        // Tapered Eval
        int eval = ((mg * p) + (eg * (TOTAL_PHASE - p))) / TOTAL_PHASE;
        
        // Mobility
        eval += MoveGenerator.getMobility(BitBoard.WHITE);
        eval -= MoveGenerator.getMobility(BitBoard.BLACK);

        return (side == BitBoard.WHITE) ? eval : -eval;
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