package src.engine;

import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.game.Precomputer;
import src.game.Zobrist;
import src.utils.GameStack;

public class Evaluator {
    public static final int MATE_SCORE = 10000;
    public static final int MATE_BOUND = MATE_SCORE - 1000;
    public static final int DRAW_SCORE = 0;

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

    // FIXME: THIS NEEDS TO BE RECALIBRATED
    public static final int[] PIECE_VALUES = {
        100, -100,
        320, -320,
        330, -330,
        500, -500,
        900, -900,
          0,    0
    };

    // FIXME: Implement fifty move rule
    // FIXME: Recheck threefold repetition
    public static final boolean isThreeFoldRepetition(long hash) {
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

    public static final int getMateScore(int turn, int ply) {
        int square = Long.numberOfTrailingZeros(BitBoard.piece_bitboards[BitBoard.WHITE_KING + turn]);
        if (MoveGenerator.isUnderAttack(square, turn)) {
            return -MATE_SCORE + ply;
        }
        return DRAW_SCORE;
    }

    public static final int getPositionalWeight(int piece, int square) {
        if ((piece & BitBoard.COLOR_MASK) == BitBoard.WHITE) {
            return POSITIONAL_WEIGHTS[(piece & BitBoard.PIECE_MASK) >> 1][square ^ 56];
        } else {
            return -POSITIONAL_WEIGHTS[(piece & BitBoard.PIECE_MASK) >> 1][square];
        }
    }

    // FIXME: Implement mobility evaluation
    // FIXME: Weights based on game phase
    // FIXME: King shield, Passed Pawn, Doubled Pawns
    public static int getStaticEvaluation(int side) {
        int color = (side == BitBoard.WHITE) ? 1 : -1;
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

        return color * eval;
    }

    public static void main(String[] args) {
        BitBoard.initBoardByFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ");
        Precomputer.initAllMoveTables();
        Zobrist.initZobristTable();

        // BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        int evaluation = getStaticEvaluation(BitBoard.WHITE);
        System.out.println("Static Evaluation: " + evaluation);
    }
}
