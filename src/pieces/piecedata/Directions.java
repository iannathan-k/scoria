package src.pieces.piecedata;

public class Directions {
    public final static int[][] KNIGHT_DIRECTIONS = {
        {2, 1},
        {2, -1},
        {-2, 1},
        {-2, -1},
        {1, -2},
        {-1, -2},
        {1, 2},
        {-1, 2}
    };

    public final static int[][] BISHOP_DIRECTIONS = {
        {1, 1},
        {1, -1},
        {-1, 1},
        {-1, -1}
    };

    public final static int[][] ROOK_DIRECTIONS = {
        {1, 0},
        {-1, 0},
        {0, 1},
        {0, -1}
    };

    public final static int[][] ALL_DIRECTIONS = {
        {1, 1},
        {1, -1},
        {-1, 1},
        {-1, -1},
        {1, 0},
        {-1, 0},
        {0, 1},
        {0, -1}
    };

    public final static PieceType[] ROOK_QUEEN_TYPES = {
        PieceType.ROOK, PieceType.QUEEN
    };

    public final static PieceType[] BISHOP_QUEEN_TYPES = {
        PieceType.BISHOP, PieceType.QUEEN
    };
}
