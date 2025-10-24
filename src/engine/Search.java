package src.engine;

import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.game.Precomputer;
import src.game.Zobrist;
import src.utils.GameStack;
import src.utils.MoveList;

public class Search {

    private static int negascout(int depth, int alpha, int beta, int moving_side) {
        // Probe TT

        if (depth == 0) {
            return Evaluator.getStaticEvaluation();
        }

        // Razoring & Deep Razoring

        // Reverse Futility Pruning

        // Futility Pruning

        // Null Move Pruning

        MoveList move_list = MoveGenerator.generateAllMoves(moving_side);
        
        // Move Ordering

        int best_score = Integer.MIN_VALUE;

        for (int i = 0; i < move_list.size(); i++) {
            int move = move_list.get(i);

            // Late Move Reduction

            // Negascout

            MoveHandler.doMove(move);

            int eval = -negascout(depth - 1, -beta, -alpha, moving_side ^ 1);

            MoveHandler.undoMove();

            best_score = Math.max(best_score, eval);

            // History Heuristic

            alpha = Math.max(alpha, eval);

            if (alpha >= beta) {
                break;
            }
        }

        // Age History Heuristic

        // Transposition Store

        return best_score;
    }

    public static void main(String[] args) {
        BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        Precomputer.initAllMoveTables();
        GameStack.initGameStack();
        Zobrist.initZobristTable();

        long start = System.nanoTime();
        int score = negascout(1 , Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, BitBoard.WHITE);
        System.out.println("Time: " + (System.nanoTime() - start) / 1_000_000 + "ms");
        System.out.println("Score: " + score);
    }
}
