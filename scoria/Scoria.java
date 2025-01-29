package scoria;

import java.util.ArrayList;
import pieces.*;
import pieces.enums.*;
import src.*;

public class Scoria {

    private static int heuristicScore(Piece[][] board, int[][] move, PieceColor color) {
        int[] origin_pos = move[0];
        int[] target_pos = move[1];

        if (!PieceHandler.inRange(target_pos)) {
            System.out.println(origin_pos[0] + ", " + origin_pos[1]);
            System.out.println(target_pos[0] + ", " + target_pos[1]);
            Interface.printBoard(board);
        }

        Piece piece = board[origin_pos[0]][origin_pos[1]];
        Piece capture = board[target_pos[0]][target_pos[1]];

        int score = 0;

        if (capture.getType() != PieceType.EMPTY) {
            score += capture.getPoints() - piece.getPoints();
        }

        if (piece.getType() == PieceType.PAWN && (target_pos[0] == 0 || target_pos[0] == 7)) {
            score += 800;
        }

        return score;
    }

    public static int[][] minimax(Piece[][] board, int depth, int alpha, int beta, boolean turn) {

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
            int[][] max_eval = {{-Integer.MAX_VALUE}, {}, {}};
            for (int[][] move : possible_moves) {

                if (depth == 1) {
                    Piece piece = board[move[0][0]][move[0][1]];
                    Piece captured = board[move[1][0]][move[1][1]];

                    if (piece.getType() == PieceType.PAWN) {
                        if (captured.getType() == PieceType.EMPTY && move[0][1] != move[1][1]) {
                            // en passant
                            Main.ep_count++;
                        }
                        if (move[1][0] == 0 || move[1][0] == 7) {
                            // promotion logic
                            Main.promo_count++;
                        }
                    }
                    if (piece.getType() == PieceType.KING && Math.abs(move[1][1] - move[0][1]) > 1) {
                        // castle logic
                        Main.castle_count++;
                    }

                    if (captured.getType() != PieceType.EMPTY) {
                        Main.capture_count++;
                    }
                }
                

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
                    // break;
                }
            }

            return max_eval;

        } else {
            int[][] min_eval = {{Integer.MAX_VALUE}, {}, {}};
            for (int[][] move : possible_moves) {

                if (depth == 1) {
                    Piece piece = board[move[0][0]][move[0][1]];
                    Piece captured = board[move[1][0]][move[1][1]];

                    if (piece.getType() == PieceType.PAWN) {
                        if (captured.getType() == PieceType.EMPTY && move[0][1] != move[1][1]) {
                            // en passant
                            Main.ep_count++;
                        }
                        if (move[1][0] == 0 || move[1][0] == 7) {
                            // promotion logic
                            Main.promo_count++;
                        }
                    }
                    if (piece.getType() == PieceType.KING && Math.abs(move[1][1] - move[0][1]) > 1) {
                        // castle logic
                        Main.castle_count++;
                    }

                    if (captured.getType() != PieceType.EMPTY) {
                        Main.capture_count++;
                    }
                }

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
                    // break;
                }
            }

            return min_eval;
        }
    }
}
