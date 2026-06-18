package src.user;

import src.engine.Evaluator;
import src.engine.Search;
import src.game.BitBoard;
import src.game.MoveHandler;
import src.game.Perft;
import src.game.Zobrist;
import src.utils.GameStack;

public class Command {

    private static void positionCommand(String args[]) {
        GameStack.clear(); // FIXME: Move this somewhere else

        if (args[1].equals("startpos")) {
            BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            Evaluator.manualEvaluation();
            Zobrist.manualZobristHash();

            for (int i = 3; i < args.length; i++) {
                int move = Uci.uciToMove(args[i]);
                MoveHandler.doMove(move);
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

            Evaluator.manualEvaluation();
            Zobrist.manualZobristHash();

            for (int j = i + 1; j < args.length; j++) {
                int move = Uci.uciToMove(args[j]);
                MoveHandler.doMove(move);
            }
        }
    }

    // FIXME: Add Timer.java
    private static void goCommand(String args[]) {
        int wtime = 0;
        int btime = 0;
        int winc = 0;
        int binc = 0;

        for (int i = 1; i < args.length; i += 2) {
            int num = args.length > 2 ? Integer.parseInt(args[i + 1]) : -1;

            switch (args[i]) {
                case "perft" -> Perft.runPerftTest(BitBoard.moving_side, num);
                case "depth" -> Search.iterativeDeepener(num, Search.MAX_TIME);
                case "movetime" -> Search.iterativeDeepener(Search.MAX_DEPTH, num);
                case "wtime" -> wtime = num;
                case "btime" -> btime = num;
                case "winc" -> winc = num;
                case "binc" -> binc = num;
                default -> Search.iterativeDeepener(Search.MAX_DEPTH, Search.MAX_TIME);
            }
        }

        if (wtime + btime + winc + binc != 0) {
            int time = (BitBoard.moving_side == BitBoard.WHITE)
                ? wtime / 40 + winc / 2
                : btime / 40 + binc / 2;

            time = Math.max(time, 20);

            Search.iterativeDeepener(Search.MAX_DEPTH, time);
        }
    }

    public static void parseCommand(String command) {
        String[] args = command.split("\\s");

        switch (args[0]) {
            case "go" -> goCommand(args);
            case "position" -> positionCommand(args);
            case "d" -> Uci.printBoard();
            case "uci" -> System.out.println("uciok");
            case "isready" -> System.out.println("readyok");
        }
    }
}
