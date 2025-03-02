package src;

import java.util.Scanner;

import src.core.Command;
import src.core.Game;
import src.pieces.PieceData;
import src.pieces.PieceHandler;
import src.scoria.Scoria;

public class Main {

    public static void main(String args[]) {
        
        System.out.println("starting...");
        Game.initGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            if (command.equals("exit")) break;

            Command.parseCommand(command);
        }
        scanner.close();
    }

}