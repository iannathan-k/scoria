package pieces;

import pieces.enums.*;

public class PieceHandler {
    private static King[] king_pieces = new King[2];

    private static int[] getKingPos(PieceColor color) {
        if (color == PieceColor.WHITE) {
            return king_pieces[0].getPosition();
        } else {
            return king_pieces[1].getPosition();
        }
    }

    public static boolean inRange(int[] pos) {
        return 0 <= pos[0] && pos[0] <= 7 && 0 <= pos[1] && pos[1] <= 7;
    }

    private static boolean slidingPiece(Piece[][] board, int[] king_pos, int[][] dirs, PieceType[] attack_pieces, PieceColor color) {
        for (int[] dir : dirs) {
            int[] attack = {king_pos[0] + dir[0], king_pos[1] + dir[1]};
            while (PieceHandler.inRange(attack)) {
                Piece piece = board[attack[0]][attack[1]];
                if (piece.getType() == PieceType.EMPTY) {
                    attack[0] += dir[0];
                    attack[1] += dir[1];
                    continue;
                }
                if (piece.getColor() == color) {
                    break;
                }
                if (piece.getType() == attack_pieces[0] || piece.getType() == attack_pieces[1]) {
                    return true;
                }
                break;
            }
        }

        return false;
    }

    private static boolean pawnPiece(Piece[][] board, int[] king_pos, int[][] pawn_attacks, PieceColor color) {
        for (int[] attack : pawn_attacks) {
            if (!PieceHandler.inRange(attack)) {
                continue;
            }
            Piece piece = board[attack[0]][attack[1]];
            if (piece.getType() != PieceType.PAWN) {
                continue;
            }
            if (piece.getColor() == color) {
                continue;
            }

            return true;
        }

        return false;
    }

    private static boolean kingPiece(Piece[][] board, int[] king_pos, int[][] dirs) {
        for (int[] dir : dirs) {
            int[] attack = {king_pos[0] + dir[0], king_pos[1] + dir[1]};
            if (!PieceHandler.inRange(attack)) {
                continue;
            }
            if (board[attack[0]][attack[1]].getType() == PieceType.KING) {
                return true;
            }
        }

        return false;
    }

    public static boolean kingInCheck(Piece[][] board, PieceColor color) {

        int[] king_pos = getKingPos(color);

        int[][] rook_attack = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1}
        };

        int[][] bishop_attack = {
            {1, 1},
            {1, -1},
            {-1, 1},
            {-1, -1}
        };

        int[][] knight_attacks = {
            {king_pos[0] + 2, king_pos[1] + 1},
            {king_pos[0] + 2, king_pos[1] - 1},
            {king_pos[0] - 2, king_pos[1] + 1},
            {king_pos[0] - 2, king_pos[1] - 1},
            {king_pos[0] + 1, king_pos[1] - 2},
            {king_pos[0] - 1, king_pos[1] - 2},
            {king_pos[0] + 1, king_pos[1] + 2},
            {king_pos[0] - 1, king_pos[1] + 2}
        };

        int[][] white_pawn_attacks = {
            {king_pos[0] + 1, king_pos[1] - 1},
            {king_pos[0] + 1, king_pos[1] + 1}
        };

        int[][] black_pawn_attacks = {
            {king_pos[0] - 1, king_pos[1] - 1},
            {king_pos[0] - 1, king_pos[1] + 1}
        };

        if (slidingPiece(board, king_pos, rook_attack, new PieceType[] {PieceType.ROOK, PieceType.QUEEN}, color)) {
            return true;
        }

        if (slidingPiece(board, king_pos, bishop_attack, new PieceType[] {PieceType.BISHOP, PieceType.QUEEN}, color)) {
            return true;
        }

        for (int[] attack : knight_attacks) {
            if (!PieceHandler.inRange(attack)) {
                continue;
            }
            Piece piece = board[attack[0]][attack[1]];
            if (piece.getColor() == color) {
                continue;
            }
            if (piece.getType() == PieceType.KNIGHT) {
                return true;
            }
        }

        // Revise this section
        if (color == PieceColor.BLACK) {
            if (pawnPiece(board, king_pos, white_pawn_attacks, color)) {
                return true;
            }
        } else {
            if (pawnPiece(board, king_pos, black_pawn_attacks, color)) {
                return true;
            }
        }

        if (kingPiece(board, king_pos, rook_attack)) {
            return true;
        }
        if (kingPiece(board, king_pos, bishop_attack)) {
            return true;
        }

        return false;
    }

    public static boolean kingCheck(Piece[][] board, int[] origin_pos, int[] target_pos, PieceColor color) {
        Piece piece = board[origin_pos[0]][origin_pos[1]];
        Piece captured = board[target_pos[0]][target_pos[1]];

        board[target_pos[0]][target_pos[1]] = piece;
        board[origin_pos[0]][origin_pos[1]] = new Empty();

        boolean result = kingInCheck(board, color);

        board[origin_pos[0]][origin_pos[1]] = piece;
        board[target_pos[0]][target_pos[1]] = captured;

        return result;
    }
}
