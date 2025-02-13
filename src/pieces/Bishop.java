package src.pieces;

import java.util.ArrayList;

import src.pieces.piecedata.*;

public class Bishop extends Piece {

    public Bishop(int[] pos, PieceColor color) {
        this.pos = pos;
        this.color = color;                                                      
        this.type = PieceType.BISHOP;
        this.points = 330;
    }

    @Override
    public ArrayList<int[]> getMoves(Piece[][] board) {
        ArrayList<int[]> possible_moves = new ArrayList<int[]>();

        for (int[] dir : Directions.BISHOP_DIRECTIONS) {
            int[] move = {this.pos[0] + dir[0], this.pos[1] + dir[1]};
            while (PieceHandler.inRange(move)) {
                if (board[move[0]][move[1]].getColor() == this.color) {
                    break;
                }
                if (board[move[0]][move[1]] instanceof Empty) {
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
