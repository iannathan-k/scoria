package src.game;

import java.util.Arrays;

import src.user.Uci;

public class BitBoard {
    public static final int WHITE           = 0;
    public static final int BLACK           = 1;

    public static final int NO_PIECE  = -1;
    public static final int PAWN      = 0b0000; //  0
    public static final int KNIGHT    = 0b0010; //  2
    public static final int BISHOP    = 0b0100; //  4
    public static final int ROOK      = 0b0110; //  6
    public static final int QUEEN     = 0b1000; //  8
    public static final int KING      = 0b1010; // 10

    public static final int WHITE_PAWN      = 0b0000; //  0
    public static final int WHITE_KNIGHT    = 0b0010; //  2
    public static final int WHITE_BISHOP    = 0b0100; //  4
    public static final int WHITE_ROOK      = 0b0110; //  6
    public static final int WHITE_QUEEN     = 0b1000; //  8
    public static final int WHITE_KING      = 0b1010; // 10
    public static final int BLACK_PAWN      = 0b0001; //  1
    public static final int BLACK_KNIGHT    = 0b0011; //  3
    public static final int BLACK_BISHOP    = 0b0101; //  5
    public static final int BLACK_ROOK      = 0b0111; //  7
    public static final int BLACK_QUEEN     = 0b1001; //  9
    public static final int BLACK_KING      = 0b1011; // 11

    public static final int PIECE_MASK = 0b1110;
    public static final int COLOR_MASK = 0b0001;

    public static final long ROW_1 = 0x00000000000000FFL;
    public static final long ROW_2 = 0x000000000000FF00L;
    public static final long ROW_3 = 0x0000000000FF0000L;
    public static final long ROW_4 = 0x00000000FF000000L;
    public static final long ROW_5 = 0x000000FF00000000L;
    public static final long ROW_6 = 0x0000FF0000000000L;
    public static final long ROW_7 = 0x00FF000000000000L;
    public static final long ROW_8 = 0xFF00000000000000L;
    public static final long COL_A = 0x0101010101010101L;
    public static final long COL_B = 0x0202020202020202L;
    public static final long COL_C = 0x0404040404040404L;
    public static final long COL_D = 0x0808080808080808L;
    public static final long COL_E = 0x1010101010101010L;
    public static final long COL_F = 0x2020202020202020L;
    public static final long COL_G = 0x4040404040404040L;
    public static final long COL_H = 0x8080808080808080L;

    public static final int WHITE_KING_ROOK_MASK    = 0b1000;
    public static final int WHITE_QUEEN_ROOK_MASK   = 0b0100;
    public static final int BLACK_KING_ROOK_MASK    = 0b0010;
    public static final int BLACK_QUEEN_ROOK_MASK   = 0b0001;
    public static final int WHITE_CASTLE_MASK       = 0b1100;
    public static final int BLACK_CASTLE_MASK       = 0b0011;

    public static final int NO_PASSANT = -1;

    public static final int NORTH       = 8;
    public static final int SOUTH       = -8;
    public static final int EAST        = 1;
    public static final int WEST        = -1;
    public static final int NORTH_EAST  = 9;
    public static final int NORTH_WEST  = 7;
    public static final int SOUTH_EAST  = -7;
    public static final int SOUTH_WEST  = -9;
    public static final int NORTH_2     = 16;
    public static final int SOUTH_2     = -16;
    public static final int N_SHIFT     = 8;
    public static final int S_SHIFT     = 8;
    public static final int NE_SHIFT    = 9;
    public static final int NW_SHIFT    = 7;
    public static final int SE_SHIFT    = 7;
    public static final int SW_SHIFT    = 9;

    public static final long LIGHT_SQUARES = 0x55AA55AA55AA55AAL;
    public static final long DARK_SQUARES  = 0xAA55AA55AA55AA55L;

    /* Current Bitboard
     * P N B R Q K p n b r q k
     * 
     * New Bitboard
     * P p N n B b R r Q q K k
     * Color check --> piece & 1 (odd or even)
     * Piece check --> start from 0 or 1, increment by 2
     * Offset --> add color
     */

