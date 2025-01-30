package scoria;

import java.util.*;

public class Transposition {

    static class BoardState {
        private int depth;
        private int[][] best_move;
        private ArrayList<int[][]> possible_moves;

        public BoardState(int depth, int[][] best_move, ArrayList<int[][]> possible_moves) {
            this.depth = depth;
            this.best_move = best_move;
            this.possible_moves = possible_moves;
        }

        public int getDepth() {
            return depth;
        }

        public int[][] getBestMove() {
            return best_move;
        }

        public ArrayList<int[][]> getPossibleMoves() {
            return possible_moves;
        }
    }
    
    private static HashMap<Long, BoardState> transposition_table = new HashMap<Long, BoardState>();

    public static void addState(long hash, BoardState state) {
        transposition_table.put(hash, state);
    }

    public static BoardState getState(long hash) {
        return transposition_table.get(hash);
    }

}
