package src.scoria;

import java.util.*;
import src.pieces.*;

public class Evaluator {

    private static HashMap<Long, Integer> position_table = new HashMap<Long, Integer>();

    public static void incrementPositionTable(long hash) {
        position_table.put(hash, position_table.getOrDefault(hash, 0) + 1);
    }

    public static void decrementPositionTable(long hash) {
        position_table.put(hash, position_table.get(hash) - 1);
    }

    // this definitely has to be updated
    public static int gameWinner(byte[] board, boolean turn, long hash) {
        int color = turn ? PieceData.WHITE : PieceData.BLACK;

        // threefold repetition
        if (position_table.get(hash) != null && position_table.get(hash) >= 3) {
            return PieceData.NULL;
        }

        // If can still move
        if (PieceHandler.hasPossibleMove(board, color)) {
            return -1;
        }

        // If king in check
        if (PieceHandler.underAttack(board, color, PieceHandler.getKingPos(color))) {
            return turn ? PieceData.BLACK : PieceData.WHITE;
        }
        
        // Stalemate
        return PieceData.NULL;
    }

    public static int posWeight(int type, int color, int pos) {
        int[] weight_map = WeightMap.getMap(type);
        return (color == PieceData.WHITE) ? weight_map[pos] : weight_map[63 - pos]; // check the black condition here
    }

    public static int piecePoints(int type) {
        return switch (type) {
            case PieceData.PAWN -> 100;
            case PieceData.KNIGHT -> 330;
            case PieceData.BISHOP -> 330;
            case PieceData.ROOK -> 500;
            case PieceData.QUEEN -> 900;
            case PieceData.KING -> 0;
            default -> throw new IllegalArgumentException("Invalid Piece " + type);
        };
    }

    public static int boardEval(byte[] board, boolean turn, long hash) {
        int winner = gameWinner(board, turn, hash);
        switch (winner) {
            case PieceData.WHITE: return 10000;
            case PieceData.BLACK: return -10000;
            case PieceData.NULL: return 0;
            default: break;
        };

        int evaluation = 0;
        
        for (int i = 0; i < 64; i++) {
            int piece = board[i];
            if (piece == PieceData.EMPTY) {
                continue;
            }

            if ((piece & PieceData.COLOR_MASK) == PieceData.WHITE) {
                evaluation += 2 * piecePoints(piece & PieceData.TYPE_MASK);
                evaluation += posWeight(piece & PieceData.TYPE_MASK, PieceData.WHITE, i);

                if ((piece & PieceData.TYPE_MASK) != PieceData.PAWN) {
                    evaluation += 2 * PieceHandler.generateMoves(board, piece & PieceData.TYPE_MASK, i).size();
                }
            } else {
                evaluation -= 2 * piecePoints(piece & PieceData.TYPE_MASK);
                evaluation -= posWeight(piece & PieceData.TYPE_MASK, PieceData.BLACK, i);

                if ((piece & PieceData.TYPE_MASK) != PieceData.PAWN) {
                    evaluation -= 2 * PieceHandler.generateMoves(board, piece & PieceData.TYPE_MASK, i).size();
                }
            }
        }

        return evaluation;
    }
}
