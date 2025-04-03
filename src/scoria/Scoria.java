package src.scoria;

import java.util.ArrayList;

import src.core.Game;
import src.core.Interface;
import src.core.MoveHandler;
import src.pieces.*;

public class Scoria {

    private static boolean cancel_mode = true;
    private static long cancel_time;
    private static int[] current_best_move = new int[2];
    private static int current_depth;

    private static int[][] history_table = new int[2][16191];

    public static void setCancelMode(boolean mode) {
        cancel_mode = mode;
    }

    public static void clearHistoryTable() {
        history_table = new int[2][16191];
    }

    public static int[] iterativeDeepener(byte[] board, boolean turn) {
        int sign = turn ? 1 : -1;
        current_depth = 0;
        current_best_move = new int[2];
        cancel_time = System.nanoTime() + Game.MAX_TIME * 1_000_000L;

        while (System.nanoTime() < cancel_time) {
            current_depth++;
            int[] move = negamax(board, current_depth, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, turn, sign, false);
            
            if (move[0] != Integer.MIN_VALUE) {
                current_best_move = move;
            }
        }
        
        Game.setLastThinkDepth(current_depth);
        Game.setLastThinkTime((System.nanoTime() - cancel_time) / 1_000_000 + Game.MAX_TIME);
        return current_best_move;
    }

    public static int[] uciGoIterative(byte[] board, boolean turn, int MAX_DEPTH, long MAX_TIME) {
        int sign = turn ? 1 : -1;
        current_depth = 0;
        current_best_move = new int[2];
        cancel_time = System.nanoTime() + MAX_TIME * 1_000_000L;

        long start = System.currentTimeMillis();
        while (System.nanoTime() < cancel_time && current_depth < MAX_DEPTH) {
            current_depth++;
            
            int[] move = negamax(board, current_depth, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, turn, sign, false);
            
            if (move[0] != Integer.MIN_VALUE) {
                current_best_move = move;
                long time = System.currentTimeMillis() - start;
                int nodes = Game.getMoveCount();
                System.out.println(
                    "info depth " + current_depth + 
                    " score " + move[0] +
                    " nodes " + nodes + 
                    " time " + time +
                    " pv " + Interface.moveToUci(move[1])
                );
            }
        }

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
        int piece_type = board[origin_pos] & PieceData.TYPE_MASK;
        byte captured = board[target_pos];

        int score = 3 * Evaluator.posWeight(piece_type, color, target_pos);
        score -= Evaluator.posWeight(piece_type, color, origin_pos);

        if (captured != PieceData.EMPTY) {
            score += 100;
            score += 3 * Evaluator.piece_points[captured & PieceData.TYPE_MASK];
            score -= Evaluator.piece_points[piece_type];
        }

        score += Math.min(history_table[color >> 3][move & 0xFFFF], 100);

        return score;
    }

    private static boolean isCapture(byte[] board, int move) {
        return board[move & MoveHandler.POS_MASK] != PieceData.EMPTY;
    }

    public static int[] negamax(byte[] board, int depth, int alpha, int beta, boolean turn, int sign, boolean is_null) {
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
            return new int[] {sign * Evaluator.boardEval(board, turn, board_hash, depth)};
        }

        int color = turn ? PieceData.WHITE : PieceData.BLACK;
        if (depth > 3 && !PieceHandler.kingUnderAttack(board, color) && !is_null) {
            int null_eval = -negamax(board, depth - 4, -beta, -beta + 1, !turn, -sign, true)[0];
            if (null_eval >= beta) {
                return new int[] {beta};
            }
        }

        ArrayList<Integer> possible_moves = PieceHandler.getAllMoves(board, color);
        possible_moves.sort((move1, move2) -> Integer.compare(
            heuristicScore(board, move2, color), 
            heuristicScore(board, move1, color)
        ));

        int parent_alpha = alpha;
        boolean is_first = true;

        int[] best_eval = {Integer.MIN_VALUE, -1};
        for (int move : possible_moves) {

            int reduction = 0;
            if (!is_first && depth > 3 && !is_null && !isCapture(board, move)) {
                reduction = depth >> 1;
            }

            byte captured = MoveHandler.moveState(board, move, board_hash);

            int eval;
            if (is_first) {
                eval = -negamax(board, depth - 1, -beta, -alpha, !turn, -sign, false)[0];
                is_first = false;
            } else {
                eval = -negamax(board, depth - 1 - reduction, -alpha - 1, -alpha, !turn, -sign, true)[0];

                if (eval > alpha && eval < beta) {
                    eval = -negamax(board, depth - 1, -beta, -alpha, !turn, -sign, false)[0];
                }
            }

            MoveHandler.undoState(board, move, captured, board_hash);

            if (System.nanoTime() > cancel_time && cancel_mode) {
                return new int[] {Integer.MIN_VALUE};
            }

            if (eval > best_eval[0]) {
                best_eval[0] = eval;
                best_eval[1] = move;
            }

            alpha = Math.max(eval, alpha);
            if (beta <= alpha) {
                history_table[color >> 3][move & 0xFFFF] += depth * depth;
                break;
            }
        }

        Transposition.addState(board_hash, new Transposition.BoardState(depth, best_eval, getNodeType(best_eval[0], parent_alpha, beta)));

        return best_eval;
    }
}