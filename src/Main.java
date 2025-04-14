package src;

import java.util.Scanner;

import src.core.Command;
import src.core.Game;
import src.core.ListenerThread;
import src.pieces.PreComputer;

public class Main {

    public static void main(String args[]) {
        
        System.out.println(Command.scoria_logo);
        Game.initGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
        PreComputer.initializePremoves();

        Scanner scanner = ListenerThread.scanner;

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            if (command.equals("quit")) break;

            Command.parseCommand(command);
        }
        scanner.close();
    }

}