package scoria;

import java.util.*;
import pieces.*;
import pieces.enums.PieceColor;
import pieces.enums.PieceType;

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
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece instanceof Empty) {
                    continue;
                }

                PieceColor color = piece.getColor();

                if (piece instanceof Rook && !((Rook) piece).peekMove()) {
                    int offset = (j == 7) ? 0 : 1;
                    int castle_side = (color == PieceColor.WHITE) ? 0 : 2;
                    hash ^= castle_table[castle_side + offset];
                } else if (piece instanceof King && !((King) piece).peekMove()) {
                    int castle_side = (color == PieceColor.WHITE) ? 0 : 2;
                    hash ^= castle_table[castle_side];
                    hash ^= castle_table[castle_side + 1];
                } else if (piece instanceof Pawn) {
                    int current_move_num = PieceHandler.currentMoveNumber();
                    if (((Pawn) piece).peekLeft() != -1 && ((Pawn) piece).peekLeft() == current_move_num) {
                        hash ^= passant_table[j - 1];
                    }
                    if (((Pawn) piece).peekRight() != -1 && ((Pawn) piece).peekRight() == current_move_num) {
                        hash ^= passant_table[j + 1];
                    }
                }
    
                hash ^= zobrist_table[i][j][piece.getType().ordinal() + color.ordinal() * 6];
            }
        }

        if (turn) {
            hash ^= turn_table;
        }

        return hash;
    }

}
