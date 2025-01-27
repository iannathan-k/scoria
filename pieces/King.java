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
    public ArrayList<int[]> getMoves(Piece[][] board) {
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
            if (!PieceHandler.inRange(move)) {
                continue;
            }
            if (board[move[0]][move[1]].getColor() == this.color) {
                continue;
            }
            if (!PieceHandler.kingCheck(board, this.pos, move, this.color)) {
                possible_moves.add(move);
            }
        }

        return possible_moves;
    }
    
}
