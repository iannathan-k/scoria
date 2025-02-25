package src.pieces;

import java.util.*;

import src.core.MoveHandler;
import src.pieces.piecedata.*;

public abstract class PieceHandler {
    private static King[] king_pieces = new King[2]; // {white, black}

    public static void setKingPiece(King king, int index) {
        king_pieces[index] = king;
    }

    public static King getKingPiece(int index) {
        return king_pieces[index];
    }

    public static boolean isKingStuck(Piece[][] board, PieceColor color) {
        return king_pieces[color.ordinal()].getMoves(board).isEmpty();
    }

    public static int[] getKingPos(PieceColor color) {
        return king_pieces[color.ordinal()].getPosition();
    }

    public static boolean inRange(int[] pos) {
        // Review this again
        return (pos[0] | pos[1]) >= 0 && pos[0] < 8 && pos[1] < 8;
    }

    public static boolean hasPossibleMove(Piece[][] board, PieceColor color) {
        for (int i = 0; i < 64; i++) {
            Piece piece = board[i >> 3][i & 7];
            if (piece == null) {
                continue;
            }
             if (piece.getColor() != color) {
                continue;
            }
            if (piece.getMoves(board).size() > 0) {
                return true;
            }
        }

        return false;
    }

    public static ArrayList<int[][]> getAllMoves(Piece[][] board, PieceColor color) {
        ArrayList<int[][]> possible_moves = new ArrayList<int[][]>();

        for (int i = 0; i < 64; i++) {
            Piece piece = board[i >> 3][i & 7];
            if (piece == null) {
                continue;
            }
            if (piece.getColor() != color) {
                continue;
            }
            for (int[] move : piece.getMoves(board)) {
                possible_moves.add(new int[][] {{i >> 3, i & 7}, move});
            }
        }

        return possible_moves;
    }

    private static boolean slidingPiece(Piece[][] board, int[] king_pos, int[][] dirs, PieceType[] attack_pieces, PieceColor color) {
        for (int[] dir : dirs) {
            int[] attack = {king_pos[0] + dir[0], king_pos[1] + dir[1]};
            while (inRange(attack)) {
                Piece piece = board[attack[0]][attack[1]];
                if (piece == null) {
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
            if (!inRange(attack)) {
                continue;
            }
            Piece piece = board[attack[0]][attack[1]];
            if (!(piece instanceof Pawn)) {
                continue;
            }
            if (piece.getColor() == color) {
                continue;
            }

            return true;
        }

        return false;
    }

    private static boolean kingPiece(Piece[][] board, int[] king_pos, int[][] dirs, PieceColor color) {
        for (int[] dir : dirs) {
            int[] attack = {king_pos[0] + dir[0], king_pos[1] + dir[1]};
            if (!inRange(attack)) {
                continue;
            } 
            if (board[attack[0]][attack[1]] == null) {
                continue;
            }
            if (board[attack[0]][attack[1]].getColor() == color) {
                continue;
            }
            if (board[attack[0]][attack[1]] instanceof King) {
                return true;
            }
        }

        return false;
    }

    public static boolean underAttack(Piece[][] board, PieceColor color, int[] pos) {

        int[][] white_pawn_attacks = {
            {pos[0] + 1, pos[1] - 1},
            {pos[0] + 1, pos[1] + 1}
        };

        int[][] black_pawn_attacks = {
            {pos[0] - 1, pos[1] - 1},
            {pos[0] - 1, pos[1] + 1}
        };

        if (slidingPiece(board, pos, Directions.ROOK_DIRECTIONS, Directions.ROOK_QUEEN_TYPES, color)) {
            return true;
        }

        if (slidingPiece(board, pos, Directions.BISHOP_DIRECTIONS, Directions.BISHOP_QUEEN_TYPES, color)) {
            return true;
        }

        for (int[] dir : Directions.KNIGHT_DIRECTIONS) {
            int[] attack = {pos[0] + dir[0], pos[1] + dir[1]};
            if (!inRange(attack)) {
                continue;
            }
            Piece piece = board[attack[0]][attack[1]];
            if (piece == null) {
                continue;
            }
            if (piece.getColor() == color) {
                continue;
            }
            if (piece instanceof Knight) {
                return true;
            }
        }

        if (color == PieceColor.BLACK) {
            if (pawnPiece(board, pos, white_pawn_attacks, color)) {
                return true;
            }
        } else {
            if (pawnPiece(board, pos, black_pawn_attacks, color)) {
                return true;
            }
        }

        if (kingPiece(board, pos, Directions.ALL_DIRECTIONS, color)) {
            return true;
        }

        return false;
    }

    public static boolean kingCheck(Piece[][] board, int[] origin_pos, int[] target_pos, PieceColor color) {
        Piece[] board_info = MoveHandler.pseudoMoveState(board, origin_pos, target_pos);

        boolean result = underAttack(board, color, getKingPos(color));

        MoveHandler.pseudoUndoState(board, origin_pos, target_pos, board_info);

        return result;
    }
}
