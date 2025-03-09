package src.core;

import src.pieces.*;

public abstract class Setup {

    public static int getPiece(char piece_char) {
        // requires a way to deal with king positions
        int piece = 0;

        if (Character.isLowerCase(piece_char)) {
            piece |= PieceData.BLACK;
            piece_char = Character.toUpperCase(piece_char);
        }

        return switch (piece_char) {
            case 'P' -> piece | PieceData.PAWN;
            case 'N' -> piece | PieceData.KNIGHT;
            case 'B' -> piece | PieceData.BISHOP;
            case 'R' -> piece | PieceData.ROOK;
            case 'Q' -> piece | PieceData.QUEEN;
            case 'K' -> piece | PieceData.KING;
            default -> throw new IllegalArgumentException("Illegal Piece " + piece_char);
        };
    }

    public static void castleRightsSetup(char right) {
        switch (right) {
            case 'K' -> PieceHandler.setCastleRights(0);
            case 'Q' -> PieceHandler.setCastleRights(1);
            case 'k' -> PieceHandler.setCastleRights(2);
            case 'q' -> PieceHandler.setCastleRights(3);
        }
    }
    
    public static void setUp(byte[] board, String fen) {
        String[] fen_stream = fen.split("\\s");

        // Board piece locations
        int index = 0;

        // Reset Logic
        for (int i = 0; i < 64; i++) {
            board[i] = PieceData.EMPTY;
        }

        PieceHandler.clearPassantRights();

        // Piece Placement
        for (int i = 0; i < fen_stream[0].length(); i++) {
            char piece = fen_stream[0].charAt(i);

            if (piece == '/') {
                continue;
            }
            if (Character.isDigit(piece)) {
                index += Character.getNumericValue(piece);
                continue;
            }
            if (piece == 'K') PieceHandler.setKingPosition(index, PieceData.WHITE);
            if (piece == 'k') PieceHandler.setKingPosition(index, PieceData.BLACK);

            board[index] = (byte) getPiece(piece);
            index += 1;
        }

        // Turn
        Game.setTurn(fen_stream[1].equals("w"));

        // // Castling Rights
        // if (fen_stream.length < 3) {
        //     return;
        // }

        // En Passant Rights
        if (fen_stream.length >= 4 && fen_stream[3].length() > 1) {
            int col = fen_stream[3].charAt(0) - 'a';
            int color = fen_stream[3].charAt(1) == '6' ? PieceData.BLACK : PieceData.WHITE;
            PieceHandler.setPassantRights(col, color);
        }
    }
}
