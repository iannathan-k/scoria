package src.scoria;

import java.util.ArrayList;

import src.core.Game;
import src.core.MoveHandler;
import src.pieces.*;

public class Scoria {

    private static boolean cancel_mode = true;
    private static long cancel_time;
    private static int[] current_best_move = new int[2];
    private static int current_depth;

    public static void setCancelMode(boolean mode) {
        cancel_mode = mode;
    }

    public static int[] iterativeDeepener(byte[] board, boolean turn) {
        current_depth = 0;
        current_best_move = new int[2];
        cancel_time = System.nanoTime() + Game.THINK_TIME * 1_000_000L;
        while (System.nanoTime() < cancel_time) {
            current_depth++;
            int[] move = minimax(board, current_depth, Integer.MIN_VALUE, Integer.MAX_VALUE, turn);
            
            if (move[0] != Integer.MIN_VALUE) {
                current_best_move = move;
            }
            if (Math.abs(move[0]) == 10000) {
                break;
            }
        }
        Game.setLastThinkDepth(current_depth);
        Game.setLastThinkTime((System.nanoTime() - cancel_time) / 1_000_000 + Game.THINK_TIME);
        return current_best_move;
    }

    private static int getNodeType(int eval, int alpha, int beta) {
        if (eval >= beta) return Transposition.BETA_NODE;
        if (eval <= alpha) return Transposition.ALPHA_NODE;
        return Transposition.EXACT_NODE;
    }

    public static int perftCount(byte[] board, int depth, boolean turn) {
        if (depth == 0) return 1;

        int color = turn ? PieceData.WHITE : PieceData.BLACK;
        ArrayList<Integer> possible_moves = PieceHandler.getAllMoves(board, color);

        int node_count = 0;

        for (int move : possible_moves) {
            byte captured = MoveHandler.moveState(board, move, -1);
            node_count += perftCount(board, depth - 1, !turn);
            MoveHandler.undoState(board, move, captured, -1);
        }
        return node_count;
    }

    private static int heuristicScore(byte[] board, int move, int color) {
        int origin_pos = (move >> 8) & MoveHandler.POS_MASK;
        int target_pos = move & MoveHandler.POS_MASK;
        byte piece = board[origin_pos];
        byte captured = board[target_pos];

        int score = 0;

        if (captured != PieceData.EMPTY) {
            score += 3 * Evaluator.piece_points[captured & PieceData.TYPE_MASK];
            score -= Evaluator.piece_points[piece & PieceData.TYPE_MASK];
        }

        if ((move & MoveHandler.PROMO_MASK) != 0) {
            score += 500;
        }

        score += 3 * Evaluator.posWeight(piece & PieceData.TYPE_MASK, color, target_pos);

        return score;
    }

    public static int[] minimax(byte[] board, int depth, int alpha, int beta, boolean turn) {
        long board_hash = Zobrist.manualHash(board, turn);
        Transposition.BoardState entry = Transposition.getState(board_hash);

        if (entry != null && entry.getDepth() >= depth) {
            if (entry.isExact()) {
                return entry.getBestMove();
            }
            if (entry.isBeta() && entry.getBestMove()[0] >= beta) {
                return entry.getBestMove();
            }
            if (entry.isAlpha() && entry.getBestMove()[0] <= alpha) {
                return entry.getBestMove();
            }
        }

        if (depth == 0 || Evaluator.gameWinner(board, turn, board_hash) != Evaluator.NOT_OVER) {
            Game.move_count++;
            return new int[] {Evaluator.boardEval(board, turn, board_hash)};
        }

        int color = turn ? PieceData.WHITE : PieceData.BLACK;
        ArrayList<Integer> possible_moves = PieceHandler.getAllMoves(board, color);

        possible_moves.sort((move1, move2) -> Integer.compare(
            heuristicScore(board, move2, color), 
            heuristicScore(board, move1, color)
        ));

        int parent_alpha = alpha;
        int parent_beta = beta;

        if (turn) {
            int[] max_eval = {Integer.MIN_VALUE, -1};
            for (int move : possible_moves) {
                byte captured = MoveHandler.moveState(board, move, board_hash);
                int eval = minimax(board, depth - 1, alpha, beta, !turn)[0];
                MoveHandler.undoState(board, move, captured, board_hash);
                
                if (System.nanoTime() > cancel_time && cancel_mode) {
                    return new int[] {Integer.MIN_VALUE};
                }

                if (eval > max_eval[0]) {
                    max_eval[0] = eval;
                    max_eval[1] = move;
                }

                alpha = Math.max(eval, alpha);
                if (beta <= alpha) {
                    break;
                }
            }

            Transposition.addState(board_hash, new Transposition.BoardState(depth, max_eval, getNodeType(max_eval[0], parent_alpha, parent_beta)));
            return max_eval;

        } else {
            int[] min_eval = {Integer.MAX_VALUE, -1};
            for (int move : possible_moves) {
                byte captured = MoveHandler.moveState(board, move, board_hash);
                int eval = minimax(board, depth - 1, alpha, beta, !turn)[0];
                MoveHandler.undoState(board, move, captured, board_hash);

                if (System.nanoTime() > cancel_time && cancel_mode) {
                    return new int[] {Integer.MIN_VALUE};
                }

                if (eval < min_eval[0]) {
                    min_eval[0] = eval;
                    min_eval[1] = move;
                }
                
                beta = Math.min(eval, beta);
                if (beta <= alpha) {
                    break;
                }
            }

            Transposition.addState(board_hash, new Transposition.BoardState(depth, min_eval, getNodeType(min_eval[0], parent_alpha, parent_beta)));
            return min_eval;
        }
    }
}