package pieces;

import java.util.*;
import pieces.enums.*;
import src.*;

public abstract class PieceHandler {
    private static King[] king_pieces = new King[2];
    private static int move_number = 0;

    public static int currentMoveNumber() {
        return move_number;
    }

    public static void nextMoveNumber() {
        move_number++;
    }

    public static void lastMoveNumber() {
        move_number--;
    } 

    public static void setKingPiece(King king, int index) {
        king_pieces[index] = king;
    }

    public static King getKingPiece(int index) {
        return king_pieces[index];
    }

    public static boolean isKingStuck(Piece[][] board, PieceColor color) {
        int king_index = (color == PieceColor.WHITE) ? 0 : 1;
        return king_pieces[king_index].getMoves(board).isEmpty();
    }

    public static int[] getKingPos(PieceColor color) {
        int king_index = (color == PieceColor.WHITE) ? 0 : 1;
        return king_pieces[king_index].getPosition();
    }

    public static boolean inRange(int[] pos) {
        return 0 <= pos[0] && pos[0] <= 7 && 0 <= pos[1] && pos[1] <= 7;
    }

    public static ArrayList<int[][]> getAllMoves(Piece[][] board, PieceColor color) {
        ArrayList<int[][]> possible_moves = new ArrayList<int[][]>();

        for (int i = 0; i < 64; i++) {
            Piece piece = board[i / 8][i % 8];
            if (piece.getColor() != color) {
                continue;
            }
            for (int[] move : piece.getMoves(board)) {
                possible_moves.add(new int[][] {{i / 8, i % 8}, move});
            }
        }

        return possible_moves;
    }

    private static boolean slidingPiece(Piece[][] board, int[] king_pos, int[][] dirs, PieceType[] attack_pieces, PieceColor color) {
        for (int[] dir : dirs) {
            int[] attack = {king_pos[0] + dir[0], king_pos[1] + dir[1]};
            while (inRange(attack)) {
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
            if (!inRange(attack)) {
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

    private static boolean kingPiece(Piece[][] board, int[] king_pos, int[][] dirs, PieceColor color) {
        for (int[] dir : dirs) {
            int[] attack = {king_pos[0] + dir[0], king_pos[1] + dir[1]};
            if (!inRange(attack)) {
                continue;
            }
            if (board[attack[0]][attack[1]].getColor() == color) {
                continue;
            }
            if (board[attack[0]][attack[1]].getType() == PieceType.KING) {
                return true;
            }
        }

        return false;
    }

    public static boolean underAttack(Piece[][] board, PieceColor color, int[] pos) {

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
            {pos[0] + 2, pos[1] + 1},
            {pos[0] + 2, pos[1] - 1},
            {pos[0] - 2, pos[1] + 1},
            {pos[0] - 2, pos[1] - 1},
            {pos[0] + 1, pos[1] - 2},
            {pos[0] - 1, pos[1] - 2},
            {pos[0] + 1, pos[1] + 2},
            {pos[0] - 1, pos[1] + 2}
        };

        int[][] white_pawn_attacks = {
            {pos[0] + 1, pos[1] - 1},
            {pos[0] + 1, pos[1] + 1}
        };

        int[][] black_pawn_attacks = {
            {pos[0] - 1, pos[1] - 1},
            {pos[0] - 1, pos[1] + 1}
        };

        if (slidingPiece(board, pos, rook_attack, new PieceType[] {PieceType.ROOK, PieceType.QUEEN}, color)) {
            return true;
        }

        if (slidingPiece(board, pos, bishop_attack, new PieceType[] {PieceType.BISHOP, PieceType.QUEEN}, color)) {
            return true;
        }

        for (int[] attack : knight_attacks) {
            if (!inRange(attack)) {
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

        if (color == PieceColor.BLACK) {
            if (pawnPiece(board, pos, white_pawn_attacks, color)) {
                return true;
            }
        } else {
            if (pawnPiece(board, pos, black_pawn_attacks, color)) {
                return true;
            }
        }

        if (kingPiece(board, pos, rook_attack, color)) {
            return true;
        }
        if (kingPiece(board, pos, bishop_attack, color)) {
            return true;
        }

        return false;
    }

    public static boolean kingCheck(Piece[][] board, int[] origin_pos, int[] target_pos, PieceColor color) {
        Piece[] board_info = MoveHandler.moveState(board, origin_pos, target_pos);

        boolean result = underAttack(board, color, getKingPos(color));

        MoveHandler.undoState(board, origin_pos, target_pos, board_info);

        return result;
    }
}
