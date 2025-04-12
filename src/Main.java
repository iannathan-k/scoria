package src;

import java.util.Scanner;

import src.core.Command;
import src.core.Game;
import src.pieces.PreComputer;

public class Main {

    public static void main(String args[]) {
        
        System.out.println(Command.scoria_logo);
        Game.initGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
        PreComputer.initializePremoves();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String command = scanner.nextLine();
            if (command.equals("quit")) break;

            Command.parseCommand(command);
        }
        scanner.close();
    }

}