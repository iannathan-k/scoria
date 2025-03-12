package src.pieces;

import java.util.*;

import src.core.MoveHandler;

public abstract class PieceHandler {
    private static int[] king_positions = new int[2]; // {white, black}

    private static ArrayDeque<Integer> castle_rights = new ArrayDeque<Integer>();
    private static ArrayDeque<Integer> passant_rights = new ArrayDeque<Integer>(); // you also need for black and white so just << 8 for then the blacks

    /*
     * Passant Rights
     * 00000000 00000000
     * black    white
     * 
     * Castle Rights
     * 0000
     * KQkq
     */

    public static void setPassantRights(int col, int color) {
        passant_rights.push(8 - color + col);
        // invert color
    }

    public static void clearPassantRights() {
        // 16 is unreachable value
        passant_rights.push(16);
    }

    public static void popPassantRights() {
        passant_rights.pop();
    }

    public static int peekPassantRights() {
        return passant_rights.peek();
    }

    public static void clearCastleRights() {
        castle_rights.push(0b0000);
    }

    public static void setCastleRights(int mask) {
        castle_rights.push(castle_rights.peek() | mask);
    }

    public static void maskCastleRights(int mask, int color) {
        int offset = (color == PieceData.WHITE) ? 0 : 2;
        castle_rights.push(castle_rights.peek() & (mask << offset));
    }

    public static void popCastleRights() {
        castle_rights.pop();
    }

    public static int peekCastleRights() {
        return castle_rights.peek();
    }

    public static void setKingPosition(int pos, int color) {
        king_positions[color == PieceData.WHITE ? 0 : 1] = pos;
    }

    public static int getKingPos(int color) {
        return king_positions[color == PieceData.WHITE ? 0 : 1];
    }
    
    public static boolean isKingStuck(byte[] board, int color) {
        return King.getMoves(board, getKingPos(color)).isEmpty();
    }

    public static boolean inRange(int row, int col) {
        return (row | col) > -1 && row < 8 && col < 8;
    }

    public static ArrayList<Integer> generateMoves(byte[] board, int type, int pos) {
        return switch(type) {
            case PieceData.PAWN -> Pawn.getMoves(board, pos);
            case PieceData.KNIGHT -> Knight.getMoves(board, pos);
            case PieceData.BISHOP -> Bishop.getMoves(board, pos);
            case PieceData.ROOK -> Rook.getMoves(board, pos);
            case PieceData.QUEEN -> Queen.getMoves(board, pos);
            case PieceData.KING -> King.getMoves(board, pos);
            default -> throw new IllegalArgumentException("Invalid Type");
        };
    }

    public static boolean hasPossibleMove(byte[] board, int color) {
        for (int i = 0; i < 64; i++) {
            byte piece = board[i];
            if (piece == PieceData.EMPTY) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) != color) {
                continue;
            }
            if (generateMoves(board, piece & PieceData.TYPE_MASK, i).size() > 0) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Integer> getAllMoves(byte[] board, int color) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();

        for (int i = 0; i < 64; i++) {
            byte piece = board[i];
            if (piece == PieceData.EMPTY) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) != color) {
                continue;
            }
            possible_moves.addAll(generateMoves(board, piece & PieceData.TYPE_MASK, i));
        }
        return possible_moves;
    }

    private static boolean slidingPiece(byte[] board, int row, int col, int[][] dirs, int attacker, int color) {
        for (int[] dir : dirs) {
            int new_row = row + dir[0];
            int new_col = col + dir[1];
            while (inRange(new_row, new_col)) {
                int target = new_row << 3 | new_col;
                byte piece = board[target];

                if (piece == PieceData.EMPTY) {
                    new_row += dir[0];
                    new_col += dir[1];
                    continue;
                }
                if ((piece & PieceData.COLOR_MASK) == color) {
                    break;
                }

                int type = piece & PieceData.TYPE_MASK;
                if (type == attacker || type == PieceData.QUEEN) {
                    return true;
                }
                break;
            }
        }

        return false;
    }

    public static boolean underAttack(byte[] board, int color, int pos) {
        int row = pos >> 3;
        int col = pos & 7;

        if (slidingPiece(board, row, col, PieceData.BISHOP_DIRECTIONS, PieceData.BISHOP, color)) {
            return true;
        }

        if (slidingPiece(board, row, col, PieceData.ROOK_DIRECTIONS, PieceData.ROOK, color)) {
            return true;
        }

        for (int[] dir : PieceData.KNIGHT_DIRECTIONS) {
            int new_row = row + dir[0];
            int new_col = col + dir[1];
            if (!inRange(new_row, new_col)) {
                continue;
            }

            byte piece = board[new_row << 3 | new_col];
            if ((piece & PieceData.TYPE_MASK) != PieceData.KNIGHT) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) == color) {
                continue;
            }

            return true;
        }

        int[][] dirs = (color == PieceData.WHITE) ? PieceData.BLACK_PAWN_DIRECTIONS : PieceData.WHITE_PAWN_DIRECTIONS;
        for (int[] dir : dirs) {
            int new_row = row + dir[0];
            int new_col = col + dir[1];
            if (!inRange(new_row, new_col)) {
                continue;
            }

            byte piece = board[new_row << 3 | new_col];
            if ((piece & PieceData.TYPE_MASK) != PieceData.PAWN) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) == color) {
                continue;
            }

            return true;
        }

        // king
        for (int[] dir : PieceData.ALL_DIRECTIONS) {
            int new_row = row + dir[0];
            int new_col = col + dir[1];
            if (!inRange(new_row, new_col)) {
                continue;
            } 

            byte piece = board[new_row << 3 | new_col];
            if ((piece & PieceData.TYPE_MASK) != PieceData.KING) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) == color) {
                continue;
            }

            return true;
        }

        return false;
    }

    public static boolean kingCheck(byte[] board, int move, int color) {
        byte captured = MoveHandler.pseudoMoveState(board, move);

        boolean result = underAttack(board, color, getKingPos(color));

        MoveHandler.pseudoUndoState(board, move, captured);

        return result;
    }
}
