package src.pieces;

public class PieceData {
    // Types

    public static final byte EMPTY  = 0b00000000;
    public static final byte PAWN   = 0b00000001;
    public static final byte KNIGHT = 0b00000010;
    public static final byte BISHOP = 0b00000011;
    public static final byte ROOK   = 0b00000100;
    public static final byte QUEEN  = 0b00000101;
    public static final byte KING   = 0b00000110;

    // Colors

    public final static byte BLACK  = 0b00001000;
    public final static byte WHITE  = 0b00000000;
    public final static byte NULL   = 0b00010000;

    // Masks

    public static final byte TYPE_MASK   = 0b00000111;
    public static final byte COLOR_MASK  = 0b00001000;

    // Castling Masks

    public static final byte KING_RIGHTS_MASK = 0b0011;
    public static final byte BLACK_QUEEN_ROOK = 0b1110;
    public static final byte BLACK_KING_ROOK  = 0b1101;
    public static final byte WHITE_QUEEN_ROOK = 0b1011;
    public static final byte WHITE_KING_ROOK  = 0b0111;
}
