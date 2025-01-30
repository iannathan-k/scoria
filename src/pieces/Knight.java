package src.pieces;

import java.util.ArrayList;

import src.pieces.enums.*;

public class Knight extends Piece {

    public Knight(int[] pos, PieceColor color) {
        this.pos = pos;
        this.color = color;                                                      
        this.type = PieceType.KNIGHT;
        this.points = 320;
    }

    @Override
    public ArrayList<int[]> getMoves(Piece[][] board) {
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
