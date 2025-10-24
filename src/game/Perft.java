package src.game;

import src.user.Uci;
import src.utils.MoveList;

public class Perft {
    // FIXME: Clean this up
    private static long perft(int side, int depth) {
        if (depth == 0) return 1;
        
        long nodes = 0L;
        MoveList move_list = MoveGenerator.generateAllMoves(side);

        if (depth == 1) return move_list.size();

        for (int i = 0; i < move_list.size(); i++) {
            int move = move_list.get(i);

            MoveHandler.doMove(move);
            nodes += perft(side ^ 1, depth - 1);
            MoveHandler.undoMove();
        }

        return nodes;
    }

    // FIXME: Change all System prints to debugger prints instead
    public static void runPerftTest(int side, int depth) {
        long start_time = System.currentTimeMillis();
        long nodes = 0L;
        MoveList move_list = MoveGenerator.generateAllMoves(side);

        for (int i = 0; i < move_list.size(); i++) {
            int move = move_list.get(i);

            MoveHandler.doMove(move);
            long child_nodes = perft(side ^ 1, depth - 1);
            MoveHandler.undoMove();

            System.out.println(Uci.moveToUci(move) + ": " + child_nodes);

            nodes += child_nodes;
        }

        System.out.println("total nodes: " + nodes);
        System.out.println("total time: " + (System.currentTimeMillis() - start_time) + "ms");
    }
}
