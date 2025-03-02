package src.core;

import src.pieces.PieceData;
import src.scoria.*;

public abstract class Interface {
    private static String getChar(int type) {
        return switch (type) {
            case PieceData.PAWN -> "P";
            case PieceData.KNIGHT -> "N";
            case PieceData.BISHOP -> "B";
            case PieceData.ROOK -> "R";
            case PieceData.QUEEN -> "Q";
            case PieceData.KING -> "K";
            default -> throw new IllegalArgumentException("Invalid Char " + type);
        };
    }

    public static void printBoard(byte[] board) {
        System.out.println("    a   b   c   d   e   f   g   h");
        System.out.println("  +---+---+---+---+---+---+---+---+");

        for (int i = 0; i < 8; i++) {
            String line = (8 - i) + " | ";

            for (int j = 0; j < 8; j++) {
                byte piece = board[i << 3 | j];

                if (piece == PieceData.EMPTY) {
                    line += "  | ";
                    continue;
                }

                String piece_char = getChar(piece & PieceData.TYPE_MASK);
                if ((piece & PieceData.COLOR_MASK) == PieceData.BLACK) {
                    piece_char = piece_char.toLowerCase();
                }

                line += piece_char + " | ";
            }

            System.out.println(line);
            System.out.println("  +---+---+---+---+---+---+---+---+");
        }
    }

    public static String posToSquare(int pos) {
        return (char) (97 + (pos & 7)) + Integer.toString(8 - (pos >> 3));
    }

    public static int squareToPos(String square) {
        return (8 - (square.charAt(1) - '0')) * 8 + square.charAt(0) - 'a';
    }

    public static String moveToUci(int move) {
        return posToSquare((move >> 8) & MoveHandler.POS_MASK) + posToSquare(move & MoveHandler.POS_MASK);
    }

    public static int uciToMove(String uci) {
        // this one does not set flags yet
        return (squareToPos(uci.substring(0, 2)) << 8) | squareToPos(uci.substring(2, 4));
    }

    public static void printEndGame() {
        long hash = Zobrist.manualHash(Game.board, Game.getTurn());
        int winner = Evaluator.gameWinner(Game.board, Game.getTurn(), hash);
        switch (winner) {
            case PieceData.WHITE -> System.out.println("white won");
            case PieceData.BLACK -> System.out.println("black won");
            case PieceData.NULL -> System.out.println("stalemate");
            default -> throw new IllegalArgumentException("Unexpected value: " + winner);
        }
    }

    public static void printCLI() {
        long hash = Zobrist.manualHash(Game.board, Game.getTurn());
        if (Game.getTurn()) {
            System.out.println("~~~ black to move ~~~");
        } else {
            System.out.println("~~~ white to move ~~~");
        }

        Interface.printBoard(Game.board);

        System.out.println("eval: " + Evaluator.boardEval(Game.board, Game.getTurn(), hash));
        System.out.println("depth: " + Game.getLastThinkDepth());
        System.out.println("nodes: " + Game.getMoveCount());
        System.out.println("time: " + Game.getLastThinkTime() + "ms");
    }
}