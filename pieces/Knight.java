package pieces;

import java.util.ArrayList;
import pieces.enums.*;

public class Knight extends Piece {

    public Knight(int[] pos, PieceColor color) {
        this.pos = pos;
        this.color = color;                                                      
        this.type = PieceType.KNIGHT;
        this.points = 320;
    }

    @Override
    public ArrayList<int[]> get_moves(Piece[][] board) {
        ArrayList<int[]> possible_moves = new ArrayList<int[]>();
        
        int[][] moves = {
            {this.pos[0] + 2, this.pos[1] + 1},
            {this.pos[0] + 2, this.pos[1] - 1},
            {this.pos[0] - 2, this.pos[1] + 1},
            {this.pos[0] - 2, this.pos[1] - 1},
            {this.pos[0] + 1, this.pos[1] - 2},
            {this.pos[0] - 1, this.pos[1] - 2},
            {this.pos[0] + 1, this.pos[1] + 2},
            {this.pos[0] - 1, this.pos[1] + 2}
        };

        for (int[] move : moves) {
            if (!PieceHandler.in_range(move)) {
                continue;
            }
            if (board[move[0]][move[1]].get_color() == this.color) {
                continue;
            }
            if (!PieceHandler.king_check(board, this.pos, move, this.color)) {
                possible_moves.add(move);
            }
        }

        return possible_moves;

    }
    
}
