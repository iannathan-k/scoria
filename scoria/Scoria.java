package scoria;

import java.util.ArrayList;
import pieces.*;
import pieces.enums.*;
import src.*;

public class Scoria {

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
            return entry.getBestMove();
        }

        if (depth == 0) {
            Main.move_count++;
            return new int[][] {{Evaluator.boardEval(board, turn)}, {}, {}};
        }
        
        PieceColor color = turn ? PieceColor.WHITE : PieceColor.BLACK;
        ArrayList<int[][]> possible_moves = PieceHandler.getAllMoves(board, color);

        possible_moves.sort((move1, move2) -> {
            int score1 = heuristicScore(board, move1, color);
            int score2 = heuristicScore(board, move2, color);
            return Integer.compare(score2, score1);
        });

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

                alpha = (eval > alpha) ? eval : alpha;
                if (beta <= alpha) {
                    break;
                }
            }

            Transposition.BoardState new_state = new Transposition.BoardState(depth, max_eval, possible_moves);
            Transposition.addState(board_hash, new_state);

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

                beta = (eval < beta) ? eval : beta;
                if (beta <= alpha) {
                    break;
                }
            }

            Transposition.BoardState new_state = new Transposition.BoardState(depth, min_eval, possible_moves);
            Transposition.addState(board_hash, new_state);

            return min_eval;
        }
    }
}
