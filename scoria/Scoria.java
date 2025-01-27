package scoria;

import java.util.ArrayList;
import pieces.*;
import src.*;

public class Scoria {

    public static int[] minimax(Piece[][] board, int depth, boolean turn) {
        if (depth == 0) {
            return new int[] {0, -1, -1, -1, -1};
        }

        ArrayList<int[][]> possible_moves = new ArrayList<int[][]>();

        if (turn) {
            int[] max_eval = {-Integer.MAX_VALUE, -1, -1, -1, -1};
            for (int[][] move : possible_moves) {
                Piece[] board_info = MoveHandler.moveState(board, move[0], move[1]);
                int eval = minimax(board, depth - 1, !turn)[0];
                MoveHandler.undoState(board, move[0], move[1], board_info);

                // Make this section more efficient later
                if (eval > max_eval[0]) {
                    max_eval[0] = eval;
                    max_eval[1] = move[0][0];
                    max_eval[2] = move[0][1];
                    max_eval[3] = move[1][0];
                    max_eval[4] = move[1][1];
                }
            }

            return max_eval;

        } else {
            int[] min_eval = {Integer.MAX_VALUE, -1, -1, -1, -1};
            for (int[][] move : possible_moves) {
                Piece[] board_info = MoveHandler.moveState(board, move[0], move[1]);
                int eval = minimax(board, depth - 1, !turn)[0];
                MoveHandler.undoState(board, move[0], move[1], board_info);

                // Make this section more efficient later
                if (eval < min_eval[0]) {
                    min_eval[0] = eval;
                    min_eval[1] = move[0][0];
                    min_eval[2] = move[0][1];
                    min_eval[3] = move[1][0];
                    min_eval[4] = move[1][1];
                }
            }

            return min_eval;
        }
    }
}
