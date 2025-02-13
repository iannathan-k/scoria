package src.pieces;

import java.util.ArrayList;

import src.pieces.piecedata.*;

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

        for (int[] direction : Directions.KNIGHT_DIRECTIONS) {
            int[] move = {this.pos[0] + direction[0], this.pos[1] + direction[1]};
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

        return possible_moves;

    }
    
}
