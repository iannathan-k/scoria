package src.game;

import src.engine.Uci;
import src.utils.GameStack;
import src.utils.MoveList;

public class Perft {
    public static long perft(int side, int depth) {
        if (depth == 0) return 1;
        
        long nodes = 0;
        MoveList possible_moves = MoveGenerator.generateAllMoves(side);

        if (depth == 1) return possible_moves.size();

        for (int i = 0; i < possible_moves.size(); i++) {
            int move = possible_moves.get(i);

            MoveHandler.doMove(move);
            nodes += perft(side ^ 1, depth - 1);
            MoveHandler.undoMove();
        }

        return nodes;
    }

    public static void main(String[] args) {
        BitBoard.initBoardByFen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8");
        // BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        BitBoard.printBoard();
        Precomputer.initAllMoveTables();
        GameStack.initGameStack();

        int depth = 3;
        int side = BitBoard.WHITE;
        long total = 0;

        long start = System.nanoTime();

        MoveList moves = MoveGenerator.generateAllMoves(side);
        for (int i = 0; i < moves.size(); i++) {
            int move = moves.get(i);
            MoveHandler.doMove(move);

            long subnodes = perft(side ^ 1, depth - 1);
            total += subnodes;

            System.out.println(Uci.moveToUci(move) + ": " + subnodes);

            MoveHandler.undoMove();
        }
        BitBoard.printMoveMap(moves);

        System.out.println("TOTAL: " + total);
        System.out.println("TIME: " + (System.nanoTime() - start) / 1_000_000 + "ms");
    }
}
