package src.core;

import java.util.Arrays;

import src.scoria.Evaluator;
import src.scoria.Scoria;
import src.scoria.Transposition;
import src.scoria.Zobrist;

public class Command {

    private static final String starting_position = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private static final String help_uci_string = 
        """
        usage: <command> {argument1} {argument2}

        uci settings
            uci                          Toggle Uci Settings
            ucinewgame                   Create a new game
            isready                      Wait for the engine

        game tools
            position {fen} {moves}       Setup position by fen and moves
            go {mode} {arg}              Run a go command by mode

        miscellaneous
            d                            Display the board
            help                         Display this screen
            quit                         Quit the program
        """.strip();

    private static final String uci_string = 
        """
        id name Scoria_v3.5.32
        id author iannathan-k (Ian Nathan Kusmiantoro)

        option name Debug Log File type string default <empty>
        option name Move Overhead type spin default 0 min 0 max 100
        option name Clear Hash type button
        uciok
        """.strip();

    public static final String scoria_logo =
    """
    ========================================
           _____                 _      
          / ___/_________  _____(_)___ _
          \\__ \\/ ___/ __ \\/ ___/ / __ `/
         ___/ / /__/ /_/ / /  / / /_/ / 
        /____/\\___/\\____/_/  /_/\\__,_/  
        
        Ian Nathan Kusmiantoro
        Version 3.5.32
    ========================================
    """;

    private static int latency = 0;

    private static void positionCommand(String[] args) {
        String[] move_args = new String[0];
    
        if (args[1].equals("fen")) {
            String[] sub_args = args[2].split(" moves ");
    
            Game.initGame(sub_args[0]);
    
            if (sub_args.length > 1) {
                move_args = sub_args[1].split("\\s+");
            }
        } else if (args[1].equals("startpos")) {
            Game.initGame(starting_position);
    
            if (args.length > 2) {
                String[] sub_args = args[2].split("\\s+");

                if (sub_args.length <= 1) return;

                move_args = Arrays.copyOfRange(sub_args, 1, sub_args.length);
            }
        }
    
        Game.move_number = 0;
        for (String uci_move : move_args) {
            long hash = Zobrist.manualHash(Game.board, Game.turn);
            int bot_move = Interface.uciToMove(uci_move);
            MoveHandler.moveState(Game.board, bot_move, hash);
            Game.turn = -Game.turn;
            Game.move_number++;
        }
    }

    private static long calculateTime(long remaining_time) {
        int remaining_moves = Math.max(60 - (Game.move_number >> 1), 10);
        return Math.max(150, remaining_time / remaining_moves);
    }

    private static void goCommand(String command) {
        String[] args = command.split("\\s+");
        long max_time = Integer.MAX_VALUE;
        int max_depth = 245;

        for (int i = 1; i < args.length; i += 2) {
            switch (args[i]) {
                case "movetime" -> max_time = Long.parseLong(args[i + 1]);
                case "depth" -> max_depth = Integer.parseInt(args[i + 1]);
                case "btime" -> max_time = (Game.turn == -1) ? calculateTime(Integer.parseInt(args[i + 1])) : max_time;
                case "wtime" -> max_time = (Game.turn == 1) ? calculateTime(Integer.parseInt(args[i + 1])) : max_time;
                case "infinite" -> new ListenerThread().start();

                case "perft" -> {
                    GameHandler.perft(Integer.parseInt(args[i + 1]));
                    return;
                }
            }
        }

        max_time -= latency;

        int move = Scoria.iterativeDeepener(Game.board, Game.turn, max_depth, max_time)[0];
        DebugLogger.logOut("bestmove " + Interface.moveToUci(move));
    }

    private static void setOptionCommand(String arg) {
        String[] sub_args = arg.split(" value ");
        switch (sub_args[0]) {
            case "Debug Log File" -> DebugLogger.debug_path = (sub_args.length > 1) ? sub_args[1] : "";
            case "Move Overhead" -> latency = Integer.parseInt(sub_args[1]);
            case "Clear Hash" -> Transposition.clearHashTable();
            default -> DebugLogger.logOut("unrecognized option: " + sub_args[0]);
        }
    }

    private static void clearGameInfo() {
        Evaluator.clearPositionTable();
        Scoria.clearHistoryTable();
        Game.move_number = 0;
        Game.node_count = 0;
    }

    public static void parseCommand(String command) {
        String[] args = command.split("\\s" , 3);
        switch (args[0]) {
            case "uci" -> DebugLogger.logOut(uci_string);
            case "ucinewgame" -> clearGameInfo();
            case "isready" -> DebugLogger.logOut("readyok");
            case "position" -> positionCommand(args);
            case "go" -> goCommand(command);
            case "d" -> Interface.printBoard(Game.board);
            case "help" -> DebugLogger.logOut(help_uci_string);
            case "eval" -> GameHandler.eval();
            case "setoption" -> setOptionCommand(args[2]);
            default -> DebugLogger.logOut("unknown command: " + command);
        }
    }
}   