    public static long[] piece_bitboards = new long[12];
    public static long[] color_bitboards = new long[2];
    public static long occupancy_bitboard;
    
    public static int moving_side = WHITE;
    public static int castle_rights = WHITE_CASTLE_MASK | BLACK_CASTLE_MASK;
    public static int passant_rights = NO_PASSANT;
    public static int halfmoves = 0;
    public static int fullmoves = 1;

    public static boolean isEmpty(int square) {
        long mask = 1L << square;
        return (occupancy_bitboard & mask) == 0;
    }

    public static int getPieceAt(int square) {
        long mask = 1L << square;
        for (int i = 0; i < 12; i++) {
            if ((piece_bitboards[i] & mask) != 0) return i;
        }
        return NO_PIECE;
    }

    public static int getPieceAt(int square, int color) {
        long mask = 1L << square;
        for (int i = color; i < 12; i += 2) {
            if ((piece_bitboards[i] & mask) != 0) return i; 
        }
        return NO_PIECE;
    }

    public static boolean hasNonPawnPiece(int color) {
        return (color_bitboards[color] 
                & ~piece_bitboards[PAWN | color] 
                & ~piece_bitboards[KING | color]) 
                != 0;
    }

    public static void initBoardByFen(String fen) {
        String[] fen_array = fen.split("\\s");

        Arrays.fill(piece_bitboards, 0L);
        Arrays.fill(color_bitboards, 0L);
        occupancy_bitboard = 0L;

        int row = 7;
        int col = 0;
        for (int i = 0; i < fen_array[0].length(); i++) {
            char piece_char = fen_array[0].charAt(i);

            if (piece_char == '/') {
                row--;
                col = 0;
                continue;
            }

            if (Character.isDigit(piece_char)) {
                col += Character.getNumericValue(piece_char);
                continue;
            }

            int piece_val = NO_PIECE;
            switch (piece_char) {
                case 'P' -> piece_val = WHITE_PAWN;
                case 'N' -> piece_val = WHITE_KNIGHT;
                case 'B' -> piece_val = WHITE_BISHOP;
                case 'R' -> piece_val = WHITE_ROOK;
                case 'Q' -> piece_val = WHITE_QUEEN;
                case 'K' -> piece_val = WHITE_KING;
                case 'p' -> piece_val = BLACK_PAWN;
                case 'n' -> piece_val = BLACK_KNIGHT;
                case 'b' -> piece_val = BLACK_BISHOP;
                case 'r' -> piece_val = BLACK_ROOK;
                case 'q' -> piece_val = BLACK_QUEEN;
                case 'k' -> piece_val = BLACK_KING;
            }

            int square = row << 3 | col;
            piece_bitboards[piece_val] |= 1L << square;
            occupancy_bitboard |= 1L << square;
            color_bitboards[piece_val & COLOR_MASK] |= 1L << square;

            col++;
        }

        passant_rights = BitBoard.NO_PASSANT;
        castle_rights = 0;
        moving_side = BLACK;
        halfmoves = 0;
        fullmoves = 1;

        // Moving Side
        if (fen_array.length > 1 && fen_array[1].equals("w")) {
            moving_side = WHITE;
        }

        // Castle Rights
        if (fen_array.length > 2) {
            if (fen_array[2].contains("K")) castle_rights |= WHITE_KING_ROOK_MASK;
            if (fen_array[2].contains("Q")) castle_rights |= WHITE_QUEEN_ROOK_MASK;
            if (fen_array[2].contains("k")) castle_rights |= BLACK_KING_ROOK_MASK;
            if (fen_array[2].contains("q")) castle_rights |= BLACK_QUEEN_ROOK_MASK;
        }

        // Passant Rights
        if (fen_array.length > 3 && !fen_array[3].equals("-")) {
            passant_rights = Uci.algebraicToSquare(fen_array[3]);
        }

        // Halfmoves
        if (fen_array.length > 4) {
            halfmoves = Integer.parseInt(fen_array[4]);
        }

        if (fen_array.length > 5) {
            fullmoves = Integer.parseInt(fen_array[5]);
        }
    }
}