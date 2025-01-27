package pieces;
import pieces.enums.*;
import java.util.*;

public abstract class Piece {
    protected int[] pos;
    protected PieceColor color;
    protected PieceType type;
    protected int points;

    public PieceType get_type() {
        return this.type;
    }

    public PieceColor get_color() {
        return this.color;
    }

    public int get_points() {
        return this.points;
    }

    public int[] get_pos() {
        return this.pos;
    }

    public void set_pos(int[] pos) {
        this.pos = pos;
    }

    public abstract ArrayList<int[]> get_moves(Piece[][] board);
}