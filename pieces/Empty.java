package pieces;
import pieces.enums.*;
import java.util.ArrayList;

public class Empty extends Piece {

    private PieceColor color = PieceColor.EMPTY;
    private PieceType type = PieceType.EMPTY;

    public PieceColor getColor() {
        return this.color;
    }

    public PieceType getType() {
        return this.type;
    }

    @Override
    public int[] getPosition() {
        throw new UnsupportedOperationException("!! EMPTY CANNOT GET POSITION !!");
    }

    @Override
    public void setPosition(int[] pos) {
        throw new UnsupportedOperationException("!! EMPTY CANNOT SET POSITION !!");
    }

    @Override
    public ArrayList<int[]> getMoves(Piece[][] board) {
        throw new UnsupportedOperationException("!! EMPTY CANNOT GENERATE MOVES !!");
    }
}
