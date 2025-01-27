package pieces;

import java.util.ArrayList;
import pieces.enums.*;

public class King extends Piece {

    public King(int[] pos, PieceColor color) {
        this.pos = pos;
        this.color = color;
        this.type = PieceType.KING;
    }

    @Override
    public ArrayList<int[]> get_moves(Piece[][] board) {
        ArrayList<int[]> possible_moves = new ArrayList<int[]>();
        
        int[][] moves = {
            {this.pos[0] + 1, this.pos[1]},
            {this.pos[0] - 1, this.pos[1]},
            {this.pos[0],     this.pos[1] + 1},
            {this.pos[0],     this.pos[1] - 1},
            {this.pos[0] + 1, this.pos[1] + 1},
            {this.pos[0] + 1, this.pos[1] - 1},
            {this.pos[0] - 1, this.pos[1] + 1},
            {this.pos[0] - 1, this.pos[1] - 1}
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
