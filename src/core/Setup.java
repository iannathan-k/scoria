package src.core;

import src.pieces.*;

public abstract class Setup {

    private static final byte CASTLE_INVERT = 0b1111;

    public static int getPiece(char piece_char) {
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
            case 'K' -> PieceHandler.setCastleRights(PieceData.WHITE_KING_ROOK ^ CASTLE_INVERT);
            case 'Q' -> PieceHandler.setCastleRights(PieceData.WHITE_QUEEN_ROOK ^ CASTLE_INVERT);
            case 'k' -> PieceHandler.setCastleRights(PieceData.BLACK_KING_ROOK ^ CASTLE_INVERT);
            case 'q' -> PieceHandler.setCastleRights(PieceData.BLACK_QUEEN_ROOK ^ CASTLE_INVERT);
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
        PieceHandler.clearCastleRights();

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
        Game.turn = fen_stream[1].equals("w") ? 1 : -1;

        // Castling Rights
        if (fen_stream.length >= 3 && !fen_stream[2].equals("-")) {
            for (int i = 0; i < fen_stream[2].length(); i++) {
                castleRightsSetup(fen_stream[2].charAt(i));
            }
        }

        // En Passant Rights
        if (fen_stream.length >= 4 && !fen_stream[3].equals("-")) {
            int col = fen_stream[3].charAt(0) - 'a';
            int color = fen_stream[3].charAt(1) == '6' ? PieceData.BLACK : PieceData.WHITE;
            PieceHandler.setPassantRights(col, color);
        }

        if (fen_stream.length == 6) {
            Game.move_number = Integer.parseInt(fen_stream[5]) << 1;
        }
    }
}
