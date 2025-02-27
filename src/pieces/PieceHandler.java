package src.pieces;

import java.util.*;

import src.core.MoveHandler;

public abstract class PieceHandler {
    private static int[] king_positions = new int[2]; // {white, black}

    private static ArrayDeque<Integer> castle_rights = new ArrayDeque<Integer>();
    private static ArrayDeque<Integer> passant_rights = new ArrayDeque<Integer>();

    // This will cause problems
    public static void setPassantRights(int col) {
        passant_rights.push(passant_rights.pop() | (1 << col));
    }

    public static void clearPassantRights() {
        passant_rights.push(0);
    }

    public static void popPassantRights() {
        passant_rights.pop();
    }

    public static int peekPassantRights() {
        return passant_rights.peek();
    }

    public static void setCastleRights() {

    }

    public static void removeCastleRights() {

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
        return (row | col) >= 0 && row <= 8 && col <= 8;
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
        for (byte piece : board) {
            if (piece == PieceData.EMPTY) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) != color) {
                continue;
            }
            if (generateMoves(board, piece, color).size() > 0) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Integer> getAllMoves(byte[] board, int color) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();

        for (byte piece : board) {
            if (piece == PieceData.EMPTY) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) != color) {
                continue;
            }
            possible_moves.addAll(generateMoves(board, piece, color));
        }

        return possible_moves;
    }

    private static boolean slidingPiece(byte[] board, int pos, int[][] dirs, int attacker, int color) {
        for (int[] dir : dirs) {
            int row = (pos >> 3) + dir[0];
            int col = (pos & 7) + dir[1];
            while (inRange(row, col)) {
                int piece = board[row << 3 | col];
                if (piece == PieceData.EMPTY) {
                    row += dir[0];
                    col += dir[1];
                    continue;
                }
                if ((piece & PieceData.COLOR_MASK) == color) {
                    break;
                }
                if ((piece & PieceData.TYPE_MASK) == attacker) {
                    return true;
                }
                if ((piece & PieceData.TYPE_MASK) == PieceData.QUEEN) {
                    return true;
                }
                break;
            }
        }

        return false;
    }

    private static boolean pawnPiece(byte[] board, int pos, int[][] dirs, int color) {
        for (int[] dir : dirs) {
            int row = (pos >> 3) + dir[0];
            int col = (pos & 7) + dir[1];
            if (!inRange(row, col)) {
                continue;
            }

            byte piece = board[row << 3 | col];
            if ((piece & PieceData.TYPE_MASK) != PieceData.PAWN) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) == color) {
                continue;
            }

            return true;
        }

        return false;
    }

    public static boolean underAttack(byte[] board, int color, int pos) {

        if (slidingPiece(board, pos, PieceData.BISHOP_DIRECTIONS, PieceData.BISHOP, color)) {
            return true;
        }

        if (slidingPiece(board, pos, PieceData.ROOK_DIRECTIONS, PieceData.ROOK, color)) {
            return true;
        }

        for (int[] dir : PieceData.KNIGHT_DIRECTIONS) {
            int row = (pos >> 3) + dir[0];
            int col = (pos & 7) + dir[1];
            if (!inRange(row, col)) {
                continue;
            }

            byte piece = board[row << 3 | col];
            if ((piece & PieceData.TYPE_MASK) != PieceData.KNIGHT) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) == color) {
                continue;
            }

            return true;
        }

        if (color == PieceData.WHITE) {
            if (pawnPiece(board, pos, PieceData.BLACK_PAWN_DIRECTIONS, color)) {
                return true;
            }
        } else {
            if (pawnPiece(board, pos, PieceData.WHITE_PAWN_DIRECTIONS, color)) {
                return true;
            }
        }

        for (int[] dir : PieceData.ALL_DIRECTIONS) {
            int row = (pos >> 3) + dir[0];
            int col = (pos & 7) + dir[1];
            if (!inRange(row, col)) {
                continue;
            } 

            byte piece = board[row << 3 | col];
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
