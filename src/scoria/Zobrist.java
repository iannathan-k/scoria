package src.scoria;

import java.util.*;
import src.pieces.*;

public class Zobrist {
    private static long[][] zobrist_table = new long[64][12];
    private static long[] castle_table = new long[4]; // KQkq
    private static long[] passant_table = new long[17];
    private static long turn_table = 0;

    public static void initTable() {
        Random random = new Random();

        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 12; j++) {
                zobrist_table[i][j] = random.nextLong();
            }
        }

        for (int i = 0; i < 4; i++) {
            castle_table[i] = random.nextLong();
        }

        for (int i = 0; i < 17; i++) {
            passant_table[i] = random.nextLong();
        }

        turn_table = random.nextLong();
    }

    public static long manualHash(byte[] board, boolean turn) {
        long hash = 0;
        for (int i = 0; i < 64; i++) {
            // int j = i & 7;
            byte piece = board[i];
            if (piece == PieceData.EMPTY) {
                continue;
            }

            int color = piece & PieceData.COLOR_MASK;
            int type = piece & PieceData.TYPE_MASK;
            
            // // Castle Rights
            // if (piece instanceof Rook && !((Rook) piece).peekMove()) {
            //     int offset = (j == 7) ? 0 : 1;
            //     int castle_side = (color == PieceColor.WHITE) ? 0 : 2;
            //     hash ^= castle_table[castle_side + offset];

            // } else if (piece instanceof King && !((King) piece).peekMove()) {
            //     int castle_side = (color == PieceColor.WHITE) ? 0 : 2;
            //     hash ^= castle_table[castle_side];
            //     hash ^= castle_table[castle_side + 1];

            // Normal Pieces
            int offset = type - 1;
            offset += (color == PieceData.WHITE) ? 0 : 6;
            hash ^= zobrist_table[i][offset];
        }

        // Turn
        hash ^= turn ? turn_table : 0;

        // en passant
        hash ^= passant_table[PieceHandler.peekPassantRights()];

        return hash;
    }

}
