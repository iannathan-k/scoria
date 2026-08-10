package src.game;

import src.user.Uci;
import src.utils.MoveList;
import src.utils.Logger;

public class Perft {

    private static long perft(int side, int depth) {        
        long nodes = 0L;
        MoveList move_list = MoveGenerator.generateAllMoves(side);

        for (int i = 0; i < move_list.size(); i++) {
            int move = move_list.getMove(i);

            MoveHandler.doMove(move);

            // Verify Pseudo Legality
            if (MoveGenerator.isKingInCheck(side)) {
                MoveHandler.undoMove();
                continue;
            }

            nodes += (depth == 1) ? 1 : perft(side ^ 1, depth - 1);
            MoveHandler.undoMove();
        }

        return nodes;
    }

    public static void runPerftTest(int side, int depth) {
        long start_time = System.currentTimeMillis();
        long nodes = 0L;

        MoveList move_list = MoveGenerator.generateAllMoves(side);

        for (int i = 0; i < move_list.size(); i++) {
            int move = move_list.getMove(i);

            MoveHandler.doMove(move);

            // Verify Pseudo Legality
            if (MoveGenerator.isKingInCheck(side)) {
                MoveHandler.undoMove();
                continue;
            }

            long child_nodes = (depth == 1) ? 1 : perft(side ^ 1, depth - 1);
            MoveHandler.undoMove();

            Logger.outln(Uci.moveToUci(move) + ": " + child_nodes);

            nodes += child_nodes;
        }

        Logger.outln("total nodes: " + nodes);
        Logger.outln("total time: " + (System.currentTimeMillis() - start_time) + "ms");
    }
}
