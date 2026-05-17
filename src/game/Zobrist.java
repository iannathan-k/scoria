package src.game;

import java.util.Random;

public class Zobrist {
    private static final long[][] PIECE_TABLE = new long[64][12];
    private static final long[] CASTLE_TABLE = new long[16];
    private static final long[] PASSANT_TABLE = new long[8];

    private static long turn_table;
    private static long zobrist_hash = 0L;

    public static void initZobristTable() {
        Random random = new Random(27);

        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 12; j++) {
                PIECE_TABLE[i][j] = random.nextLong();
            }
        }

        for (int i = 0; i < CASTLE_TABLE.length; i++) {
            CASTLE_TABLE[i] = random.nextLong();
        }

        for (int i = 0; i < PASSANT_TABLE.length; i++) {
            PASSANT_TABLE[i] = random.nextLong();
        }

        turn_table = random.nextLong();

        manualZobristHash();
    }

    public static void manualZobristHash() {
        zobrist_hash = 0L;
        
        for (int i = 0; i < 12; i++) {
            long piece_bitboard = BitBoard.piece_bitboards[i];
            while (piece_bitboard != 0) {
                int pos = Long.numberOfTrailingZeros(piece_bitboard);
                zobrist_hash ^= PIECE_TABLE[pos][i];
                piece_bitboard &= piece_bitboard - 1;
            }
        }

        zobrist_hash ^= CASTLE_TABLE[BitBoard.castle_rights];

        if (BitBoard.passant_rights != BitBoard.NO_PASSANT) {
            zobrist_hash ^= PASSANT_TABLE[BitBoard.passant_rights & 7];
        }

        if (BitBoard.moving_side == BitBoard.BLACK) {
            zobrist_hash ^= turn_table;
        }
    }

    public static void updateZobristHash(int move) {
        int origin = (move >> 6) & MoveHandler.POSITION_MASK;
        int target = move & MoveHandler.POSITION_MASK;
        int piece = (move >> 12) & MoveHandler.PIECE_MASK;
        int side = piece & BitBoard.COLOR_MASK;
        int capture = BitBoard.getPieceAt(target, side ^ 1);

        // Remove passant piece
        if ((move & MoveHandler.PASSANT_FLAG) != 0) {
            int ep_square = (side == BitBoard.WHITE) ? target + BitBoard.SOUTH : target + BitBoard.NORTH;
            int ep_capture = BitBoard.PAWN | side ^ 1;

            zobrist_hash ^= PIECE_TABLE[ep_square][ep_capture];
        }

        // Clear Passant Rights
        if (BitBoard.passant_rights != BitBoard.NO_PASSANT) {
            zobrist_hash ^= PASSANT_TABLE[BitBoard.passant_rights & 7];
        }

        // Move piece
        zobrist_hash ^= PIECE_TABLE[origin][piece];
        zobrist_hash ^= PIECE_TABLE[target][piece];

        // Remove capture
        if (capture != BitBoard.NO_PIECE) {
            zobrist_hash ^= PIECE_TABLE[target][capture];
        }

        // Based on only passant file because turn would be different
        if ((move & MoveHandler.DOUBLE_FLAG) != 0) {
            int passant_file = target & 7;
            zobrist_hash ^= PASSANT_TABLE[passant_file];
        }

        // Castling
        if ((move & MoveHandler.CASTLE_FLAG) != 0) {
            if (target == MoveGenerator.SQUARE_G1) {
                zobrist_hash ^= PIECE_TABLE[MoveGenerator.SQUARE_F1][BitBoard.WHITE_ROOK];
                zobrist_hash ^= PIECE_TABLE[MoveGenerator.SQUARE_H1][BitBoard.WHITE_ROOK];
            } else if (target == MoveGenerator.SQUARE_C1) {
                zobrist_hash ^= PIECE_TABLE[MoveGenerator.SQUARE_D1][BitBoard.WHITE_ROOK];
                zobrist_hash ^= PIECE_TABLE[MoveGenerator.SQUARE_A1][BitBoard.WHITE_ROOK];
            } else if (target == MoveGenerator.SQUARE_G8) {
                zobrist_hash ^= PIECE_TABLE[MoveGenerator.SQUARE_F8][BitBoard.BLACK_ROOK];
                zobrist_hash ^= PIECE_TABLE[MoveGenerator.SQUARE_H8][BitBoard.BLACK_ROOK];
            } else {
                zobrist_hash ^= PIECE_TABLE[MoveGenerator.SQUARE_D8][BitBoard.BLACK_ROOK];
                zobrist_hash ^= PIECE_TABLE[MoveGenerator.SQUARE_A8][BitBoard.BLACK_ROOK];
            }
        }

        // Promotion
        if ((move & MoveHandler.PROMOTED_MASK) != 0) {
            int promoted_piece = (move & MoveHandler.PROMOTED_MASK) >>> 16;
            zobrist_hash ^= PIECE_TABLE[target][piece];
            zobrist_hash ^= PIECE_TABLE[target][promoted_piece];
        }

        // Update Castle Rights
        int castle = BitBoard.castle_rights;
        if (origin == MoveGenerator.SQUARE_H1 || target == MoveGenerator.SQUARE_H1) {
            castle &= ~BitBoard.WHITE_KING_ROOK_MASK;
        }
        if (origin == MoveGenerator.SQUARE_A1 || target == MoveGenerator.SQUARE_A1) {
            castle &= ~BitBoard.WHITE_QUEEN_ROOK_MASK;
        }
        if (origin == MoveGenerator.SQUARE_H8 || target == MoveGenerator.SQUARE_H8) {
            castle &= ~BitBoard.BLACK_KING_ROOK_MASK;
        }
        if (origin == MoveGenerator.SQUARE_A8 || target == MoveGenerator.SQUARE_A8) {
            castle &= ~BitBoard.BLACK_QUEEN_ROOK_MASK;
        }
        if (origin == MoveGenerator.SQUARE_E1) {
            castle &= ~BitBoard.WHITE_KING_CASTLE_MASK;
        }
        if (origin == MoveGenerator.SQUARE_E8) {
            castle &= ~BitBoard.BLACK_KING_CASTLE_MASK;
        }

        zobrist_hash ^= CASTLE_TABLE[BitBoard.castle_rights];
        zobrist_hash ^= CASTLE_TABLE[castle];
        
        zobrist_hash ^= turn_table;
    }

    public static void updateZobristHashNull() {
        zobrist_hash ^= turn_table;

        if (BitBoard.passant_rights != BitBoard.NO_PASSANT) {
            zobrist_hash ^= PASSANT_TABLE[BitBoard.passant_rights & 7];
        }
    }

    public static long getZobristHash() {
        return zobrist_hash;
    }

    public static void setZobristHash(long hash) {
        zobrist_hash = hash;
    }
}