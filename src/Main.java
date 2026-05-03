package src;

import java.util.Scanner;

import src.user.Command;
import src.game.BitBoard;
import src.game.Precomputer;
import src.game.Zobrist;

public class Main {
    public static void main(String[] args) {
        BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"); 
        Precomputer.initAllMoveTables();
        Zobrist.initZobristTable();

        System.out.println("Scoria v4.3.5");

        Scanner scanner = new Scanner(System.in);

        String input_command = "";
        while (!input_command.equals("quit")) {
            input_command = scanner.nextLine();
            Command.parseCommand(input_command);
        }

        scanner.close();
    }
}