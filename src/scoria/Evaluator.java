package src.scoria;

import java.util.*;
import src.pieces.*;
import src.pieces.piecedata.*;

public class Evaluator {

    private static HashMap<Long, Integer> position_table = new HashMap<Long, Integer>();

    public static void incrementPositionTable(long hash) {
        if (position_table.get(hash) == null) {
            position_table.put(hash, 1);
        } else {
            position_table.put(hash, position_table.get(hash) + 1);
        }
    }

    public static void decrementPositionTable(long hash) {
        position_table.put(hash, position_table.get(hash) - 1);
    }

    public static PieceColor gameWinner(Piece[][] board, boolean turn, long hash) {
        PieceColor color = turn ? PieceColor.WHITE : PieceColor.BLACK;

        if (position_table.get(hash) == null) {
            position_table.put(hash, 1);
        }

        if (position_table.get(hash) >= 3) {
            return PieceColor.NULL;
        }

        if (PieceHandler.hasPossibleMove(board, color)) {
            return PieceColor.EMPTY;
        }

        if (PieceHandler.underAttack(board, color, PieceHandler.getKingPos(color))) {
            return turn ? PieceColor.BLACK : PieceColor.WHITE;
        }
        
        return PieceColor.NULL;
    }

    public static int posWeight(PieceType type, PieceColor color, int[] pos) {
        int[][] weight_map = WeightMap.getMap(type);
        return (color == PieceColor.WHITE) ? weight_map[pos[0]][pos[1]] : weight_map[7 - pos[0]][pos[1]];
    }

    public static int boardEval(Piece[][] board, boolean turn, long hash) {
        PieceColor winner = gameWinner(board, turn, hash);
        switch (winner) {
            case WHITE: return 10000;
            case BLACK: return -10000;
            case NULL: return 0;
            default: break;
        }

        int evaluation = 0;
        
        for (int i = 0; i < 64; i++) {
            Piece piece = board[i / 8][i % 8];
            if (piece == null) {
                continue;
            }
            if (piece.getColor() == PieceColor.WHITE) {
                evaluation += 2 * piece.getPoints();
                evaluation += posWeight(piece.getType(), PieceColor.WHITE, piece.getPosition());

                if (piece instanceof Pawn) {
                    continue;
                }
                evaluation += 2 * piece.getMoves(board).size();


            } else {
                evaluation -= 2 * piece.getPoints();
                evaluation -= posWeight(piece.getType(), PieceColor.BLACK, piece.getPosition());

                if (piece instanceof Pawn) {
                    continue;
                }
                evaluation -= 2 * piece.getMoves(board).size();
            }
        }

        return evaluation;
    }
}
