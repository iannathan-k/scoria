package src.engine;

import java.util.Arrays;

import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.game.Precomputer;
import src.game.Zobrist;
import src.user.Uci;
import src.utils.MoveList;

public class Search {

    private static final int INFINITY = Integer.MAX_VALUE - 1;

    private static long cancel_time;
    private static long node_count;
    private static int current_depth;

    public static void iterativeDeepener(int max_depth, int max_time) {
        int start_alpha = -INFINITY;
        int start_beta = INFINITY;
        
        current_depth = 0;
        long start = System.currentTimeMillis();

        cancel_time = start + max_time;
        node_count = 0;

        while (current_depth < max_depth && System.currentTimeMillis() < cancel_time) {
            current_depth++;
            int eval = negascout(current_depth, 0, start_alpha, start_beta, BitBoard.moving_side);

            if (eval == Integer.MIN_VALUE) return;

            // Aspiration Windows
            if (eval <= start_alpha || eval >= start_beta) {
                // FIXME: Remove Later
                System.out.println("RESTART");
                start_alpha = -INFINITY;
                start_beta = INFINITY;
                current_depth--;
                continue;
            }

            start_alpha = eval - 70;
            start_beta = eval + 70;

            // FIXME: Add nps
            // FIXME: Add hashfull
            System.out.println(
                "info depth " + current_depth +
                " score cp " + eval +
                " nodes " + node_count +
                " time " + (System.currentTimeMillis() - start) +
                " pv " + Uci.moveListToUciString(collectPrincipalVariation(current_depth))
            );

            Transposition.nextGeneration();
        }
    }

    private static MoveList collectPrincipalVariation(int depth) {
        long hash = Zobrist.getZobristHash();
        MoveList principal_variation = new MoveList(depth);

        if (!Transposition.doesExist(hash) || depth == 0) {
            return principal_variation;
        }

        int move = Transposition.getBestMove(hash);

        principal_variation.add(move);

        MoveHandler.doMove(move);

        principal_variation.addAll(collectPrincipalVariation(depth - 1));

        MoveHandler.undoMove();

        return principal_variation;
    }

    private static int heuristicScore(int move, int best_move) {
        if (move == best_move) return 10000;

        int origin = (move >> 6) & MoveHandler.POSITION_MASK;
        int target = move & MoveHandler.POSITION_MASK;
        int piece = (move >> 12) & MoveHandler.PIECE_MASK;
        int captured = BitBoard.getPieceAt(target);
        
        int score = 1000;
        score += Evaluator.getPositionalWeight(piece, target);
        score -= Evaluator.getPositionalWeight(piece, origin);

        if (captured != BitBoard.NO_PIECE) {
            score += Evaluator.PIECE_VALUES[captured & BitBoard.PIECE_MASK];
        }

        return score;
    }

    // FIXME: Consider partial sorting
    private static void sortMoveList(MoveList move_list) {
        int best_move = Transposition.getBestMove(Zobrist.getZobristHash());

        long[] scored_moves = new long[move_list.size()];
        for (int i = 0; i < scored_moves.length; i++) {
            int move = move_list.get(i);
            scored_moves[i] = (((long) heuristicScore(move, best_move)) << 32) | move;
        }

        Arrays.sort(scored_moves);
        move_list.clear();

        for (int i = scored_moves.length - 1; i >= 0; i--) {
            move_list.add((int) scored_moves[i]);
        }
    }

    private static int quiescence(int alpha, int beta, int turn) {
        int static_eval = Evaluator.getStaticEvaluation(turn);

        if (static_eval >= beta) {
            return static_eval;
        }
        if (alpha < static_eval) {
            alpha = static_eval;
        }

        MoveList noisy_list = MoveGenerator.generateNoisyMoves(turn);
        sortMoveList(noisy_list);

        int best_eval = static_eval;
        for (int i = 0; i < noisy_list.size(); i++) {
            int move = noisy_list.get(i);

            MoveHandler.doMove(move);

            int eval = -quiescence(-beta, -alpha, turn ^ 1);

            MoveHandler.undoMove();

            best_eval = Math.max(best_eval, eval);
            alpha = Math.max(alpha, eval);

            if (alpha >= beta) {
                break;
            }
        }

        return best_eval;
    }

    private static int negascout(int depth, int ply, int alpha, int beta, int turn) {
        long board_hash = Zobrist.getZobristHash();

        if (ply > 0 && Evaluator.isThreeFoldRepetition(board_hash)) {
            return Evaluator.DRAW_SCORE;
        }

        // Transposition Probe
        if (Transposition.doesExist(board_hash) && Transposition.getDepth(board_hash) >= depth) {
            int tt_score = Transposition.getScore(board_hash, ply);
            if (Transposition.isExact(board_hash)) {
                return tt_score;
            }
            if (Transposition.isBeta(board_hash) && tt_score >= beta) {
                return tt_score;
            }
            if (Transposition.isAlpha(board_hash) && tt_score <= alpha) {
                return tt_score;
            }
        }

        if (depth == 0) {
            node_count++;
            return quiescence(alpha, beta, turn);
            // return Evaluator.getStaticEvaluation(turn);
        }

        MoveList move_list = MoveGenerator.generateAllMoves(turn);

        if (move_list.isEmpty()) {
            return Evaluator.getMateScore(turn, ply);
        }

        sortMoveList(move_list);

        int parent_alpha = alpha;
        int best_score = Integer.MIN_VALUE;
        int best_move = 0;

        for (int i = 0; i < move_list.size(); i++) {
            int move = move_list.get(i);

            MoveHandler.doMove(move);

            // Null Window
            int eval;
            if (i == 0) {
                eval = -negascout(depth - 1, ply + 1, -beta, -alpha, turn ^ 1);
            } else {
                eval = -negascout(depth - 1, ply + 1, -alpha - 1, -alpha, turn ^ 1);

                if (eval > alpha && eval < beta) {
                    eval = -negascout(depth - 1, ply + 1, -beta, -alpha, turn ^ 1);
                }
            }

            MoveHandler.undoMove();

            if (System.currentTimeMillis() > cancel_time) {
                return Integer.MIN_VALUE;
            }

            if (eval > best_score) {
                best_move = move;
                best_score = eval;
            }

            alpha = Math.max(alpha, eval);

            if (alpha >= beta) {
                break;
            }
        }

        byte node_type;
        if (best_score >= beta) {
            node_type = Transposition.BETA_NODE;
        } else if (best_score <= parent_alpha) {
            node_type = Transposition.ALPHA_NODE;
        } else {
            node_type = Transposition.EXACT_NODE;
        }

        Transposition.addTransposition(
            board_hash, 
            depth, 
            best_score, 
            node_type, 
            best_move, 
            ply
        );

        return best_score;
    }

    public static void main(String[] args) {
        BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        // BitBoard.initBoardByFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ");
        // BitBoard.initBoardByFen("8/7k/8/8/8/Q7/K7/8 w - - 0 1");
        // BitBoard.initBoardByFen("8/7k/8/8/2K5/6Q1/8/8 b - - 5 3");
        Precomputer.initAllMoveTables();
        Zobrist.initZobristTable();

        iterativeDeepener(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
}