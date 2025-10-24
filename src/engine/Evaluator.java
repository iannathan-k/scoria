package src.engine;

import src.game.BitBoard;
import src.game.Precomputer;
import src.game.Zobrist;
import src.utils.GameStack;

public class Evaluator {
    private static final int[] PAWN_WEIGHTS = {
         0,   0,  0,  0,  0,  0,  0,  0,
        50,  50, 50, 50, 50, 50, 50, 50,
        10,  10, 20, 30, 30, 20, 10, 10,
         5,   5, 10, 25, 25, 10,  5,  5,
         0,   0,  0, 20, 20,  0,  0,  0,
         5,  -5,-10,  0,  0,-10, -5,  5,
         5,  10, 10,-20,-20, 10, 10,  5,
         0,   0,  0,  0,  0,  0,  0,  0
    };

    private static final int[] KNIGHT_WEIGHTS = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    };

    private static final int[] BISHOP_WEIGHTS = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };

    private static final int[] ROOK_WEIGHTS = {
         0,   0,  0,  0,  0,  0,  0,  0,
         5,  10, 10, 10, 10, 10, 10,  5,
        -5,   0,  0,  0,  0,  0,  0, -5,
        -5,   0,  0,  0,  0,  0,  0, -5,
        -5,   0,  0,  0,  0,  0,  0, -5,
        -5,   0,  0,  0,  0,  0,  0, -5,
        -5,   0,  0,  0,  0,  0,  0, -5,
         0,   0,  0,  5,  5,  0,  0,  0
    };

    private static final int[] QUEEN_WEIGHTS = {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
        -5,   0,  5,  5,  5,  5,  0, -5,
         0,   0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] KING_WEIGHTS = {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20,
         20, 30, 10,  0,  0, 10, 30, 20
    };

    private static final int[][] POSITIONAL_WEIGHTS = {
        PAWN_WEIGHTS, 
        KNIGHT_WEIGHTS, 
        BISHOP_WEIGHTS, 
        ROOK_WEIGHTS, 
        QUEEN_WEIGHTS, 
        KING_WEIGHTS
    };

    private static final int[] PIECE_VALUES = {
         100,  320,  330,  500,  900, 0,
        -100, -320, -330, -500, -900, 0
    };

    // FIXME: Implement threefold repetition
    // FIXME: Implement fifty-move rule
    // FIXME: Implement checkmate/stalemate detection
    // FIXME: Implement positional evaluation
    // FIXME: Implement mobility evaluation
    public static int getStaticEvaluation() {
        int eval = 0;

        // FIXME: Could be Unbranched
        for (int i = 0; i < 12; i++) {
            long bitboard = BitBoard.piece_bitboards[i];
            int piece_count = Long.bitCount(bitboard);
            eval += piece_count * PIECE_VALUES[i];

            // FIXME: Could be Unbranched
            // FIXME: Could not use %
            while (bitboard != 0) {
                int pos = Long.numberOfTrailingZeros(piece_count);
                int side = (BitBoard.getPieceColor(i) == BitBoard.WHITE) ? 1 : -1;

                eval += side * POSITIONAL_WEIGHTS[i % 6][pos];

                bitboard &= bitboard - 1;
            }
        }

        return eval;
    }

    public static void main(String[] args) {
        BitBoard.initBoardByFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R");
        Precomputer.initAllMoveTables();
        GameStack.initGameStack();
        Zobrist.initZobristTable();

        // BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        int evaluation = getStaticEvaluation();
        System.out.println("Static Evaluation: " + evaluation);
    }
}
