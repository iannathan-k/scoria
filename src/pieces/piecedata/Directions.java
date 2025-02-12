package src.pieces.piecedata;

public class Directions {
    public final static int[][] bishop_directions = {
        {1, 1},
        {1, -1},
        {-1, 1},
        {-1, -1}
    };

    public final static int[][] rook_directions = {
        {1, 0},
        {-1, 0},
        {0, 1},
        {0, -1}
    };

    public final static int[][] knight_directions = {
        {2, 1},
        {2, -1},
        {-2, 1},
        {-2, -1},
        {1, -2},
        {-1, -2},
        {1, 2},
        {-1, 2}
    };

    public final static int[][] all_directions = {
        {1, 1},
        {1, -1},
        {-1, 1},
        {-1, -1},
        {1, 0},
        {-1, 0},
        {0, 1},
        {0, -1}
    };
}
