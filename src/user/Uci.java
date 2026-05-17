package src.user;

import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.game.Zobrist;
import src.utils.MoveList;

public class Uci {
    
    private static final char[] PIECE_CHARS = {
        'P', 'p',
        'N', 'n',
        'B', 'b',
        'R', 'r',
        'Q', 'q',
        'K', 'k'
    };

    private static String squareToAlgebraic(int pos) {
        char row = (char) ('1' + (pos >> 3));
        char col = (char) ('a' + (pos & 7));
        return "" + col + row;
    }

    public static int algebraicToSquare(String alg) {
        int col = alg.charAt(0) - 'a';
        int row = alg.charAt(1) - '1';
        return row << 3 | col;
    }

    public static String moveToUci(int move) {
        int origin = (move >> 6) & MoveHandler.POSITION_MASK;
        int target = move & MoveHandler.POSITION_MASK;

        String uci = squareToAlgebraic(origin) + squareToAlgebraic(target);

        int promotion_piece = (move & MoveHandler.PROMOTED_MASK) >>> 16;
        switch (promotion_piece & BitBoard.PIECE_MASK) {
            case BitBoard.KNIGHT    -> uci += "n";
            case BitBoard.BISHOP    -> uci += "b";
            case BitBoard.ROOK      -> uci += "r";
            case BitBoard.QUEEN     -> uci += "q";
        }

        return uci;
    }

    public static int uciToMove(String uci) {
        int origin = algebraicToSquare(uci.substring(0, 2));
        int target = algebraicToSquare(uci.substring(2, 4));
        int piece = BitBoard.getPieceAt(origin);
        int capture = BitBoard.getPieceAt(target);
        int move = MoveGenerator.encodeMove(origin, target, piece);

        // Double pawn push
        if ((piece & BitBoard.PIECE_MASK) == BitBoard.PAWN
            && Math.abs(target - origin) >= 16) {

            move |= MoveHandler.DOUBLE_FLAG;
        }

        // En passant
        if ((piece & BitBoard.PIECE_MASK) == BitBoard.PAWN
            && capture == BitBoard.NO_PIECE
            && (origin & 7) != (target & 7)) {

            move |= MoveHandler.PASSANT_FLAG;
        }

        // Promotion
        if (uci.length() == 5) {
            int color = piece & BitBoard.COLOR_MASK;
            char promoted_char = uci.charAt(4);

            switch (promoted_char) {
                case 'q' -> move |= (BitBoard.QUEEN + color << 16);
                case 'r' -> move |= (BitBoard.ROOK + color << 16);
                case 'b' -> move |= (BitBoard.BISHOP + color << 16);
                case 'n' -> move |= (BitBoard.KNIGHT + color << 16);
            }
        }

        // Castling
        if ((piece & BitBoard.PIECE_MASK) == BitBoard.KING
            && Math.abs((origin & 7) - (target & 7)) >= 2) {

            move |= MoveHandler.CASTLE_FLAG;
        }

        return move;
    }

    public static String moveListToUciString(MoveList move_list) {
        String uci = "";

        for (int i = 0; i < move_list.size(); i++) {
            uci += moveToUci(move_list.get(i)) + " ";
        }

        return uci;
    }

    public static String getCurrentFen() {
        String fen = "";

        for (int i = 7; i >= 0; i--) {
            int empty_count = 0;

            for (int j = 0; j < 8; j++) {
                int square = i << 3 | j;
                int piece_index = BitBoard.getPieceAt(square);

                if (piece_index == BitBoard.NO_PIECE) {
                    empty_count++;
                    continue;
                }

                if (empty_count > 0) {
                    fen += empty_count;
                    empty_count = 0;
                }

                fen += PIECE_CHARS[piece_index];
            }

            if (empty_count > 0) {
                fen += empty_count;
            }

            if (i > 0) {
                fen += "/";
            }
        }

        // Side to move
        fen += (BitBoard.moving_side == BitBoard.WHITE) ? " w " : " b ";

        // Castling rights
        if ((BitBoard.castle_rights & BitBoard.WHITE_KING_ROOK_MASK) != 0) {
            fen += "K";
        }
        if ((BitBoard.castle_rights & BitBoard.WHITE_QUEEN_ROOK_MASK) != 0) {
            fen += "Q";
        }
        if ((BitBoard.castle_rights & BitBoard.BLACK_KING_ROOK_MASK) != 0) {
            fen += "k";
        }
        if ((BitBoard.castle_rights & BitBoard.BLACK_QUEEN_ROOK_MASK) != 0) {
            fen += "q";
        }
        if (BitBoard.castle_rights == 0) {
            fen += "-";
        }

        // Passant Rights

        if (BitBoard.passant_rights != BitBoard.NO_PASSANT) {
            fen += " " + squareToAlgebraic(BitBoard.passant_rights) + " ";
        } else {
            fen += " - ";
        }

        return fen;
    }

    public static void printBitBoard(long bitboard) {
        System.out.println("    a b c d e f g h");
        System.out.println();

        for (int i = 7; i >= 0; i--) {
            System.out.print(i + 1 + "   ");

            for (int j = 0; j < 8; j++) {
                int square = (i << 3) | j;
                if ((bitboard & (1L << square)) != 0) {
                    System.out.print("1 ");
                } else {
                    System.out.print(". ");
                }
            }

            System.out.println();
        }
    }

    public static void printBoard() {
        System.out.println("    a b c d e f g h");
        System.out.println();

        for (int i = 7; i >= 0; i--) {
            System.out.print(i + 1 + "   ");

            for (int j = 0; j < 8; j++) {
                int piece_index = BitBoard.getPieceAt(i << 3 | j);
                char piece_char = (piece_index != BitBoard.NO_PIECE) ? PIECE_CHARS[piece_index] : '.';
                
                System.out.print(piece_char + " ");
            }

            System.out.println();
        }

        System.out.println();
        System.out.println("hash: " + Long.toHexString(Zobrist.getZobristHash()));
        System.out.println("fen: " + getCurrentFen());
    }

    public static void printBoardIndexes() {
        System.out.println("    a  b  c  d  e  f  g  h");
        System.out.println();

        for (int i = 7; i >= 0; i--) {
            System.out.print(i + 1 + "   ");

            for (int j = 0; j < 8; j++) {
                int square = (i << 3) | j;
                if (square < 10) {
                    System.out.print(" ");
                }

                System.out.print(square + " ");
            }

            System.out.println();
        }
    }
}
