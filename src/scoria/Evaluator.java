package src.scoria;
import src.pieces.*;
import src.pieces.enums.*;

public class Evaluator {

    public static PieceColor gameWinner(Piece[][] board, boolean turn) {
        PieceColor color = turn ? PieceColor.WHITE : PieceColor.BLACK;

        if (!PieceHandler.isKingStuck(board, color)) {
            return PieceColor.EMPTY;
        }
        if (PieceHandler.getAllMoves(board, color).isEmpty()) {
            if (PieceHandler.underAttack(board, color, PieceHandler.getKingPos(color))) {
                return turn ? PieceColor.BLACK : PieceColor.WHITE;
            }
            
            return PieceColor.NULL;
        }

        return PieceColor.EMPTY;
    }

    public static int posWeight(PieceType type, PieceColor color, int[] pos) {
        int[][] weight_map = WeightMap.getMap(type);
        return (color == PieceColor.WHITE) ? weight_map[pos[0]][pos[1]] : weight_map[7 - pos[0]][pos[1]];
    }

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
            if (piece instanceof Empty) {
                continue;
            }
            if (piece.getColor() == PieceColor.WHITE) {
                white_advantage += 2 * piece.getPoints();
                white_advantage += posWeight(piece.getType(), PieceColor.WHITE, piece.getPosition());
                white_advantage += 2 * piece.getMoves(board).size();
            } else {
                black_advantage += 2 * piece.getPoints();
                black_advantage += posWeight(piece.getType(), PieceColor.BLACK, piece.getPosition());
                black_advantage += 2 * piece.getMoves(board).size();
            }
        }

        return white_advantage - black_advantage;
    }
}
