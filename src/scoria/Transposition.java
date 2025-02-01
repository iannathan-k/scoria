package src.scoria;

import java.util.*;

public class Transposition {

    public static final int EXACT_NODE = 0;
    public static final int ALPHA_NODE = 1;
    public static final int BETA_NODE = 2;

    static class BoardState {
        private int depth;
        private int[][] best_move;
        private int node_type;

        public BoardState(int depth, int[][] best_move, int node_type) {
            this.depth = depth;
            this.best_move = best_move;
            this.node_type = node_type;
        }

        public int getDepth() {
            return depth;
        }

        public int[][] getBestMove() {
            return best_move;
        }

        public boolean isExact() {
            return this.node_type == EXACT_NODE;
        }

        public boolean isAlpha() {
            return this.node_type == ALPHA_NODE;
        }

        public boolean isBeta() {
            return this.node_type == BETA_NODE;
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
