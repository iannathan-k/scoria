package src.scoria;

import java.util.*;

public class Transposition {

    public static final int EXACT_NODE = 0;
    public static final int ALPHA_NODE = 1;
    public static final int BETA_NODE = 2;

    public static class BoardState {
        private int depth;
        private int best_score;
        private int node_type;
        private int[] best_line;

        public BoardState(int depth, int best_score, int node_type, int[] best_line) {
            this.depth = depth;
            this.best_score = best_score;
            this.node_type = node_type;
            this.best_line = best_line;
        }

        public int getDepth() {
            return depth;
        }

        public int getBestScore() {
            return best_score;
        }

        public int[] getBestLine() {
            return best_line;
        }

        public boolean isExact() {
            return node_type == EXACT_NODE;
        }

        public boolean isAlpha() {
            return node_type == ALPHA_NODE;
        }

        public boolean isBeta() {
            return node_type == BETA_NODE;
        }
    }
    
    private static HashMap<Long, BoardState> transposition_table = new HashMap<Long, BoardState>();

    public static void addTransposition(long hash, BoardState state) {
        transposition_table.put(hash, state);
    }

    public static BoardState getTransposition(long hash) {
        return transposition_table.get(hash);
    }

    public static void clearHashTable() {
        transposition_table.clear();
    }

}
