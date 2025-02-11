package src.core;

import src.pieces.*;
import src.pieces.enums.*;

public abstract class Setup {

    public static Piece makePieceObj(char piece, int index) {

        PieceColor color = PieceColor.WHITE;
        int dir = -1;
        int king_index = 0;

        if (Character.isLowerCase(piece)) {
            color = PieceColor.BLACK;
            dir = 1;
            piece = Character.toUpperCase(piece);
            king_index = 1;
        }

        switch (piece) {
            case 'P':
                return new Pawn(new int[] {index / 8, index % 8}, color, dir);
            case 'N':
                return new Knight(new int[] {index / 8, index % 8}, color);
            case 'B':
                return new Bishop(new int[] {index / 8, index % 8}, color);
            case 'R':
                return new Rook(new int[] {index / 8, index % 8}, color);
            case 'Q':
                return new Queen(new int[] {index / 8, index % 8}, color);
            case 'K':
                PieceHandler.setKingPiece(new King(new int[] {index / 8, index % 8}, color), king_index); 
                return PieceHandler.getKingPiece(king_index);
        }

        throw new UnsupportedOperationException("!! UNSUPPORTED PIECETYPE !!");
    }

    public static void castleRightsSetup(char right) {
        if (Character.isUpperCase(right)) {
            ((King) Game.board[7][4]).pushMovedFalse();
        } else {
            ((King) Game.board[0][4]).pushMovedFalse();
        }

        switch (right) {
            case 'K' -> ((Rook) Game.board[7][7]).pushMovedFalse();
            case 'Q' -> ((Rook) Game.board[7][0]).pushMovedFalse();
            case 'k' -> ((Rook) Game.board[0][7]).pushMovedFalse();
            case 'q' -> ((Rook) Game.board[0][0]).pushMovedFalse();
        }
    }
    
    public static void setUp(Piece[][] board, String fen) {
        int index = 0;
        String[] fen_stream = fen.split("\\s");

        // Board piece locations
        for (int i = 0; i < fen_stream[0].length(); i++) {
            char piece = fen.charAt(i);

            if (piece == '/' || piece == ' ') {
                continue;
            }
            if (Character.isDigit(piece)) {
                index += Character.getNumericValue(piece);
                continue;
            }

            board[index / 8][index % 8] = makePieceObj(piece, index);
            index += 1;
        }

        // Turn
        boolean turn = (fen_stream[1] == "w")? true : false;
        Game.setTurn(turn);

        // Castling Rights
        if (fen_stream.length < 3) {
            return;
        }
        
        for (int i = 0; i < fen_stream[2].length(); i++) {
            castleRightsSetup(fen_stream[2].charAt(i));
        }
    }
}
