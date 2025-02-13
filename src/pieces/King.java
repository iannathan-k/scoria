package src.pieces;

import java.util.*;

import src.pieces.piecedata.*;

public class King extends Piece {

    private ArrayDeque<Boolean> moved_stack = new ArrayDeque<Boolean>();

    public King(int[] pos, PieceColor color) {
        this.pos = pos;
        this.color = color;
        this.type = PieceType.KING;
        this.moved_stack.push(true);
    }

    public void pushMove() {
        moved_stack.push(true);
    }

    public void pushMovedFalse() {
        moved_stack.push(false);
    }

    public void popMove() {
        moved_stack.pop();
    }

    public boolean peekMove() {
        return moved_stack.peek();
    }

    private boolean canCastle(Piece[][] board, int dir) {
        if (PieceHandler.underAttack(board, this.color, this.pos)) {
            return false;
        }
        int[] square = new int[] {this.pos[0], this.pos[1] + dir};
        while (square[1] > 0 && square[1] < 7) {
            if (!(board[square[0]][square[1]] instanceof Empty)) {
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

    @Override
    public ArrayList<int[]> getMoves(Piece[][] board) {
        ArrayList<int[]> possible_moves = new ArrayList<int[]>();

        for (int[] direction : Directions.ALL_DIRECTIONS) {
            int[] move = new int[] {this.pos[0] + direction[0], this.pos[1] + direction[1]};
            if (!PieceHandler.inRange(move)) {
                continue;
            }
            if (board[move[0]][move[1]].getColor() == this.color) {
                continue;
            }
            if (!PieceHandler.kingCheck(board, this.pos, move, this.color)) {
                possible_moves.add(new int[] {move[0], move[1]});
            }
        }

        if (!moved_stack.peek() && canCastle(board, -1)) {
            possible_moves.add(new int[] {this.pos[0], this.pos[1] - 2});
        }
        if (!moved_stack.peek() && canCastle(board, 1)) {
            possible_moves.add(new int[] {this.pos[0], this.pos[1] + 2});
        }

        return possible_moves;
    }
    
}
