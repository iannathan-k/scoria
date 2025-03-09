package src.core;

import java.util.ArrayList;
import java.util.Scanner;

import src.pieces.PieceData;
import src.pieces.PieceHandler;
import src.scoria.Scoria;
import src.scoria.Zobrist;

public class GameHandler {

    public static void humanBotCLI() {
        Scanner scanner = new Scanner(System.in);

        int[] scoria_move = new int[2];

        while (!Game.isGameOver()) {
            long hash = Zobrist.manualHash(Game.board, Game.getTurn());
            if (Game.isHumanTurn()) {
                String uci_move = scanner.nextLine();
                int move = Interface.uciToMove(uci_move);
                MoveHandler.moveState(Game.board, move, hash);
                Interface.printCLI();
            } else {
                scoria_move = Game.getScoriaMove(Game.getTurn());
                MoveHandler.moveState(Game.board, scoria_move[1], hash);
                Interface.printCLI();
                System.out.println("move: " + Interface.moveToUci(scoria_move[1]));
            }

            Game.notTurn();
        }
        scanner.close();

        Interface.printEndGame();
    }

    public static void humanBotUCI() {

        Scanner scanner = new Scanner(System.in);

        while (!Game.isGameOver()) {
            long hash = Zobrist.manualHash(Game.board, Game.getTurn());
            if (Game.isHumanTurn()) {
                String uci_move = scanner.nextLine();

                while (uci_move.equals("d")) {
                    Interface.printCLI();
                    uci_move = scanner.nextLine();
                }

                int move = Interface.uciToMove(uci_move);
                MoveHandler.moveState(Game.board, move, hash);
            } else {
                int[] scoria_move = Game.getScoriaMove(Game.getTurn());
                System.out.println(Interface.moveToUci(scoria_move[1]));
                MoveHandler.moveState(Game.board, scoria_move[1], hash);
            }

            Game.notTurn();
        }
        scanner.close();

        Interface.printEndGame();

    }

    public static void botBotCLI() {

        while (!Game.isGameOver()) {
            long hash = Zobrist.manualHash(Game.board, Game.getTurn());
            int[] scoria_move = Game.getScoriaMove(Game.getTurn());
            MoveHandler.moveState(Game.board, scoria_move[1], hash);
            Interface.printCLI();
            Game.notTurn();
        }

        Interface.printEndGame();

    }

    public static void botBotUCI() {

        while (!Game.isGameOver()) {
            long hash = Zobrist.manualHash(Game.board, Game.getTurn());
            int[] scoria_move = Game.getScoriaMove(Game.getTurn());
            MoveHandler.moveState(Game.board, scoria_move[1], hash);
            System.out.println(Interface.moveToUci(scoria_move[1]));
            Game.notTurn();
        }

        Interface.printEndGame();

    }

    public static void perft(int depth) {
        int color = Game.getTurn() ? PieceData.WHITE : PieceData.BLACK;
        ArrayList<Integer> first_moves = PieceHandler.getAllMoves(Game.board, color);

        int total_nodes = 0;

        long start = System.nanoTime();

        for (int move : first_moves) {
            byte captured = MoveHandler.moveState(Game.board, move, -1);
            int move_count = Scoria.perftCount(Game.board, depth - 1, !Game.getTurn());
            MoveHandler.undoState(Game.board, move, captured, -1);

            total_nodes += move_count;
            System.out.println(Interface.moveToUci(move) + ": " + move_count);
        }

        long run_time = (System.nanoTime() - start) / 1_000_000;

        System.out.println("total nodes: " + total_nodes);
        System.out.println("total time: " + run_time + "ms");
    }

    public static void eval(int depth) {
        int color = Game.getTurn() ? PieceData.WHITE : PieceData.BLACK;
        ArrayList<Integer> first_moves = PieceHandler.getAllMoves(Game.board, color);

        int best_eval = (Game.getTurn()) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        String best_move = "";
        Scoria.setCancelMode(false);
        long start = System.nanoTime();

        for (int move : first_moves) {
            long hash = Zobrist.manualHash(Game.board, Game.getTurn());
            byte captured = MoveHandler.moveState(Game.board, move, hash);
            int eval = Scoria.minimax(Game.board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !Game.getTurn())[0];
            MoveHandler.undoState(Game.board, move, captured, hash);
            System.out.println(Interface.moveToUci(move) + ": " + eval);
        
            if (Game.getTurn() && eval > best_eval) {
                best_eval = eval;
                best_move = Interface.moveToUci(move);
            } else if (!Game.getTurn() && eval < best_eval) {
                best_eval = eval;
                best_move = Interface.moveToUci(move);
            }
        }

        long run_time = (System.nanoTime() - start) / 1_000_000;

        Scoria.setCancelMode(true);
        System.out.println("best move: " + best_move +  ", " + best_eval);
        System.out.println("total time: " + run_time + "ms");
    }
}
