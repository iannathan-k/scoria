package pieces;

import java.util.*;
import pieces.enums.*;

public class Pawn extends Piece {

    private int dir;

    public Pawn(int[] pos, PieceColor color, int dir) {
        this.pos = pos;
        this.color = color;                                                      
        this.type = PieceType.PAWN;
        this.points = 100;
        this.dir = dir;
    }

    public int getDirection() {
        return this.dir;
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
            if (board[moves[i][0]][moves[i][1]].getType() != PieceType.EMPTY) {
                continue;
            }
            if (PieceHandler.kingCheck(board, this.pos, moves[i], this.color)) {
                continue;
            }
            if (i == 0) {
                possible_moves.add(moves[0]);
            } else if (!possible_moves.isEmpty() && (this.pos[0] == 6 || this.pos[0] == 1)) {
                possible_moves.add(moves[1]);
            }
        }

        for (int i = 2; i < 4; i++) {
            if (!PieceHandler.inRange(moves[i])) {
                continue;
            }
            if (board[moves[i][0]][moves[i][1]].getType() == PieceType.EMPTY) {
                continue;
            }
            if (board[moves[i][0]][moves[i][1]].getColor() == this.color) {
                continue;
            }
            if (!PieceHandler.kingCheck(board, this.pos, moves[i], this.color)) {
                possible_moves.add(moves[i]);
            }
        }

        return possible_moves;
    }
}
