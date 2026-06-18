package src.game;

import src.user.Uci;
import src.utils.MoveList;

public class Perft {

    // FIXME: Needs to be fixed to not generate illegal moves in the first place
    private static long perft(int side, int depth) {
        if (depth == 0) return 1;
        
        long nodes = 0L;
        MoveList move_list = MoveGenerator.generateAllMoves(side);

        // if (depth == 1) return move_list.size();

        for (int i = 0; i < move_list.size(); i++) {
            int move = move_list.getMove(i);

            MoveHandler.doMove(move);

            // Verify Pseudo Legality
            if (MoveGenerator.isKingInCheck(side)) {
                MoveHandler.undoMove();
                continue;
            }

            nodes += perft(side ^ 1, depth - 1);
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

            long child_nodes = perft(side ^ 1, depth - 1);
            MoveHandler.undoMove();

            System.out.println(Uci.moveToUci(move) + ": " + child_nodes);

            nodes += child_nodes;
        }

        System.out.println("total nodes: " + nodes);
        System.out.println("total time: " + (System.currentTimeMillis() - start_time) + "ms");
    }
}
