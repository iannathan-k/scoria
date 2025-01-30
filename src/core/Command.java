package src.core;

public class Command {

    private static String mode;

    public static void parseCommand(String command) {
        String[] commandStream = command.split("\\s", 2);
        boolean auto = (commandStream.length == 1) ? true : false;

        switch (commandStream[0]) {
            case "pos":
                if (auto) {
                    Game.initGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
                } else {
                    Game.initGame(commandStream[1]);
                }
                break;

            case "d":
                Interface.printBoard(Game.board);
                break;
            case "play":
                if (auto) {
                    GameHandler.botBotCLI();
                    break;
                }

                switch (commandStream[1]) {
                    case "1" -> GameHandler.humanBotCLI();
                    case "2" -> GameHandler.humanBotUCI();
                    case "3" -> GameHandler.botBotCLI();
                    case "4" -> GameHandler.botBotUCI();
                };
                break;
            default:
                System.out.println("Unknown Command: " + commandStream[0]);
        }
    }
}
