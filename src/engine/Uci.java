package src.engine;

import src.game.BitBoard;
import src.game.MoveHandler;

public class Uci {

    private static final String[] SQUARE_MAP = {
        "a1","b1","c1","d1","e1","f1","g1","h1",
        "a2","b2","c2","d2","e2","f2","g2","h2",
        "a3","b3","c3","d3","e3","f3","g3","h3",
        "a4","b4","c4","d4","e4","f4","g4","h4",
        "a5","b5","c5","d5","e5","f5","g5","h5",
        "a6","b6","c6","d6","e6","f6","g6","h6",
        "a7","b7","c7","d7","e7","f7","g7","h7",
        "a8","b8","c8","d8","e8","f8","g8","h8"
    };

    public static String moveToUci(int move) {
        int origin = (move >> 6) & MoveHandler.POSITION_MASK;
        int target = move & MoveHandler.POSITION_MASK;

        String uci = SQUARE_MAP[origin] + SQUARE_MAP[target];

        int promotion_piece = (move & MoveHandler.PROMOTED_MASK) >>> 16;
        if (promotion_piece == BitBoard.WHITE_KNIGHT || promotion_piece == BitBoard.BLACK_KNIGHT) {
            uci += "n";
        } else if (promotion_piece == BitBoard.WHITE_BISHOP || promotion_piece == BitBoard.BLACK_BISHOP) {
            uci += "b";
        } else if (promotion_piece == BitBoard.WHITE_ROOK || promotion_piece == BitBoard.BLACK_ROOK) {
            uci += "r";
        } else if (promotion_piece == BitBoard.WHITE_QUEEN || promotion_piece == BitBoard.BLACK_QUEEN) {
            uci += "q";
        }

        return uci;
    }
}
