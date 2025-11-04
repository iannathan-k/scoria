package src.user;

import src.engine.Search;
import src.game.BitBoard;
import src.game.MoveHandler;
import src.game.Perft;
import src.game.Zobrist;

public class Command {

    private static void positionCommand(String args[]) {
        if (args[1].equals("startpos")) {
            BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            for (int i = 3; i < args.length; i++) {
                int move = Uci.uciToMove(args[i]);
                MoveHandler.doMove(move);
                BitBoard.moving_side ^= 1;
            }
        } else {
            String fen = "";
            int i = 2;

            // Reconstruct fen string
            while (i < args.length && !args[i].equals("moves")) {
                fen += args[i] + " ";
                i++;
            }

            BitBoard.initBoardByFen(fen.trim());

            for (int j = i + 1; j < args.length; j++) {
                int move = Uci.uciToMove(args[j]);
                MoveHandler.doMove(move);
                BitBoard.moving_side ^= 1;
            }
        }

        Zobrist.manualZobristHash();
    }

    // FIXME: Change to looping condition
    // FIXME: Add TimeMan
    private static void goCommand(String args[]) {
        switch(args[1]) {
            case "perft" -> Perft.runPerftTest(BitBoard.moving_side, Integer.parseInt(args[2]));
            case "depth" -> Search.iterativeDeepener(Integer.parseInt(args[2]), Integer.MAX_VALUE);
            case "movetime" -> Search.iterativeDeepener(Integer.MAX_VALUE, Integer.parseInt(args[2]));
        }
    }

    public static void parseCommand(String command) {
        String[] args = command.split("\\s");

        switch (args[0]) {
            case "go" -> goCommand(args);
            case "position" -> positionCommand(args);
            case "d" -> Uci.printBoard();
        }
    }
}
