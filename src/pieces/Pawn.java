package src.pieces;

import java.util.*;

import src.core.Game;
import src.pieces.enums.*;

public class Pawn extends Piece {

    private int dir;
    private ArrayDeque<Integer> left_stack = new ArrayDeque<Integer>();
    private ArrayDeque<Integer> right_stack = new ArrayDeque<Integer>();

    public Pawn(int[] pos, PieceColor color, int dir) {
        this.pos = pos;
        this.color = color;                                                      
        this.type = PieceType.PAWN;
        this.points = 100;
        this.dir = dir;
        this.left_stack.push(-1);
        this.right_stack.push(-1);
    }

    public int getDirection() {
        return this.dir;
    }

    public void pushLeft(int move_count) {
        this.left_stack.push(move_count);
    }

    public void pushRight(int move_count) {
        this.right_stack.push(move_count);
    }

    public void popLeft() {
        this.left_stack.pop();
    }

    public void popRight() {
        this.right_stack.pop();
    }

    public int peekLeft() {
        return this.left_stack.peek();
    }

    public int peekRight() {
        return this.right_stack.peek();
    }

    private boolean passantCheck(Piece[][] board, ArrayDeque<Integer> stack, int offset) {
        if (Game.currentMoveNumber() != stack.peek()) {
            return false;
        }
        if (!PieceHandler.inRange(new int[] {this.pos[0], this.pos[1] + offset})) {
            return false;
        }
        if (!(board[this.pos[0]][this.pos[1] + offset] instanceof Pawn)) {
            return false;
        }
        if (!(board[this.pos[0] + dir][this.pos[1] + offset] instanceof Empty)) {
            return false;
        }
        if (PieceHandler.kingCheck(board, this.pos, new int[] {this.pos[0] + dir, this.pos[1] + offset}, color)) {
            return false;
        }

        return true;
    }

    @Override
    public ArrayList<int[]> getMoves(Piece[][] board) {
        ArrayList<int[]> possible_moves = new ArrayList<int[]>();

        int[][] moves = {
            {this.pos[0] + this.dir, this.pos[1]},
            {this.pos[0] + this.dir * 2, this.pos[1]},
            {this.pos[0] + this.dir, this.pos[1] + 1},
            {this.pos[0] + this.dir, this.pos[1] - 1}
        };

        for (int i = 0; i < 2; i++) {
            if (!PieceHandler.inRange(moves[i])) {
                continue;
            }
            if (!(board[moves[i][0]][moves[i][1]] instanceof Empty)) {
                continue;
            }
            if (PieceHandler.kingCheck(board, this.pos, moves[i], this.color)) {
                continue;
            }
            if (i == 0) {
                possible_moves.add(moves[0]);
                continue;
            }
            if (!(board[moves[i][0] - dir][moves[i][1]] instanceof Empty)) {
                continue;
            }
            if (this.pos[0] == 6 || this.pos[0] == 1) {
                possible_moves.add(moves[1]);
            }
        }

        // capture moves
        for (int i = 2; i < 4; i++) {
            if (!PieceHandler.inRange(moves[i])) {
                continue;
            }
            if (board[moves[i][0]][moves[i][1]] instanceof Empty) {
                continue;
            }
            if (board[moves[i][0]][moves[i][1]].getColor() == this.color) {
                continue;
            }
            if (!PieceHandler.kingCheck(board, this.pos, moves[i], this.color)) {
                possible_moves.add(moves[i]);
            }
        }

        // left passant
        if (passantCheck(board, left_stack, -1)) {
            possible_moves.add(new int[] {this.pos[0] + dir, this.pos[1] - 1});
        }
        if (passantCheck(board, right_stack, 1)) {
            possible_moves.add(new int[] {this.pos[0] + dir, this.pos[1] + 1});
        }

        return possible_moves;
    }
}
