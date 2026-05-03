package src.game;

import java.util.Random;

public class Zobrist {
    private static long[][] piece_table = new long[64][12];
    private static long[] castle_table = new long[4];
    private static long[] passant_table = new long[8];
    private static long turn_table;

    private static long zobrist_hash = 0L;

    public static void initZobristTable() {
        // FIXME: Find optimal seed
        Random random = new Random(21);

        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 12; j++) {
                piece_table[i][j] = random.nextLong();
            }
        }

        for (int i = 0; i < 4; i++) {
            castle_table[i] = random.nextLong();
        }

        for (int i = 0; i < 8; i++) {
            passant_table[i] = random.nextLong();
        }

        turn_table = random.nextLong();

        manualZobristHash();
    }

    // FIXME: Integrate into init Zobrist Hash
    public static void manualZobristHash() {
        zobrist_hash = 0L;
        
        for (int i = 0; i < 12; i++) {
            long piece_bitboard = BitBoard.piece_bitboards[i];
            while (piece_bitboard != 0) {
                int pos = Long.numberOfTrailingZeros(piece_bitboard);
                zobrist_hash ^= piece_table[pos][i];
                piece_bitboard &= piece_bitboard - 1;
            }
        }

        if ((BitBoard.castle_rights & BitBoard.WHITE_KING_ROOK_MASK) != 0) {
            zobrist_hash ^= castle_table[0];
        }
        if ((BitBoard.castle_rights & BitBoard.WHITE_QUEEN_ROOK_MASK) != 0) {
            zobrist_hash ^= castle_table[1];
        }
        if ((BitBoard.castle_rights & BitBoard.BLACK_KING_ROOK_MASK) != 0) {
            zobrist_hash ^= castle_table[2];
        }
        if ((BitBoard.castle_rights & BitBoard.BLACK_QUEEN_ROOK_MASK) != 0) {
            zobrist_hash ^= castle_table[3];
        }   
        
        if (BitBoard.passant_rights != BitBoard.NO_PASSANT) {
            zobrist_hash ^= passant_table[BitBoard.passant_rights & 7];
        }

        if (BitBoard.moving_side == BitBoard.BLACK) {
            zobrist_hash ^= turn_table;
        }
    }

    // FIXME: Could be incrementally updated inside the move function
    public static void updateZobristHash(int move) {
        int origin = (move >> 6) & MoveHandler.POSITION_MASK;
        int target = move & MoveHandler.POSITION_MASK;
        int piece = (move >> 12) & MoveHandler.PIECE_MASK;
        int moving_side = piece & BitBoard.COLOR_MASK;
        int capture = BitBoard.getPieceAt(target, moving_side ^ 1);

        // Remove passant piece
        if ((move & MoveHandler.PASSANT_FLAG) != 0) {
            int passant_capture_square = (moving_side == BitBoard.WHITE) ? target - 8 : target + 8;
            int passant_capture = BitBoard.getPieceAt(passant_capture_square, moving_side ^ 1); // FIXME: CAN CHANGE THIS
            zobrist_hash ^= piece_table[passant_capture_square][passant_capture];
        }

        // Clear Passant Rights
        if (BitBoard.passant_rights != BitBoard.NO_PASSANT) {
            zobrist_hash ^= passant_table[BitBoard.passant_rights & 7];
        }

        // Move piece
        zobrist_hash ^= piece_table[origin][piece];
        zobrist_hash ^= piece_table[target][piece];

        // Remove capture
        if (capture != BitBoard.NO_PIECE) {
            zobrist_hash ^= piece_table[target][capture];
        }

        // Based on only passant file because turn would be different
        if ((move & MoveHandler.DOUBLE_FLAG) != 0) {
            int passant_file = target & 7;
            zobrist_hash ^= passant_table[passant_file];
        }

        // Castling
        if ((move & MoveHandler.CASTLE_FLAG) != 0) {
            if (target == MoveGenerator.SQUARE_G1) {
                zobrist_hash ^= piece_table[MoveGenerator.SQUARE_F1][BitBoard.WHITE_ROOK];
                zobrist_hash ^= piece_table[MoveGenerator.SQUARE_H1][BitBoard.WHITE_ROOK];
            } else if (target == MoveGenerator.SQUARE_C1) {
                zobrist_hash ^= piece_table[MoveGenerator.SQUARE_D1][BitBoard.WHITE_ROOK];
                zobrist_hash ^= piece_table[MoveGenerator.SQUARE_A1][BitBoard.WHITE_ROOK];
            } else if (target == MoveGenerator.SQUARE_G8) {
                zobrist_hash ^= piece_table[MoveGenerator.SQUARE_F8][BitBoard.BLACK_ROOK];
                zobrist_hash ^= piece_table[MoveGenerator.SQUARE_H8][BitBoard.BLACK_ROOK];
            } else {
                zobrist_hash ^= piece_table[MoveGenerator.SQUARE_D8][BitBoard.BLACK_ROOK];
                zobrist_hash ^= piece_table[MoveGenerator.SQUARE_A8][BitBoard.BLACK_ROOK];
            }
        }

        // Promotion
        if ((move & MoveHandler.PROMOTED_MASK) != 0) {
            int promoted_piece = (move & MoveHandler.PROMOTED_MASK) >>> 16;
            zobrist_hash ^= piece_table[target][piece];
            zobrist_hash ^= piece_table[target][promoted_piece];
        }

        // Update Castle Rights
        if (origin == MoveGenerator.SQUARE_H1 || target == MoveGenerator.SQUARE_H1) {
            zobrist_hash ^= castle_table[0];
        } else if (origin == MoveGenerator.SQUARE_A1 || target == MoveGenerator.SQUARE_A1) {
            zobrist_hash ^= castle_table[1];
        } else if (origin == MoveGenerator.SQUARE_H8 || target == MoveGenerator.SQUARE_H8) {
            zobrist_hash ^= castle_table[2];
        } else if (origin == MoveGenerator.SQUARE_A8 || target == MoveGenerator.SQUARE_A8) {
            zobrist_hash ^= castle_table[3];
        } else if (origin == MoveGenerator.SQUARE_E1) {
            zobrist_hash ^= castle_table[0];
            zobrist_hash ^= castle_table[1];
        } else if (origin == MoveGenerator.SQUARE_E8) {
            zobrist_hash ^= castle_table[2];
            zobrist_hash ^= castle_table[3];
        }
        
        zobrist_hash ^= turn_table;
    }

    public static void updateZobristHashNull() {
        zobrist_hash ^= turn_table;

        if (BitBoard.passant_rights != BitBoard.NO_PASSANT) {
            zobrist_hash ^= passant_table[BitBoard.passant_rights & 7];
        }
    }

    public static long getZobristHash() {
        return zobrist_hash;
    }

    public static void setZobristHash(long hash) {
        zobrist_hash = hash;
    }
}