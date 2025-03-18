package src.scoria;

import java.util.*;
import src.pieces.*;

public class Evaluator {

    public static final int[] piece_points = {0, 100, 320, 330, 500, 900, 0};
    public static final int NOT_OVER = -1;

    private static HashMap<Long, Integer> position_table = new HashMap<Long, Integer>();

    public static void incrementPositionTable(long hash) {
        position_table.put(hash, position_table.getOrDefault(hash, 0) + 1);
    }

    public static void decrementPositionTable(long hash) {
        position_table.put(hash, position_table.get(hash) - 1);
    }

    public static int gameWinner(byte[] board, boolean turn, long hash) {
        int color = turn ? PieceData.WHITE : PieceData.BLACK;

        // threefold repetition
        if (position_table.get(hash) != null && position_table.get(hash) >= 3) {
            return PieceData.NULL;
        }

        // any legal moves
        if (PieceHandler.hasPossibleMove(board, color)) {
            return -1;
        }

        // if king in check
        if (PieceHandler.underAttack(board, color, PieceHandler.getKingPos(color))) {
            return turn ? PieceData.BLACK : PieceData.WHITE;
        }
        
        // stalemate
        return PieceData.NULL;
    }

    public static int posWeight(int type, int color, int pos) {
        return (color == PieceData.WHITE) ? WeightMap.getMap(type, pos) : WeightMap.getMap(type, 63 - pos);
    }

    public static int boardEval(byte[] board, boolean turn, long hash) {
        int winner = gameWinner(board, turn, hash);
        switch (winner) {
            case NOT_OVER: break;
            case PieceData.WHITE: return 10000;
            case PieceData.BLACK: return -10000;
            case PieceData.NULL: return 0;
        };

        int evaluation = 0;
        
        for (int i = 0; i < 64; i++) {
            int piece = board[i];
            if (piece == PieceData.EMPTY) {
                continue;
            }

            int type = piece & PieceData.TYPE_MASK;

            if ((piece & PieceData.COLOR_MASK) == PieceData.WHITE) {
                evaluation += 2 * piece_points[type];
                evaluation += Evaluator.posWeight(type, PieceData.WHITE, i);

                if (type != PieceData.PAWN) {
                    evaluation += 2 * PieceHandler.generateMoves(board, type, PieceData.WHITE, i).size();
                }
            } else {
                evaluation -= 2 * piece_points[type];
                evaluation -= posWeight(type, PieceData.BLACK, i);

                if (type != PieceData.PAWN) {
                    evaluation -= 2 * PieceHandler.generateMoves(board, type, PieceData.BLACK, i).size();
                }
            }
        }

        return evaluation;
    }
}
