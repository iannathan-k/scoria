package src.engine;

import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.utils.GameStack;

public class Evaluator {
    public static final int MATE_SCORE = 10000;
    public static final int MATE_BOUND = MATE_SCORE - 1000;
    public static final int DRAW_SCORE = 0;
    public static final int NULL_EVAL = -1;

    private static int base_eval;

    private static final int[][] POSITIONAL_WEIGHTS = {
        // Pawns
        {
            0,   0,  0,  0,  0,  0,  0,  0,
           50,  50, 50, 50, 50, 50, 50, 50,
           10,  10, 20, 30, 30, 20, 10, 10,
            5,   5, 10, 25, 25, 10,  5,  5,
            0,   0,  0, 20, 20,  0,  0,  0,
            5,  -5,-10,  0,  0,-10, -5,  5,
            5,  10, 10,-20,-20, 10, 10,  5,
            0,   0,  0,  0,  0,  0,  0,  0
        },

        // Knights
        {
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50
        },

        // Bishops
        {
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20
        },

        // Rooks
        {
             0,   0,  0,  0,  0,  0,  0,  0,
             5,  10, 10, 10, 10, 10, 10,  5,
            -5,   0,  0,  0,  0,  0,  0, -5,
            -5,   0,  0,  0,  0,  0,  0, -5,
            -5,   0,  0,  0,  0,  0,  0, -5,
            -5,   0,  0,  0,  0,  0,  0, -5,
            -5,   0,  0,  0,  0,  0,  0, -5,
             0,   0,  0,  5,  5,  0,  0,  0
        },

        // Queens
        {
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
            -5,   0,  5,  5,  5,  5,  0, -5,
             0,   0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20
        },

        // Kings
        {
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
             20, 20,  0,  0,  0,  0, 20, 20,
             20, 30, 10,  0,  0, 10, 30, 20
        }
    };

    public static final int[] PIECE_VALUES = {
          100,   -100,
          320,   -320,
          330,   -330,
          500,   -500,
          900,   -900,
        10000, -10000
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
            if ((piece & BitBoard.PIECE_MASK) == BitBoard.WHITE_PAWN) {
                return false;
            }
        }

        return false;
    }

    public static int getMateScore(int turn, int ply) {
        int square = Long.numberOfTrailingZeros(
            BitBoard.piece_bitboards[BitBoard.WHITE_KING | turn]
        );

        if (MoveGenerator.isUnderAttack(square, turn)) {
            return -MATE_SCORE + ply;
        }
        return DRAW_SCORE;
    }

    public static int getPositionalWeight(int piece, int square) {
        if ((piece & BitBoard.COLOR_MASK) == BitBoard.WHITE) {
            return POSITIONAL_WEIGHTS[(piece & BitBoard.PIECE_MASK) >> 1][square ^ 56];
        } else {
            return -POSITIONAL_WEIGHTS[(piece & BitBoard.PIECE_MASK) >> 1][square];
        }
    }
    
    // FIXME: Weights based on game phase
    // FIXME: King shield, Passed Pawn, Doubled Pawns
    public static void manualBaseEvaluation() {
        int eval = 0;

        // FIXME: Could be Unbranched
        for (int i = 0; i < 12; i++) {
            long bitboard = BitBoard.piece_bitboards[i];
            int piece_count = Long.bitCount(bitboard);
            eval += piece_count * PIECE_VALUES[i];

            while (bitboard != 0) {
                int square = Long.numberOfTrailingZeros(bitboard);
                eval += getPositionalWeight(i, square);

                bitboard &= bitboard - 1;
            }
        }

        base_eval = eval;
    }

    public static void updateEvaluation(int move) {
        int origin = (move >> 6) & MoveHandler.POSITION_MASK;
        int target = move & MoveHandler.POSITION_MASK;
        int piece = (move >> 12) & MoveHandler.PIECE_MASK;
        int side = piece & BitBoard.COLOR_MASK;
        int capture = BitBoard.getPieceAt(target, side ^ 1);

        base_eval += getPositionalWeight(piece, target);
        base_eval -= getPositionalWeight(piece, origin);

        if (capture != BitBoard.NO_PIECE) {
            base_eval -= PIECE_VALUES[capture];
            base_eval -= getPositionalWeight(capture, target);
        }

        if ((move & MoveHandler.PASSANT_FLAG) != 0) {
            int captured_square = (side == BitBoard.WHITE) ? target - 8 : target + 8;
            base_eval -= PIECE_VALUES[BitBoard.WHITE_PAWN | side ^ 1];
            base_eval -= getPositionalWeight(BitBoard.WHITE_PAWN | side ^ 1, captured_square);
        }

        if ((move & MoveHandler.PROMOTED_MASK) != 0) {
            int promoted_piece = (move & MoveHandler.PROMOTED_MASK) >>> 16;
            base_eval -= PIECE_VALUES[piece];
            base_eval -= getPositionalWeight(piece, target);
            base_eval += PIECE_VALUES[promoted_piece];
            base_eval += getPositionalWeight(promoted_piece, target);
        }

        if ((move & MoveHandler.CASTLE_FLAG) != 0) {
            if (target == MoveGenerator.SQUARE_G1) {
                base_eval -= getPositionalWeight(BitBoard.WHITE_ROOK, MoveGenerator.SQUARE_H1);
                base_eval += getPositionalWeight(BitBoard.WHITE_ROOK, MoveGenerator.SQUARE_F1);
            } else if (target == MoveGenerator.SQUARE_C1) {
                base_eval -= getPositionalWeight(BitBoard.WHITE_ROOK, MoveGenerator.SQUARE_A1);
                base_eval += getPositionalWeight(BitBoard.WHITE_ROOK, MoveGenerator.SQUARE_D1);
            } else if (target == MoveGenerator.SQUARE_G8) {
                base_eval -= getPositionalWeight(BitBoard.BLACK_ROOK, MoveGenerator.SQUARE_H8);
                base_eval += getPositionalWeight(BitBoard.BLACK_ROOK, MoveGenerator.SQUARE_F8);
            } else {
                base_eval -= getPositionalWeight(BitBoard.BLACK_ROOK, MoveGenerator.SQUARE_A8);
                base_eval += getPositionalWeight(BitBoard.BLACK_ROOK, MoveGenerator.SQUARE_D8);
            }
        }
    }

    public static int getRelativeEvaluation(int side) {
        int eval = base_eval;
        eval += MoveGenerator.getMobility(BitBoard.WHITE);
        eval -= MoveGenerator.getMobility(BitBoard.BLACK);
        return (side == BitBoard.WHITE) ? eval : -eval;
    }

    public static int getBaseEvaluation() {
        return base_eval;
    }

    public static void setBaseEvaluation(int eval) {
        base_eval = eval;
    }
}
