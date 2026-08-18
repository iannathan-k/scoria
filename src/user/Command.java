package src.user;

import src.engine.Evaluator;
import src.engine.Search;
import src.engine.Timer;
import src.engine.Transposition;
import src.game.BitBoard;
import src.game.MoveHandler;
import src.game.Perft;
import src.game.Zobrist;
import src.utils.GameStack;
import src.utils.Logger;

public class Command {
    private static String[] p_args;

    private static void positionCommand(String args[]) {
        Timer.abort();
        GameStack.clear();
        
        if (args[1].equals("startpos")) {
            BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            Evaluator.manualEvaluation();
            Zobrist.manualZobristHash();

            for (int i = 3; i < args.length; i++) {
                int move = Uci.uciToMove(args[i]);
                MoveHandler.doMove(move);

                if (BitBoard.moving_side == BitBoard.WHITE) {
                    BitBoard.fullmoves++;
                }
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

                if (BitBoard.moving_side == BitBoard.WHITE) {
                    BitBoard.fullmoves++;
                }
            }
        }
    }

    private static void goCommand(String args[], boolean start) {
        boolean controlled = false;
        boolean is_ponder = false;
        int wtime = 0;
        int btime = 0;
        int winc = 0;
        int binc = 0;
        int mtg = 50;

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "perft":
                    int depth = Integer.parseInt(args[i + 1]);
                    Perft.runPerftTest(BitBoard.moving_side, depth);
                    return;

                case "depth":
                    depth = Integer.parseInt(args[i + 1]);
                    Timer.initDepthSearch(depth);
                    i++;
                    break;

                case "movetime":
                    int duration = Integer.parseInt(args[i + 1]);
                    Timer.initMovetimeSearch(duration);
                    i++;
                    break;

                case "wtime":
                    controlled = true;
                    wtime = Integer.parseInt(args[i + 1]);
                    i++;
                    break;

                case "btime":
                    controlled = true;
                    btime = Integer.parseInt(args[i + 1]);
                    i++;
                    break;

                case "winc":
                    controlled = true;
                    winc = Integer.parseInt(args[i + 1]);
                    i++;
                    break;

                case "binc":
                    controlled = true;
                    binc = Integer.parseInt(args[i + 1]);
                    i++;
                    break;

                case "movestogo":
                    controlled = true;
                    mtg = Integer.parseInt(args[i + 1]);
                    i++;
                    break;

                case "ponder":
                    is_ponder = true;
                    break;

                case "infinite":
                    // Fall Through

                default:
                    Timer.initInfiniteSearch();
                    break;
            }
        }

        if (args.length == 1) {
            Timer.initInfiniteSearch();
        }

        if (controlled) {
            boolean w_turn = BitBoard.moving_side == BitBoard.WHITE;
            int m_time = w_turn ? wtime : btime;
            int o_time = w_turn ? btime : wtime;
            int m_inc = w_turn ? winc : binc;
            Timer.initControlledSearch(m_time, o_time, m_inc, mtg);
        }

        if (!start) {
            return;
        }

        if (is_ponder) {
            p_args = args;
            Timer.initInfiniteSearch();
        }
        
        new Thread(() -> {
            Search.iterativeDeepener();
        }).start();
    }

    private static void goCommand(String args[]) {
        goCommand(args, true);
    }

    private static void ponderHit() {
        goCommand(p_args, false);
    }

    private static void uciCommand() {
        Logger.outln("id name Scoria_v4.4.9");
        Logger.outln("id author iannathan-k (Ian Kusmiantoro)");
        Logger.outln();
        Logger.outln("option name Debug Log File type string default <empty>");
        Logger.outln("option name Move Overhead type spin default 0 min 0 max 1000");
        Logger.outln("option name Clear Hash type button");
        Logger.outln("option name Ponder type check default false");
        Logger.outln("option name Hash type spin default 32 min 1 max 2048");
        Logger.outln("uciok");
    }

    private static void setoptionCommand(String command) {
        String[] args = command.split("setoption name ")[1].split(" value ");

        switch (args[0].toLowerCase()) {
            case "debug log file":
                Logger.init(args[1]);
                break;

            case "move overhead":
                int overhead = Integer.parseInt(args[1]);
                Timer.setMoveOverhead(overhead);
                break;

            case "clear hash":
                Transposition.clearTranspositionTable();
                break;

            case "ponder":
                Boolean value = Boolean.valueOf(args[1]);
                Timer.setPonder(value);
                break;

            case "hash":
                int mb_size = Integer.parseInt(args[1]);
                Transposition.resizeTranspositionTable(mb_size);
                break;
        }
    }

    public static void newGameCommand() {
        Timer.abort();
        Transposition.clearTranspositionTable();
        Search.clearHeuristics();
        GameStack.clear();
    }

    public static void parseCommand(String command) {
        String[] args = command.split("\\s");

        switch (args[0]) {
            case "go" -> goCommand(args);
            case "position" -> positionCommand(args);
            case "d" -> Uci.printBoard();
            case "uci" -> uciCommand();
            case "isready" -> Logger.outln("readyok");
            case "stop" -> Timer.abort();
            case "setoption" -> setoptionCommand(command);
            case "ucinewgame" -> newGameCommand();
            case "ponderhit" -> ponderHit();
        }
    }
}
