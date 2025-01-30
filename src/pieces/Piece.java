package src.pieces;
import java.util.*;

import src.pieces.enums.*;

public abstract class Piece {
    protected int[] pos;
    protected PieceColor color;
    protected PieceType type;
    protected int points;

    public PieceType getType() {
        return this.type;
    }

    public PieceColor getColor() {
        return this.color;
    }

    public int getPoints() {
        return this.points;
    }

    public int[] getPosition() {
        return this.pos;
    }

    public void setPosition(int[] pos) {
        this.pos = pos;
    }

    public abstract ArrayList<int[]> getMoves(Piece[][] board);
}