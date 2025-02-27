package src.scoria;

import java.util.*;

import src.core.*;
import src.pieces.*;

public class Zobrist {
    private static long[][][] zobrist_table = new long[8][8][12];
    private static long[] castle_table = new long[4]; // KQkq
    private static long[] passant_table = new long[8];
    private static long turn_table = 0;

    public static void initTable() {
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 12; k++) {
                    zobrist_table[i][j][k] = random.nextLong();
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            castle_table[i] = random.nextLong();
        }

        for (int i = 0; i < 8; i++) {
            passant_table[i] = random.nextLong();
        }

        turn_table = random.nextLong();
    }

    public static long manualHash(Piece[][] board, boolean turn) {
        long hash = 0;
        for (int i = 0; i < 64; i++) {
            int j = i & 7;
            Piece piece = board[i >> 3][j];
            if (piece == null) {
                continue;
            }

            PieceColor color = piece.getColor();
            
            // Castle Rights
            if (piece instanceof Rook && !((Rook) piece).peekMove()) {
                int offset = (j == 7) ? 0 : 1;
                int castle_side = (color == PieceColor.WHITE) ? 0 : 2;
                hash ^= castle_table[castle_side + offset];

            } else if (piece instanceof King && !((King) piece).peekMove()) {
                int castle_side = (color == PieceColor.WHITE) ? 0 : 2;
                hash ^= castle_table[castle_side];
                hash ^= castle_table[castle_side + 1];

            // En Passant
            } else if (piece instanceof Pawn) {
                int current_move_num = Game.currentMoveNumber();
                int left = ((Pawn) piece).peekLeft();
                int right = ((Pawn) piece).peekRight();
                if (left != -1 && left == current_move_num) {
                    hash ^= passant_table[j - 1];
                }
                if (right != -1 && right== current_move_num) {
                    hash ^= passant_table[j + 1];
                }
            }

            // Normal Pieces
            hash ^= zobrist_table[i >> 3][j][piece.getType().ordinal() + color.ordinal() * 6];
        }

            // Turn
            hash ^= turn ? turn_table : 0;

        return hash;
    }

}
