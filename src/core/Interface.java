package src.core;

import src.pieces.PieceData;

public abstract class Interface {
    private static final String[] CHARACTER_LIST = {" ", "P", "N", "B", "R", "Q", "K"};

    public static void printBoard(byte[] board) {
        DebugLogger.logOut("    a   b   c   d   e   f   g   h");
        DebugLogger.logOut("  +---+---+---+---+---+---+---+---+");

        for (int i = 0; i < 8; i++) {
            String line = (8 - i) + " | ";

            for (int j = 0; j < 8; j++) {
                byte piece = board[i << 3 | j];

                String piece_char = CHARACTER_LIST[piece & PieceData.TYPE_MASK];
                piece_char = (piece < PieceData.BLACK) ? piece_char : piece_char.toLowerCase();

                line += piece_char + " | ";
            }

            DebugLogger.logOut(line);
            DebugLogger.logOut("  +---+---+---+---+---+---+---+---+");
        }
    }

    public static String getVariationString(int[] moves) {
        String line = "";
        for (int move : moves) {
            if (move == 0) continue;
            line += moveToUci(move);
            line += " ";
        }
        return line;
    }

    public static String getPieceCharacter(int piece) {
        String piece_char = CHARACTER_LIST[piece & PieceData.TYPE_MASK];
        piece_char = (piece < PieceData.BLACK) ? piece_char : piece_char.toLowerCase();
        return piece_char;
    }

    public static String posToSquare(int pos) {
        return (char) (97 + (pos & 7)) + Integer.toString(8 - (pos >> 3));
    }

    public static int squareToPos(String square) {
        return (8 - (square.charAt(1) - '0')) * 8 + square.charAt(0) - 'a';
    }

    public static String moveToUci(int move) {
        String move_string = posToSquare((move >> 8) & MoveHandler.POS_MASK) + posToSquare(move & MoveHandler.POS_MASK);
        
        if ((move & MoveHandler.PROMO_MASK) != 0) move_string += "q";
        return move_string;
    }

    public static int uciToMove(String uci) {
        int origin_pos = squareToPos(uci.substring(0, 2));
        int target_pos = squareToPos(uci.substring(2, 4));

        int move = origin_pos << 8 | target_pos;
        byte piece = Game.board[origin_pos];
        byte captured = Game.board[target_pos];

        // Castle Flag
        if ((piece & PieceData.TYPE_MASK) == PieceData.KING) {
            if (Math.abs((origin_pos & 7) - (target_pos & 7)) == 2) {
                move |= MoveHandler.CASTLE_MASK;
            }
        }

        // Passant Flag
        if ((piece & PieceData.TYPE_MASK) == PieceData.PAWN) {
            if ((origin_pos & 7) != (target_pos & 7) && captured == PieceData.EMPTY) {
                move |= MoveHandler.PASSANT_MASK;
            }

            // Promotion Flag
            if ((target_pos >> 3) == 7 || (target_pos >> 3) == 0) {
                move |= MoveHandler.PROMO_MASK;
            }
        }

        return move;
    }
}