package src.core;

import src.scoria.Evaluator;
import src.scoria.Scoria;
import src.scoria.Zobrist;

public class Command {

    private static final String starting_position = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private static final String help_normal_string = 
        """
        usage: <command> {argument}

        scoria settings
            uci             Toggle Uci mode
            pos {fen}       Setup game by fen string
            think {time}    Set max thinkking time
            side {color}    Set human side by color
            play {mode}     Play game by mode

        debugging tools
            perft {depth}   Run a perft by depth
            eval {depth}    Run an evaluation by depth
            version         Display current version

        miscellaneous
            d               Display the board
            version         Display current version
            exit            Exit program
            help            Displays this text
        """.strip();

    private static final String help_uci_string = 
        """
        usage: <command> {argument1} {argument2}

        uci settings
            uci                          Toggle UCI Settings
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
        id name Scoria_v3.3.14
        id author iannathan-k (Ian Nathan Kusmiantoro)

        option name Max_Think type long default 1000
        option name Max_Depth type int default 256
        uciok
        """.strip();

    private static boolean uci_mode = false;

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
                move_args = args[2].substring(6).split("\\s+");
            }
        }
    
        for (String uci_move : move_args) {
            long hash = Zobrist.manualHash(Game.board, Game.getTurn());
            int bot_move = Interface.uciToMove(uci_move);
            MoveHandler.moveState(Game.board, bot_move, hash);
            Game.notTurn();
        }
    }

    private static void goCommand(String[] args) {
        long max_time = Integer.MAX_VALUE;
        int max_depth = Integer.MAX_VALUE;

        if (args.length > 1) {
            switch (args[1]) {
                case "movetime" -> max_time = Long.parseLong(args[2]);
                case "depth" -> max_depth = Integer.parseInt(args[2]);
    
                case "perft" -> {
                    GameHandler.perft(Integer.parseInt(args[2]));
                    return;
                }

                case "eval" -> {
                    GameHandler.eval(Integer.parseInt(args[2]));
                    return;
                }
    
                default -> {
                    max_time = Game.MAX_TIME;
                    max_depth = Game.MAX_DEPTH;
                }
            }
        } else {
            max_time = Game.MAX_TIME;
            max_depth = Game.MAX_DEPTH;
        }

        int move = Scoria.iterativeDeepener(Game.board, Game.getTurn(), max_depth, max_time)[1];
        System.out.println("bestmove " + Interface.moveToUci(move));

    }

    private static void optionCommand(String[] args) {
        String[] sub_args = args[2].split("\\s+");
        switch (sub_args[0]) {
            case "Max_Think" -> Game.MAX_TIME = Long.parseLong(sub_args[1]);
            case "Max_Depth" -> Game.MAX_DEPTH = Integer.parseInt(sub_args[1]);
        }
    }

    public static void parseUniversalCommand(String command) {
        String[] args = command.split("\\s" , 3);
        switch (args[0]) {
            case "uci" -> System.out.println(uci_string);
            case "ucinewgame" -> Evaluator.clearPositionTable();
            case "isready" -> System.out.println("readyok");
            case "position" -> positionCommand(args);
            case "go" -> goCommand(args);
            case "d" -> Interface.printBoard(Game.board);
            case "setoption" -> optionCommand(args);
            case "help" -> System.out.println(help_uci_string);
            default -> System.out.println("unknown command: " + command);
        }
    }

    public static void parseCommand(String command) {
        String[] command_stream = command.split("\\s", 2);
        boolean has_modifier = (command_stream.length == 2) ? true : false;
        String field = command_stream[0];
        String modifier = has_modifier ? command_stream[1] : null;

        if (command_stream[0].equals("uci")) {
            uci_mode = !uci_mode;
        }
        if (uci_mode) {
            parseUniversalCommand(command);
            return;
        }

        switch (field) {
            case "pos" -> Game.initGame(has_modifier ? modifier : starting_position);
            case "d" -> Interface.printBoard(Game.board);
            case "perft" -> GameHandler.perft(has_modifier ? Integer.parseInt(modifier) : 5);
            case "eval" -> GameHandler.eval(has_modifier ? Integer.parseInt(modifier) : 5);
            case "version" -> System.out.println(uci_string.split("\\s+")[2]);
            case "help" -> System.out.println(help_normal_string);
            case "uci" -> System.out.print("");

            case "think" -> {
                if (has_modifier) {
                    Game.MAX_TIME = Long.parseLong(modifier);
                } else {
                    System.out.println(Game.MAX_TIME + "ms");
                }
            }

            case "play" -> {
                switch (modifier) {
                    case "1" -> GameHandler.humanBotCLI();
                    case "2" -> GameHandler.humanBotUCI();
                    case "3" -> GameHandler.botBotCLI();
                    case "4" -> GameHandler.botBotUCI();
                };
            }

            case "side" -> {
                if (!has_modifier) {
                    System.out.println(Game.getPlayerColor());
                } else {
                    Game.setPlayerSide(modifier == "white");
                }
            }
                
            default -> System.out.println("unknown command: " + field);
        }
    }
}   
