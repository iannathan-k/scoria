package pieces;
import pieces.enums.*;
import java.util.ArrayList;

public class Empty extends Piece{

    private PieceColor color = PieceColor.EMPTY;
    private PieceType type = PieceType.EMPTY;

    public PieceColor get_color() {
        return this.color;
    }

    public PieceType get_type() {
        return this.type;
    }

    @Override
    public ArrayList<int[]> get_moves(Piece[][] board) {
        throw new UnsupportedOperationException("!! EMPTY CANNOT GENERATE MOVES !!");
    }
}
