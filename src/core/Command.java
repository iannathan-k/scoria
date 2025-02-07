package src.core;

public class Command {

    public static void parseCommand(String command) {
        String[] commandStream = command.split("\\s", 2);
        boolean auto = (commandStream.length == 1) ? true : false;

        switch (commandStream[0]) {
            case "pos":
                if (auto) {
                    Game.initGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
                    break;
                }
                Game.initGame(commandStream[1]);
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

            case "perft":
                if (auto) {
                    GameHandler.perft(5);
                    break;
                }

                GameHandler.perft(Integer.parseInt(commandStream[1]));
                break;

            case "think":
                if (auto) {
                    System.out.println(Game.THINK_TIME + "ms");
                    break;
                }

                Game.THINK_TIME = Long.parseLong(commandStream[1]);
                break;

            case "eval":
                if (auto) {
                    GameHandler.eval(5);
                    break;
                }

                GameHandler.eval(Integer.parseInt(commandStream[1]));
                break;
                
            default:
                System.out.println("unknown command: " + commandStream[0]);
        }
    }
}   
