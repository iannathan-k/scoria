package src;

import java.util.Scanner;

import src.core.Command;
import src.core.Game;
import src.pieces.PieceData;
import src.pieces.PieceHandler;

public class Main {

    public static void main(String args[]) {
        
        System.out.println("starting...");
        Game.initGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");

        // System.out.println(PieceHandler.generateMoves(Game.board, PieceData.KNIGHT, 3));

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            if (command.equals("exit")) break;

            Command.parseCommand(command);
        }
        scanner.close();
    }

}