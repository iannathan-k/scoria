package src.pieces;

import java.util.*;

import src.core.MoveHandler;

public abstract class PieceHandler {
    private static int[] king_positions = new int[2]; // {white, black}

    private static ArrayDeque<Integer> castle_rights = new ArrayDeque<Integer>();
    private static ArrayDeque<Integer> passant_rights = new ArrayDeque<Integer>();

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
        // invert color
        passant_rights.push(8 - color + col);
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
        castle_rights.push(0);
    }

    public static void setCastleRights(int mask) {
        castle_rights.push(castle_rights.peek() | mask);
    }

    public static void maskCastleRights(int mask) {
        castle_rights.push(castle_rights.peek() & mask);
    }

    public static void popCastleRights() {
        castle_rights.pop();
    }

    public static int peekCastleRights() {
        return castle_rights.peek();
    }

    public static void setKingPosition(int pos, int color) {
        king_positions[color >> 3] = pos;
    }

    public static int getKingPosition(int color) {
        return king_positions[color >> 3];
    }

    public static boolean inRange(int row, int col) {
        return (row | col) > -1 && (row | col) < 8;
    }

    public static ArrayList<Integer> generateMoves(byte[] board, int type, int color, int pos) {
        return switch(type) {
            case PieceData.PAWN -> Pawn.getMoves(board, pos, color);
            case PieceData.KNIGHT -> Knight.getMoves(board, pos, color);
            case PieceData.BISHOP -> Bishop.getMoves(board, pos, color);
            case PieceData.ROOK -> Rook.getMoves(board, pos, color);
            case PieceData.QUEEN -> Queen.getMoves(board, pos, color);
            case PieceData.KING -> King.getMoves(board, pos, color);
            default -> throw new IllegalArgumentException("Invalid Type");
        };
    }

    public static boolean checkMobility(byte[] board, int type, int color, int pos) {
        return switch(type) {
            case PieceData.PAWN -> Pawn.hasMove(board, pos, color);
            case PieceData.KNIGHT -> Knight.hasMove(board, pos, color);
            case PieceData.BISHOP -> Bishop.hasMove(board, pos, color);
            case PieceData.ROOK -> Rook.hasMove(board, pos, color);
            case PieceData.QUEEN -> Queen.hasMove(board, pos, color);
            case PieceData.KING -> King.hasMove(board, pos, color);
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
            if (checkMobility(board, piece & PieceData.TYPE_MASK, color, i)) {
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
            possible_moves.addAll(generateMoves(board, piece & PieceData.TYPE_MASK, color, i));
        }
        return possible_moves;
    }

    private static boolean slidingPiece(byte[] board, int[][] premoves, int attacker, int opponent_color) {
        int queen_attacker = PieceData.QUEEN | opponent_color;
        attacker |= opponent_color;
        for (int[] dir : premoves) {
            for (int target : dir) {
                byte target_piece = board[target];

                if (target_piece == PieceData.EMPTY) {
                    continue;
                }
                if (target_piece == attacker) {
                    return true;
                }
                if (target_piece == queen_attacker) {
                    return true;
                }

                break;
            }
        }

        return false;
    }

    public static boolean underAttack(byte[] board, int color, int pos) {
        int opponent_color = 8 - color;

        if (slidingPiece(board, PreComputer.BISHOP_PREMOVES[pos], PieceData.BISHOP, opponent_color)) {
            return true;
        }

        if (slidingPiece(board, PreComputer.ROOK_PREMOVES[pos], PieceData.ROOK, opponent_color)) {
            return true;
        }

        int opponent = PieceData.KNIGHT | opponent_color;
        for (int target : PreComputer.KNIGHT_PREMOVES[pos]) {
            if (board[target] == opponent) {
                return true;
            }
        }

        // pawn
        opponent = PieceData.PAWN | opponent_color;
        int[] attacks = (color == PieceData.WHITE) 
            ? PreComputer.WHITE_PAWN_PREATTACKS[pos] 
            : PreComputer.BLACK_PAWN_PREATTACKS[pos];

        for (int target : attacks) {
            if (board[target] == opponent) {
                return true;
            }
        }

        // king
        opponent = PieceData.KING | opponent_color;
        for (int target : PreComputer.KING_PREMOVES[pos]) {
            if (board[target] == opponent) {
                return true;
            }
        }

        return false;
    }

    public static boolean kingCheck(byte[] board, int move, int color) {
        byte captured = MoveHandler.pseudoMoveState(board, move);

        boolean result = underAttack(board, color, getKingPosition(color));

        MoveHandler.pseudoUndoState(board, move, captured);

        return result;
    }

    public static boolean kingUnderAttack(byte[] board, int color) {
        return underAttack(board, color, getKingPosition(color));
    }
}
