package src.game;

import src.engine.Uci;
import src.utils.GameStack;
import src.utils.MoveList;

public class Perft {
    public static int perft(boolean side, int depth) {
        if (depth == 0) return 1;
        
        int nodes = 0;

        int color = (side) ? BitBoard.WHITE : BitBoard.BLACK;
        MoveList possible_moves = MoveGenerator.generateAllMoves(color);
        for (int i = 0; i < possible_moves.size(); i++) {
            int move = possible_moves.get(i);

            if (BitBoard.getPieceAt((move >> 6) & MoveHandler.POSITION_MASK) == -1) {
                System.out.println(Uci.moveToUci(move));
                BitBoard.printBoard();
                System.out.println();
                BitBoard.printOccupancy(BitBoard.occupancy_bitboard);
                BitBoard.printOccupancy(BitBoard.color_bitboards[0]);
                BitBoard.printOccupancy(BitBoard.color_bitboards[1]);
            }

            MoveHandler.doMove(move);
            nodes += perft(!side, depth - 1);
            MoveHandler.undoMove();
        }

        return nodes;
    }

    public static void main(String[] args) {
        BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        BitBoard.printBoard();
        Precomputer.initAllMoveTables();
        GameStack.initGameStack();

        // MoveList moves = MoveGenerator.generateAllMoves(BitBoard.WHITE);
        // System.out.println("MOVES: " + moves.size());
        // for (int i = 0; i < moves.size(); i++) {
        //     System.out.println(Uci.moveToUci(moves.get(i)));
        // }
        // BitBoard.printMoveMap(moves);
        System.out.println("nodes: " + perft(true, 4));
    }
}
