package src;

import src.user.Command;
import src.utils.Logger;
import src.engine.Search;
import src.game.BitBoard;
import src.game.Precomputer;
import src.game.Zobrist;

public class Main {
    public static void main(String[] args) {
        BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"); 
        Precomputer.initAllMoveTables();
        Zobrist.initZobristTable();
        Search.initSearchTables();

        Logger.outln("Scoria v4.4.7");
        
        String input_command = "";
        while (!input_command.equals("quit")) {
            input_command = Logger.in();
            Command.parseCommand(input_command);
        }

        Logger.close();
    }
}

