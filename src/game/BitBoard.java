package src.game;

import java.util.Arrays;

import src.utils.MoveList;

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
    
    public static int moving_side = 0;
    public static int castle_rights = 0b1111;
    public static int passant_rights;

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

    public static void initBoardByFen(String fen) {
        int index = 56;

        Arrays.fill(piece_bitboards, 0L);
        for (int i = 0; i < fen.length(); i++) {
            char piece_char = fen.charAt(i);
            if (piece_char == '/') {
                index -= 16;
                continue;
            }

            if (Character.isDigit(piece_char)) {
                index += Character.getNumericValue(piece_char);
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

            piece_bitboards[piece_val] |= 1L << index;
            occupancy_bitboard |= 1L << index;
            color_bitboards[(piece_val < 6) ? 0 : 1] |= 1L << index;
            index++;
        }
    }

    public static void printBoard() {
        for (int row = 7; row >= 0; row--) {
            for (int col = 0; col < 8; col++) {
                int piece_val = getPieceAt(row << 3 | col);
                char piece_char = '-';

                switch (piece_val) {
                    case WHITE_PAWN     -> piece_char = 'P';
                    case WHITE_KNIGHT   -> piece_char = 'N';
                    case WHITE_BISHOP   -> piece_char = 'B';
                    case WHITE_ROOK     -> piece_char = 'R';
                    case WHITE_QUEEN    -> piece_char = 'Q';
                    case WHITE_KING     -> piece_char = 'K';
                    case BLACK_PAWN     -> piece_char = 'p';
                    case BLACK_KNIGHT   -> piece_char = 'n';
                    case BLACK_BISHOP   -> piece_char = 'b';
                    case BLACK_ROOK     -> piece_char = 'r';
                    case BLACK_QUEEN    -> piece_char = 'q';
                    case BLACK_KING     -> piece_char = 'k';
                }

                System.out.print(piece_char + " ");
            }
            
            System.out.println();
        }
    }

    public static void printOccupancy(long bitboard) {
        for (int rank = 7; rank >= 0; rank--) { // rank 8 down to 1
            for (int file = 0; file < 8; file++) { // file a to h
                int square = rank * 8 + file;      // square index in bitboard
                long mask = 1L << square;
                System.out.print((bitboard & mask) != 0 ? "1 " : ". ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void printMap() {

    }

    public static void printMoveMap(MoveList move_list) {
        long map = 0L;
        int origin = (move_list.get(0) >> 6) & MoveHandler.POSITION_MASK;
        for (int i = 0; i < move_list.size(); i++) {
            int move = move_list.get(i);
            int target = move & MoveHandler.POSITION_MASK;
            map |= 1L << target;
        }

        for (int row = 7; row >= 0; row--) {
            for (int col = 0; col < 8; col++) {
                int square = row << 3 | col;
                long mask = 1L << square;
                if (square != origin) {
                    System.out.print((map & mask) != 0 ? "1 " : ". ");
                } else {
                    System.out.print("A ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}