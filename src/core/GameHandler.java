package src.core;

import java.util.Scanner;

public class GameHandler {

    public static void humanBotCLI() {
        Scanner scanner = new Scanner(System.in);

        int[][] scoria_move = {};

        while (!Game.isGameOver()) {
            if (Game.getTurn()) {
                String uci_move = scanner.nextLine();
                int[][] move = Interface.uciToMove(uci_move);
                MoveHandler.moveState(Game.board, move[0], move[1]);
                Interface.printCLI();
            } else {
                scoria_move = Game.getScoriaMove(Game.getTurn());
                MoveHandler.moveState(Game.board, scoria_move[1], scoria_move[2]);
                Interface.printCLI();
                System.out.println(Interface.moveToUci(scoria_move[1], scoria_move[2]));
            }

            Game.notTurn();
        }
        scanner.close();

        Interface.printEndGame();
    }

    public static void humanBotUCI() {

        Scanner scanner = new Scanner(System.in);

        while (!Game.isGameOver()) {
            if (Game.getTurn()) {
                String uci_move = scanner.nextLine();

                while (uci_move.equals("d")) {
                    Interface.printCLI();
                    uci_move = scanner.nextLine();
                }

                int[][] move = Interface.uciToMove(uci_move);
                MoveHandler.moveState(Game.board, move[0], move[1]);
            } else {
                int[][] scoria_move = Game.getScoriaMove(Game.getTurn());
                System.out.println(Interface.moveToUci(scoria_move[1], scoria_move[2]));
                MoveHandler.moveState(Game.board, scoria_move[1], scoria_move[2]);
            }

            Game.notTurn();
        }
        scanner.close();

        Interface.printEndGame();

    }

    public static void botBotCLI() {

        while (!Game.isGameOver()) {
            int[][] scoria_move = Game.getScoriaMove(Game.getTurn());
            MoveHandler.moveState(Game.board, scoria_move[1], scoria_move[2]);
            Interface.printCLI();
            Game.notTurn();
        }

        Interface.printEndGame();

    }

    public static void botBotUCI() {

        while (!Game.isGameOver()) {
            int[][] scoria_move = Game.getScoriaMove(Game.getTurn());
            MoveHandler.moveState(Game.board, scoria_move[1], scoria_move[2]);
            System.out.println(Interface.moveToUci(scoria_move[1], scoria_move[2]));
            Game.notTurn();
        }

        Interface.printEndGame();

    }
}
