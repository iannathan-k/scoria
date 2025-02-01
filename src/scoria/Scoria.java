package src.scoria;

import java.util.ArrayList;

import src.core.Game;
import src.core.MoveHandler;
import src.pieces.*;
import src.pieces.enums.*;

public class Scoria {

    private static long cancel_time;
    private static final long MAX_THINK_TIME = Game.THINK_TIME * 1_000_000;
    private static int[][] current_best_move = new int[3][];
    private static int current_depth;

    public static int[][] iterativeDeepener(Piece[][] board, boolean turn) {
        current_depth = 0;
        current_best_move = new int[3][];
        cancel_time = System.nanoTime() + MAX_THINK_TIME;
        while (System.nanoTime() < cancel_time) {
            current_depth++;
            current_best_move = minimax(board, current_depth, Integer.MIN_VALUE, Integer.MAX_VALUE, turn);
        }
        Game.setLastThinkDepth(current_depth);
        Game.setLastThinkTime((System.nanoTime() - cancel_time + MAX_THINK_TIME) / 1_000_000);
        return current_best_move;
    }

    private static int getNodeType(int eval, int alpha, int beta) {
        if (eval >= beta) {
            return Transposition.BETA_NODE;
        }
        if (eval <= alpha) {
            return Transposition.ALPHA_NODE;
        }
        return Transposition.EXACT_NODE;
    }

    private static int heuristicScore(Piece[][] board, int[][] move, PieceColor color) {
        int[] origin_pos = move[0];
        int[] target_pos = move[1];
        Piece piece = board[origin_pos[0]][origin_pos[1]];
        Piece capture = board[target_pos[0]][target_pos[1]];

        int score = 0;

        if (!(capture instanceof Empty)) {
            score += 3 * capture.getPoints() - piece.getPoints();
        }

        if (piece instanceof Pawn && (target_pos[0] == 0 || target_pos[0] == 7)) {
            score += 800;
        }

        score += 3 * Evaluator.posWeight(piece.getType(), color, target_pos);

        return score;
    }

    public static int[][] minimax(Piece[][] board, int depth, int alpha, int beta, boolean turn) {
        long board_hash = Zobrist.manualHash(board, turn);
        Transposition.BoardState entry = Transposition.getState(board_hash);
        if (entry != null && entry.getDepth() >= depth) {
            if (entry.isExact()) {
                return entry.getBestMove();
            }
            if (entry.isBeta() && entry.getBestMove()[0][0] >= beta) {
                return entry.getBestMove();
            }
            if (entry.isAlpha() && entry.getBestMove()[0][0] <= alpha) {
                return entry.getBestMove();
            }
        }

        if (depth == 0 || Evaluator.gameWinner(board, turn) != PieceColor.EMPTY) {
            Game.move_count++;
            return new int[][] {{Evaluator.boardEval(board, turn)}, {}, {}};
        }

        PieceColor color = turn ? PieceColor.WHITE : PieceColor.BLACK;
        ArrayList<int[][]> possible_moves = PieceHandler.getAllMoves(board, color);

        possible_moves.sort((move1, move2) -> {
            int score1 = heuristicScore(board, move1, color);
            int score2 = heuristicScore(board, move2, color);
            return Integer.compare(score2, score1);
        });

        if (depth == current_depth && current_depth > 1) {
            possible_moves.add(0, new int[][] {current_best_move[1], current_best_move[2]});
        }

        int parent_alpha = alpha;
        int parent_beta = beta;

        if (turn) {
            int[][] max_eval = {{Integer.MIN_VALUE}, {}, {}};
            for (int[][] move : possible_moves) {
                Piece[] board_info = MoveHandler.moveState(board, move[0], move[1]);
                int eval = minimax(board, depth - 1, alpha, beta, !turn)[0][0];
                MoveHandler.undoState(board, move[0], move[1], board_info);

                // Make this section more efficient later
                if (eval > max_eval[0][0]) {
                    max_eval[0][0] = eval;
                    max_eval[1] = move[0];
                    max_eval[2] = move[1];
                }

                if (System.nanoTime() > cancel_time) {
                    return max_eval;
                }

                alpha = Math.max(eval, alpha);
                if (beta <= alpha) {
                    Transposition.addState(board_hash, new Transposition.BoardState(depth, max_eval, Transposition.BETA_NODE));
                    break;
                }
    
            }

            Transposition.addState(board_hash, new Transposition.BoardState(depth, max_eval, getNodeType(max_eval[0][0], parent_alpha, parent_beta)));

            return max_eval;

        } else {
            int[][] min_eval = {{Integer.MAX_VALUE}, {}, {}};
            for (int[][] move : possible_moves) {
                Piece[] board_info = MoveHandler.moveState(board, move[0], move[1]);
                int eval = minimax(board, depth - 1, alpha, beta, !turn)[0][0];
                MoveHandler.undoState(board, move[0], move[1], board_info);

                // Make this section more efficient later
                if (eval < min_eval[0][0]) {
                    min_eval[0][0] = eval;
                    min_eval[1] = move[0];
                    min_eval[2] = move[1];
                }

                if (System.nanoTime() > cancel_time) {
                    return min_eval;
                }
                
                beta = Math.min(eval, beta);
                if (beta <= alpha) {
                    Transposition.addState(board_hash, new Transposition.BoardState(depth, min_eval, Transposition.ALPHA_NODE));
                    break;
                }
            }

            Transposition.addState(board_hash, new Transposition.BoardState(depth, min_eval, getNodeType(min_eval[0][0], parent_alpha, parent_beta)));

            return min_eval;
        }
    }
}
