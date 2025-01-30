package src.core;

import src.pieces.*;

public class MoveHandler {
    public static Piece[] moveState(Piece[][] board, int[] origin_pos, int[] target_pos) {
        Game.nextMoveNumber();
        Piece piece = board[origin_pos[0]][origin_pos[1]];
        Piece captured = board[target_pos[0]][target_pos[1]];

        // moved logic
        if (piece instanceof Rook) {
            ((Rook) piece).pushMove();
        } else if (piece instanceof King) {
            ((King) piece).pushMove();
        }

        if (piece instanceof Pawn) {
            // pawn double move
            if (Math.abs(origin_pos[0] - target_pos[0]) > 1) {
                int[] left_pos = {target_pos[0], target_pos[1] - 1};
                int[] right_pos = {target_pos[0], target_pos[1] + 1};
                if (PieceHandler.inRange(left_pos) && board[left_pos[0]][left_pos[1]] instanceof Pawn) {
                    ((Pawn) board[left_pos[0]][left_pos[1]]).pushRight(Game.currentMoveNumber());
                }
                if (PieceHandler.inRange(right_pos) && board[right_pos[0]][right_pos[1]] instanceof Pawn) {
                    ((Pawn) board[right_pos[0]][right_pos[1]]).pushLeft(Game.currentMoveNumber());
                }
            }

            if (captured instanceof Empty && origin_pos[1] != target_pos[1]) {
                // en passant
                captured = board[origin_pos[0]][target_pos[1]];
                board[target_pos[0]][target_pos[1]] = piece;
                board[origin_pos[0]][target_pos[1]] = new Empty();
                board[origin_pos[0]][origin_pos[1]] = new Empty();
                piece.setPosition(target_pos);
                return new Piece[] {captured, piece};
            }

            if (target_pos[0] == 0 || target_pos[0] == 7) {
                // promotion logic
                board[origin_pos[0]][origin_pos[1]] = new Empty();
                board[target_pos[0]][target_pos[1]] = new Queen(target_pos, piece.getColor());
                return new Piece[] {captured, piece};
            }
        }

        if (piece instanceof King && Math.abs(target_pos[1] - origin_pos[1]) > 1) {
            // castle logic
            int rook_col = (target_pos[1] == 6) ? 7 : 0;
            int dir = (origin_pos[1] - target_pos[1]) / 2;
            captured = board[target_pos[0]][rook_col];

            // move king
            board[target_pos[0]][target_pos[1]] = piece;
            board[origin_pos[0]][origin_pos[1]] = new Empty();
            piece.setPosition(target_pos);
            ((King) piece).pushMove();

            // move rook
            board[origin_pos[0]][rook_col] = new Empty();
            board[target_pos[0]][target_pos[1] + dir] = captured;
            ((Rook) captured).pushMove();
            captured.setPosition(new int[] {target_pos[0], target_pos[1] + dir});

            return new Piece[] {captured, piece};
        }

        board[target_pos[0]][target_pos[1]] = piece;
        board[origin_pos[0]][origin_pos[1]] = new Empty();
        piece.setPosition(target_pos);
        Piece[] board_info = {captured, piece};

        return board_info;
    }

    public static void undoState(Piece[][] board, int[] origin_pos, int[] target_pos, Piece[] board_info) {
        Game.lastMoveNumber();
        Piece piece = board_info[1];
        Piece captured = board_info[0];

        // unmoved logic
        if (piece instanceof Rook) {
            ((Rook) piece).popMove();

        } else if (piece instanceof King) {
            ((King) piece).popMove();
        }

        if (piece instanceof Pawn) {
            if (Math.abs(origin_pos[0] - target_pos[0]) > 1) {
                // double move
                int[] left_pos = {target_pos[0], target_pos[1] - 1};
                int[] right_pos = {target_pos[0], target_pos[1] + 1};
                if (PieceHandler.inRange(left_pos) && board[left_pos[0]][left_pos[1]] instanceof Pawn) {
                    ((Pawn) board[left_pos[0]][left_pos[1]]).popRight();
                }
                if (PieceHandler.inRange(right_pos) && board[right_pos[0]][right_pos[1]] instanceof Pawn) {
                    ((Pawn) board[right_pos[0]][right_pos[1]]).popLeft();
                }
            }

            if (target_pos[0] == 0 || target_pos[0] == 7) {
                // unpromote logic
                board[origin_pos[0]][origin_pos[1]] = piece;
                board[target_pos[0]][target_pos[1]] = captured;
                return;
            }

            int dir = ((Pawn) piece).getDirection();
            if (captured instanceof Pawn && captured.getPosition()[0] == piece.getPosition()[0] - dir) {
                // unpassant logic
                board[target_pos[0]][target_pos[1]] = new Empty();
                board[origin_pos[0]][target_pos[1]] = captured;
                board[origin_pos[0]][origin_pos[1]] = piece;
                piece.setPosition(origin_pos);
                return;
            }
        }

        if (piece instanceof King && Math.abs(target_pos[1] - origin_pos[1]) > 1) {
            // uncastle logic
            int rook_col = (target_pos[1] == 6) ? 7 : 0;
            int dir = (origin_pos[1] - target_pos[1]) / 2;

            // ummove king
            board[origin_pos[0]][origin_pos[1]] = piece;
            board[target_pos[0]][target_pos[1]] = new Empty();
            piece.setPosition(origin_pos);

            // ummove rook
            board[origin_pos[0]][rook_col] = captured;
            board[target_pos[0]][target_pos[1] + dir] = new Empty();
            ((Rook) captured).popMove();
            captured.setPosition(new int[] {origin_pos[0], rook_col});

            return;
        }

        board[origin_pos[0]][origin_pos[1]] = piece;
        board[target_pos[0]][target_pos[1]] = captured;
        piece.setPosition(origin_pos);
    }
}
