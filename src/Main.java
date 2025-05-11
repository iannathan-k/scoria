package src;

import java.util.Scanner;

import src.core.Command;
import src.core.DebugLogger;
import src.core.Game;
import src.core.ListenerThread;
import src.pieces.PreComputer;
import src.scoria.Zobrist;

public class Main {

    public static void main(String args[]) {
        
        DebugLogger.logOut(Command.scoria_logo);
        Game.initGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        PreComputer.initializePremoves();
        Zobrist.initTable();

        Scanner scanner = ListenerThread.scanner;

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            DebugLogger.logIn(command);
            if (command.equals("quit")) break;

            Command.parseCommand(command);
        }
        scanner.close();
    }

}