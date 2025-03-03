package src.pieces;

import java.util.*;

public abstract class King {

    // public static boolean canCastle(Piece[][] board, int dir) {
    //     if (moved_stack.peek()) {
    //         return false;
    //     }
    //     if (PieceHandler.underAttack(board, this.color, this.pos)) {
    //         return false;
    //     }
    //     int[] square = new int[] {this.pos[0], this.pos[1] + dir};

    //     while (square[1] > 0 && square[1] < 7) {
    //         if (board[square[0]][square[1]] != null) {
    //             return false;
    //         }
    //         if (PieceHandler.underAttack(board, this.color, square)) {
    //             return false;
    //         }

    //         square[1] += dir;
    //     }

    //     if (!(board[square[0]][square[1]] instanceof Rook)) {
    //         return false;
    //     }
    //     if (((Rook) board[square[0]][square[1]]).peekMove()) {
    //         return false;
    //     }

    //     return true;
    // }

    public static ArrayList<Integer> getMoves(byte[] board, int pos) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();
        int color = board[pos] & PieceData.COLOR_MASK;

        for (int[] dir : PieceData.ALL_DIRECTIONS) {
            int row = (pos >> 3) + dir[0];
            int col = (pos & 7) + dir[1];
            if (!PieceHandler.inRange(row, col)) {
                continue;
            }

            int target = row << 3 | col;
            int move = pos << 8 | target;
            byte target_piece = board[target];

            if (target_piece == PieceData.EMPTY) {
                if (!PieceHandler.kingCheck(board, move, color)) {
                    possible_moves.add(move);
                }
            } else if ((target_piece & PieceData.COLOR_MASK) != color) {
                if (!PieceHandler.kingCheck(board, move, color)) {
                    possible_moves.add(move);
                }
            }
        }

        // if (canCastle(board, -1)) { // Leftside castle
        //     possible_moves.add(new int[] {this.pos[0], this.pos[1] - 2});
        // }
        // if (canCastle(board, 1)) { // Rightside castle
        //     possible_moves.add(new int[] {this.pos[0], this.pos[1] + 2});
        // }

        return possible_moves;
    }
    
}
