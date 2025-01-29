package pieces;

import java.util.ArrayDeque;
import java.util.ArrayList;
import pieces.enums.*;

public class Rook extends Piece {

    public ArrayDeque<Boolean> moved_stack = new ArrayDeque<Boolean>();

    public Rook(int[] pos, PieceColor color) {
        this.pos = pos;
        this.color = color;                                                      
        this.type = PieceType.ROOK;
        this.points = 500;
        this.moved_stack.push(false);
    }

    public void pushMove() {
        moved_stack.push(true);
    }

    public void popMove() {
        moved_stack.pop();
    }

    public boolean peekMoved() {
        return moved_stack.peek();
    }

    @Override
    public ArrayList<int[]> getMoves(Piece[][] board) {
        ArrayList<int[]> possible_moves = new ArrayList<int[]>();

        int[][] dirs = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1}
        };

        for (int[] dir : dirs) {
            int[] move = {this.pos[0] + dir[0], this.pos[1] + dir[1]};
            while (PieceHandler.inRange(move)) {
                if (board[move[0]][move[1]].getColor() == this.color) {
                    break;
                }
                if (board[move[0]][move[1]].getType() == PieceType.EMPTY) {
                    if (!PieceHandler.kingCheck(board, this.pos, move, this.color)) {
                        possible_moves.add(new int[] {move[0], move[1]});
                    }
                } else {
                    if (!PieceHandler.kingCheck(board, this.pos, move, this.color)) {
                        possible_moves.add(new int[] {move[0], move[1]});
                    }
                    break;
                }

                move[0] += dir[0];
                move[1] += dir[1];
            }
        }

        return possible_moves;
    }
    
}
