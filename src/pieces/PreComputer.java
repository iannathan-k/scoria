package src.pieces;

import java.util.ArrayList;

public class PreComputer {
    public static final int[][] KNIGHT_PREMOVES = new int[64][];
    public static final int[][][] BISHOP_PREMOVES = new int[64][][];
    public static final int[][][] ROOK_PREMOVES = new int[64][][];
    public static final int[][][] QUEEN_PREMOVES = new int[64][][];
    public static final int[][] KING_PREMOVES = new int[64][];

    public static final int[][] WHITE_PAWN_PREATTACKS = new int[64][];
    public static final int[][] BLACK_PAWN_PREATTACKS = new int[64][];

    public static final int[][] KNIGHT_DIRECTIONS = {
        {2, 1},
        {2, -1},
        {-2, 1},
        {-2, -1},
        {1, -2},
        {-1, -2},
        {1, 2},
        {-1, 2}
    };

    public static final int[][] BISHOP_DIRECTIONS = {
        {1, 1},
        {1, -1},
        {-1, 1},
        {-1, -1}
    };

    public static final int[][] ROOK_DIRECTIONS = {
        {1, 0},
        {-1, 0},
        {0, 1},
        {0, -1}
    };

    public static final int[][] ALL_DIRECTIONS = {
        {1, 1},
        {1, -1},
        {-1, 1},
        {-1, -1},
        {1, 0},
        {-1, 0},
        {0, 1},
        {0, -1}
    };

    public static final int[][] WHITE_PAWN_ATTACKS = {
        {1, -1},
        {1, 1}
    };

    public static final int[][] BLACK_PAWN_ATTACKS = {
        {-1, -1},
        {-1, 1}
    };

    public static void initializePremoves() {
        // Knight Premoves
        for (int i = 0; i < 64; i++) {
            ArrayList<Integer> possible_moves = new ArrayList<Integer>();
    
            for (int[] dir : KNIGHT_DIRECTIONS) {
                int row = (i >> 3) + dir[0];
                int col = (i & 7) + dir[1];
                if (PieceHandler.inRange(row, col)) {
                    possible_moves.add(row << 3 | col);
                }
            }

            KNIGHT_PREMOVES[i] = possible_moves.stream().mapToInt(j -> j).toArray();
        }

        // Bishop Premoves
        for (int i = 0; i < 64; i++) {
            ArrayList<int[]> global_moves = new ArrayList<int[]>();
            for (int k = 0; k < 4; k++) {
                ArrayList<Integer> possible_moves = new ArrayList<Integer>();
        
                int[] dir = BISHOP_DIRECTIONS[k];
                int row = (i >> 3) + dir[0];
                int col = (i & 7) + dir[1];
        
                while (PieceHandler.inRange(row, col)) {
                    possible_moves.add(row << 3 | col);
                    row += dir[0];
                    col += dir[1];
                }
                
                if (possible_moves.isEmpty()) continue;
                global_moves.add(possible_moves.stream().mapToInt(j -> j).toArray());
            }

            BISHOP_PREMOVES[i] = global_moves.toArray(new int[0][]);
        }

        // Rook Premoves
        for (int i = 0; i < 64; i++) {
            ArrayList<int[]> global_moves = new ArrayList<int[]>();
            for (int k = 0; k < 4; k++) {
                ArrayList<Integer> possible_moves = new ArrayList<Integer>();
        
                int[] dir = ROOK_DIRECTIONS[k];
                int row = (i >> 3) + dir[0];
                int col = (i & 7) + dir[1];
        
                while (PieceHandler.inRange(row, col)) {
                    possible_moves.add(row << 3 | col);
                    row += dir[0];
                    col += dir[1];
                }
                
                if (possible_moves.isEmpty()) continue;
                global_moves.add(possible_moves.stream().mapToInt(j -> j).toArray());
            }

            ROOK_PREMOVES[i] = global_moves.toArray(new int[0][]);
        }

        // Queen Premoves
        for (int i = 0; i < 64; i++) {
            ArrayList<int[]> global_moves = new ArrayList<int[]>();
            for (int k = 0; k < 8; k++) {
                ArrayList<Integer> possible_moves = new ArrayList<Integer>();
        
                int[] dir = ALL_DIRECTIONS[k];
                int row = (i >> 3) + dir[0];
                int col = (i & 7) + dir[1];
        
                while (PieceHandler.inRange(row, col)) {
                    possible_moves.add(row << 3 | col);
                    row += dir[0];
                    col += dir[1];
                }
                
                if (possible_moves.isEmpty()) continue;
                global_moves.add(possible_moves.stream().mapToInt(j -> j).toArray());
            }

            QUEEN_PREMOVES[i] = global_moves.toArray(new int[0][]);
        }

        // King Premoves
        for (int i = 0; i < 64; i++) {
            ArrayList<Integer> possible_moves = new ArrayList<Integer>();

            for (int[] dir : ALL_DIRECTIONS) {
                int row = (i >> 3) + dir[0];
                int col = (i & 7) + dir[1];
                if (PieceHandler.inRange(row, col)) {
                    possible_moves.add(row << 3 | col);
                }
            }
            
            KING_PREMOVES[i] = possible_moves.stream().mapToInt(j -> j).toArray();
        }

        // Black Pawn Attacks
        for (int i = 0; i < 64; i++) {
            ArrayList<Integer> possible_attacks = new ArrayList<Integer>();


            int[][] dirs = BLACK_PAWN_ATTACKS;
            int row = i >> 3;
            int col = i & 7;
    
            for (int[] dir : dirs) {
                int new_row = row + dir[0];
                int new_col = col + dir[1];
                if (PieceHandler.inRange(new_row, new_col)) {
                    possible_attacks.add(new_row << 3 | new_col);
                }
            }

            BLACK_PAWN_PREATTACKS[i] = possible_attacks.stream().mapToInt(j -> j).toArray();
        }

        // White Pawn Attacks
        for (int i = 0; i < 64; i++) {
            ArrayList<Integer> possible_attacks = new ArrayList<Integer>();

            int[][] dirs = WHITE_PAWN_ATTACKS;
            int row = i >> 3;
            int col = i & 7;
    
            for (int[] dir : dirs) {
                int new_row = row + dir[0];
                int new_col = col + dir[1];
                if (PieceHandler.inRange(new_row, new_col)) {
                    possible_attacks.add(new_row << 3 | new_col);
                }
            }

            WHITE_PAWN_PREATTACKS[i] = possible_attacks.stream().mapToInt(j -> j).toArray();
        }
    }

}
