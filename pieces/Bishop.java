package pieces;

import java.util.ArrayList;
import pieces.enums.*;

public class Bishop extends Piece {

    public Bishop(int[] pos, PieceColor color) {
        this.pos = pos;
        this.color = color;                                                      
        this.type = PieceType.BISHOP;
        this.points = 330;
    }

    @Override
    public ArrayList<int[]> get_moves(Piece[][] board) {
        ArrayList<int[]> possible_moves = new ArrayList<int[]>();

        int[][] dirs = {
            {1, 1},
            {1, -1},
            {-1, 1},
            {-1, -1}
        };

        for (int[] dir : dirs) {
            int[] move = {this.pos[0] + dir[0], this.pos[1] + dir[1]};
            while (PieceHandler.in_range(move)) {
                if (PieceHandler.king_check(board, this.pos, move, this.color)) {
                    continue;
                }
                if (board[move[0]][move[1]].get_color() == this.color) {
                    break;
                }
                if (board[move[0]][move[1]].get_type() == PieceType.EMPTY) {
                    possible_moves.add(new int[] {move[0], move[1]});
                } else {
                    possible_moves.add(new int[] {move[0], move[1]});
                    break;
                }

                move[0] += dir[0];
                move[1] += dir[1];
            }
        }

        return possible_moves;
    }
    
}
