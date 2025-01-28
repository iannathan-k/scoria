package scoria;
import pieces.*;
import pieces.enums.*;

public class Evaluator {

    public static PieceColor gameWinner(Piece[][] board, boolean turn) {
        PieceColor color = turn ? PieceColor.WHITE : PieceColor.BLACK;

        if (!PieceHandler.isKingStuck(board, color)) {
            return PieceColor.EMPTY;
        }
        if (PieceHandler.underAttack(board, color, PieceHandler.getKingPos(color))) {
            return turn ? PieceColor.BLACK : PieceColor.WHITE;
        }
        if (PieceHandler.getAllMoves(board, color).isEmpty()) {
            return PieceColor.NULL;
        }

        return PieceColor.EMPTY;
    }

    // Will Enhance Later
    public static int boardEval(Piece[][] board, boolean turn) {
        PieceColor winner = gameWinner(board, turn);
        switch (winner) {
            case WHITE: return 10000;
            case BLACK: return -10000;
            case NULL: return 0;
            default: break;
        }

        int white_advantage = 0;
        int black_advantage = 0;

        for (int i = 0; i < 64; i++) {
            Piece piece = board[i / 8][i % 8];
            if (piece.getType() == PieceType.EMPTY) {
                continue;
            }

            if (piece.getColor() == PieceColor.WHITE) {
                white_advantage += piece.getPoints();
            } else {
                black_advantage += piece.getPoints();
            }
        }

        return white_advantage - black_advantage;
    }
}
