package src.game;

import java.util.Arrays;

import src.user.Uci;

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
    public static final int WHITE_KING_CASTLE_MASK  = 0b1100;
    public static final int BLACK_KING_CASTLE_MASK  = 0b0011;

    public static long[] piece_bitboards = new long[12];
    public static long[] color_bitboards = new long[2];
    public static long occupancy_bitboard;
    
    public static int moving_side = WHITE;
    public static int castle_rights = 0b1111;
    public static int passant_rights = -1;

    public static boolean isEmpty(int square) {
        long mask = 1L << square;
        return (occupancy_bitboard & mask) == 0;
    }

    public static int getPieceAt(int square) {
        long mask = 1L << square;
        for (int i = 0; i < 12; i++) {
            if ((piece_bitboards[i] & mask) != 0) return i;
        }
        return -1;
    }

    public static int getPieceAt(int square, int color) {
        long mask = 1L << square;
        for (int i = color * 6; i < color * 6 + 6; i++) {
            if ((piece_bitboards[i] & mask) != 0) return i;
        }
        return -1;
    }

    public static int getPieceColor(int piece) {
        return (piece < 6) ? WHITE : BLACK;
    }

    // FIXME: Handle half and full moves
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

            int piece_val = -1;
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
            color_bitboards[getPieceColor(piece_val)] |= 1L << square;

            col++;
        }

        passant_rights = -1;
        castle_rights = 0;
        moving_side = BLACK;

        if (fen_array.length > 1 && fen_array[1].equals("w")) {
            moving_side = WHITE;
        }

        if (fen_array.length > 2) {
            if (fen_array[2].contains("K")) castle_rights |= WHITE_KING_ROOK_MASK;
            if (fen_array[2].contains("Q")) castle_rights |= WHITE_QUEEN_ROOK_MASK;
            if (fen_array[2].contains("k")) castle_rights |= BLACK_KING_ROOK_MASK;
            if (fen_array[2].contains("q")) castle_rights |= BLACK_QUEEN_ROOK_MASK;
        }

        if (fen_array.length > 3 && !fen_array[3].equals("-")) {
            passant_rights = Uci.algebraicToSquare(fen_array[3]);
        }
    }
}