package src.scoria;

import java.util.*;

import src.core.Game;
import src.pieces.*;

public class Evaluator {

    public static final int[] piece_points = {0, 100, 320, 330, 500, 900, 0};
    public static final int NOT_OVER = -1;

    private static HashMap<Long, Integer> position_table = new HashMap<Long, Integer>();

    public static void incrementPositionTable(long hash) {
        position_table.put(hash, position_table.getOrDefault(hash, 1) + 1);
    }

    public static void decrementPositionTable(long hash) {
        position_table.put(hash, position_table.get(hash) - 1);
    }

    public static void clearPositionTable() {
        position_table.clear();
    }

    public static int posWeight(int type, int color, int pos) {
        return (color == PieceData.WHITE) ? WeightMap.POSITION_WEIGHTS[type][pos] : WeightMap.POSITION_WEIGHTS[type][63 - pos];
    }

    public static boolean isGameOver(byte[] board, int turn, long hash) {
        int color = (turn == 1) ? PieceData.WHITE : PieceData.BLACK;

        if (position_table.get(hash) != null && position_table.get(hash) >= 3) return true;
        if (PieceHandler.hasPossibleMove(board, color)) return false;
        if (PieceHandler.kingUnderAttack(board, color)) return true;
        return true;
    }

    public static int gameWinner(byte[] board, int turn, long hash) {
        int color = (turn == 1) ? PieceData.WHITE : PieceData.BLACK;

        // threefold repetition
        if (position_table.get(hash) != null && position_table.get(hash) >= 3) {
            return PieceData.NULL;
        }

        // any legal moves
        if (PieceHandler.hasPossibleMove(board, color)) {
            return NOT_OVER;
        }

        // if king in check
        if (PieceHandler.kingUnderAttack(board, color)) {
            return color ^ PieceData.COLOR_MASK;
        }
        
        // stalemate
        return PieceData.NULL;
    }

    public static int boardEval(byte[] board, int turn, long hash, int depth) {
        switch (gameWinner(board, turn, hash)) {
            case NOT_OVER: break;
            case PieceData.WHITE: return 10000 * (depth + 1);
            case PieceData.BLACK: return -10000 * (depth + 1);
            case PieceData.NULL: return 0;
        }

        int evaluation = 0;
        
        for (int i = 0; i < 64; i++) {
            int piece = board[i];
            if (piece == PieceData.EMPTY) {
                continue;
            }

            int type = piece & PieceData.TYPE_MASK;
            int color = piece & PieceData.COLOR_MASK;
            int sign = (color == PieceData.WHITE) ? 1 : -1; 

            evaluation += sign * 2 * piece_points[type];
            evaluation += sign * Evaluator.posWeight(type, color, i);
            evaluation += sign * 2 * PieceHandler.getMobility(board, type, color, i);
        }

        return evaluation;
    }

    public static int pieceEval(int piece, int pos) {
        int type = piece & PieceData.TYPE_MASK;
        int color = piece & PieceData.COLOR_MASK;
        int sign = (color == PieceData.WHITE) ? 1 : -1;

        int evaluation = sign * 2 * piece_points[type];
        evaluation += sign * Evaluator.posWeight(type, color, pos);
        evaluation += sign * 2 * PieceHandler.getMobility(Game.board, type, color, pos);

        return evaluation;
    }
}
