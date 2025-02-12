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
            // System.out.println("Hi");
            return PieceColor.NULL;
        }

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

    public static int boardEval(Piece[][] board, boolean turn, long hash) {
        PieceColor winner = gameWinner(board, turn, hash);
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
