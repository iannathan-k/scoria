package src.pieces;

import java.util.*;

public abstract class Pawn{

    // private static boolean passantCheck(Piece[][] board, ArrayDeque<Integer> stack, int offset) {
    //     if (Game.currentMoveNumber() != stack.peek()) {
    //         return false;
    //     }
    //     if (!PieceHandler.inRange(new int[] {this.pos[0], this.pos[1] + offset})) {
    //         return false;
    //     }
    //     if (!(board[this.pos[0]][this.pos[1] + offset] instanceof Pawn)) {
    //         return false;
    //     }
    //     if (board[this.pos[0] + dir][this.pos[1] + offset] != null) {
    //         return false;
    //     }
    //     if (PieceHandler.kingCheck(board, this.pos, new int[] {this.pos[0] + dir, this.pos[1] + offset}, color)) {
    //         return false;
    //     }

    //     return true;
    // }

    public static ArrayList<Integer> getMoves(byte[] board, int pos) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();

        int row = pos >> 3;
        int col = pos & 7;
        int dir = (board[pos] & PieceData.COLOR_MASK) == PieceData.WHITE ? -1 : 1;

        int[][] moves = {
            {row + dir, col},
            {row + dir * 2, col},
            {row + dir, col + 1},
            {row + dir, col - 1}
        };

        for (int i = 0; i < 2; i++) {
            int target = (moves[i][0] << 3) | moves[i][1];          
            if (!PieceHandler.inRange(moves[i][0], moves[i][1])) {
                continue;
            }

            byte piece = board[target];
            if (piece != PieceData.EMPTY) {
                continue;
            }
            if (PieceHandler.kingCheck(board, pos << 8 | target, board[pos] & PieceData.COLOR_MASK)) {
                continue;
            }

            // requires promotion flag
            if (i == 0) {
                possible_moves.add(pos << 8 | target);
                continue;
            }
            // double move
            if (board[target - (dir << 3)] != PieceData.EMPTY) {
                continue;
            }
            if (row == 1 || row == 6) {
                possible_moves.add(pos << 8 | target);
            }
        }

        // capture moves
        for (int i = 2; i < 4; i++) {
            int target = (moves[i][0] << 3) | moves[i][1];
            if (!PieceHandler.inRange(moves[i][0], moves[i][1])) {
                continue;
            }

            byte piece = board[target];
            if (piece == PieceData.EMPTY) {
                continue;
            }
            if ((piece & PieceData.COLOR_MASK) == (board[pos] & PieceData.COLOR_MASK)) {
                continue;
            }
            if (!PieceHandler.kingCheck(board, pos << 8 | target, board[pos] & PieceData.COLOR_MASK)) {
                possible_moves.add(pos << 8 | target);
            }
        }

        // // left passant
        // if (passantCheck(board, left_stack, -1)) {
        //     possible_moves.add(new int[] {this.pos[0] + dir, this.pos[1] - 1});
        // }

        // // right passant
        // if (passantCheck(board, right_stack, 1)) {
        //     possible_moves.add(new int[] {this.pos[0] + dir, this.pos[1] + 1});
        // }

        return possible_moves;
    }
}
