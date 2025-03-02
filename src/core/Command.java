package src.core;

public class Command {

    public static void parseCommand(String command) {
        String[] command_stream = command.split("\\s", 2);
        boolean has_modifier = (command_stream.length == 2) ? true : false;
        String field = command_stream[0];
        String modifier = has_modifier ? command_stream[1] : null;

        switch (field) {
            case "pos" -> Game.initGame(has_modifier ? modifier : "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");

            case "d" -> Interface.printBoard(Game.board);

            case "perft" -> GameHandler.perft(has_modifier ? Integer.parseInt(modifier) : 5);

            case "eval" -> GameHandler.eval(has_modifier ? Integer.parseInt(modifier) : 5);

            case "version" -> System.out.println("Scoria v3.0.3");

            case "think" -> {
                if (has_modifier) {
                    Game.THINK_TIME = Long.parseLong(modifier);
                } else {
                    System.out.println(Game.THINK_TIME + "ms");
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
