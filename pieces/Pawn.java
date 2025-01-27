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

    @Override
    public ArrayList<int[]> get_moves(Piece[][] board) {
        ArrayList<int[]> possible_moves = new ArrayList<int[]>();

        int[][] moves = {
            {this.pos[0] + this.dir, this.pos[1]},
            {this.pos[0] + this.dir * 2, this.pos[1]},
            {this.pos[0] + this.dir, this.pos[1] + 1},
            {this.pos[0] + this.dir, this.pos[1] - 1}
        };

        if (PieceHandler.in_range(moves[0])) {
            if (board[moves[0][0]][moves[0][1]].get_type() == PieceType.EMPTY) {
                if (!PieceHandler.king_check(board, this.pos, moves[0], this.color)) {
                    possible_moves.add(moves[0]);
                }
            }
        }

        if (PieceHandler.in_range(moves[1])) {
            if (board[moves[1][0]][moves[1][1]].get_type() == PieceType.EMPTY) {
                if (!possible_moves.isEmpty() && (this.pos[0] == 6 || this.pos[0] == 1)) {
                    if (!PieceHandler.king_check(board, this.pos, moves[1], this.color)) {
                        possible_moves.add(moves[1]);
                    }
                }
            }
        }

        for (int i = 2; i < 4; i++) {
            if (!PieceHandler.in_range(moves[i])) {
                continue;
            }
            if (board[moves[i][0]][moves[i][1]].get_type() == PieceType.EMPTY) {
                continue;
            }
            if (board[moves[i][0]][moves[i][1]].get_color() == this.color) {
                continue;
            }
            if (!PieceHandler.king_check(board, this.pos, moves[i], this.color)) {
                possible_moves.add(moves[i]);
            }
        }

        return possible_moves;
    }
}
