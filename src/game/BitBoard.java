package src.game;

public class BitBoard {
    public static final int WHITE           = 0;
    public static final int BLACK           = 1;

    public static final int NO_PIECE        = -1;
    public static final int WHITE_PAWN      = 0;
    public static final int WHITE_KNIGHT    = 1;
    public static final int WHITE_BISHOP    = 2;
    public static final int WHITE_ROOK      = 3;
    public static final int WHITE_QUEEN     = 4;
    public static final int WHITE_KING      = 5;
    public static final int BLACK_PAWN      = 6;
    public static final int BLACK_KNIGHT    = 7;
    public static final int BLACK_BISHOP    = 8;
    public static final int BLACK_ROOK      = 9;
    public static final int BLACK_QUEEN     = 10;
    public static final int BLACK_KING      = 11;

    public static long[] piece_bitboards = new long[12];
    public static long[] color_bitboards = new long[2];
    public static long occupancy_bitboard;
    
    public static int moving_side = 0;
    public static int castle_rights;
    public static int passant_rights;

    // Could optimize by only checking opponent bitboards
    // For any capture moves in a seperate function
    public static int getPieceAt(int square) {
        long mask = 1L << square;
        for (int i = 0; i < 12; i++) {
            if ((piece_bitboards[i] & mask) != 0) {
                return i;
            }
        }
        return -1;
    }

    public static int getPieceColor(int piece) {
        return (piece < 6) ? WHITE : BLACK;
    }
}
