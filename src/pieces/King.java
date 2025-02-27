package src.pieces;

import java.util.*;

public abstract class King {

    public static boolean canCastle(Piece[][] board, int dir) {
        if (moved_stack.peek()) {
            return false;
        }
        if (PieceHandler.underAttack(board, this.color, this.pos)) {
            return false;
        }
        int[] square = new int[] {this.pos[0], this.pos[1] + dir};

        while (square[1] > 0 && square[1] < 7) {
            if (board[square[0]][square[1]] != null) {
                return false;
            }
            if (PieceHandler.underAttack(board, this.color, square)) {
                return false;
            }

            square[1] += dir;
        }

        if (!(board[square[0]][square[1]] instanceof Rook)) {
            return false;
        }
        if (((Rook) board[square[0]][square[1]]).peekMove()) {
            return false;
        }

        return true;
    }

    public static ArrayList<int[]> getMoves(Piece[][] board) {
        ArrayList<int[]> possible_moves = new ArrayList<int[]>();

        for (int[] direction : PieceDatra.ALL_DIRECTIONS) {
            int[] move = new int[] {this.pos[0] + direction[0], this.pos[1] + direction[1]};
            if (!PieceHandler.inRange(move)) {
                continue;
            }
            if (board[move[0]][move[1]] != null && board[move[0]][move[1]].getColor() == this.color) {
                continue;
            }
            if (!PieceHandler.kingCheck(board, this.pos, move, this.color)) {
                possible_moves.add(new int[] {move[0], move[1]});
            }
        }

        if (canCastle(board, -1)) { // Leftside castle
            possible_moves.add(new int[] {this.pos[0], this.pos[1] - 2});
        }
        if (canCastle(board, 1)) { // Rightside castle
            possible_moves.add(new int[] {this.pos[0], this.pos[1] + 2});
        }

        return possible_moves;
    }
    
}
