package src.core;

import java.util.ArrayList;
import java.util.Scanner;

import src.pieces.Piece;
import src.pieces.PieceHandler;
import src.pieces.enums.PieceColor;
import src.scoria.Scoria;
import src.scoria.Zobrist;

public class GameHandler {

    public static void humanBotCLI() {
        Scanner scanner = new Scanner(System.in);

        int[][] scoria_move = {};

        while (!Game.isGameOver()) {
            long hash = Zobrist.manualHash(Game.board, Game.getTurn());
            if (Game.isHumanTurn()) {
                String uci_move = scanner.nextLine();
                int[][] move = Interface.uciToMove(uci_move);
                MoveHandler.deepMoveState(Game.board, move[0], move[1], hash);
                Interface.printCLI();
            } else {
                scoria_move = Game.getScoriaMove(Game.getTurn());
                MoveHandler.deepMoveState(Game.board, scoria_move[1], scoria_move[2], hash);
                Interface.printCLI();
                System.out.println("move: " + Interface.moveToUci(scoria_move[1], scoria_move[2]));
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

                int[][] move = Interface.uciToMove(uci_move);
                MoveHandler.deepMoveState(Game.board, move[0], move[1], hash);
            } else {
                int[][] scoria_move = Game.getScoriaMove(Game.getTurn());
                System.out.println(Interface.moveToUci(scoria_move[1], scoria_move[2]));
                MoveHandler.deepMoveState(Game.board, scoria_move[1], scoria_move[2], hash);
            }

            Game.notTurn();
        }
        scanner.close();

        Interface.printEndGame();

    }

    public static void botBotCLI() {

        while (!Game.isGameOver()) {
            long hash = Zobrist.manualHash(Game.board, Game.getTurn());
            int[][] scoria_move = Game.getScoriaMove(Game.getTurn());
            MoveHandler.deepMoveState(Game.board, scoria_move[1], scoria_move[2], hash);
            Interface.printCLI();
            Game.notTurn();
        }

        Interface.printEndGame();

    }

    public static void botBotUCI() {

        while (!Game.isGameOver()) {
            long hash = Zobrist.manualHash(Game.board, Game.getTurn());
            int[][] scoria_move = Game.getScoriaMove(Game.getTurn());
            MoveHandler.deepMoveState(Game.board, scoria_move[1], scoria_move[2], hash);
            System.out.println(Interface.moveToUci(scoria_move[1], scoria_move[2]));
            Game.notTurn();
        }

        Interface.printEndGame();

    }

    public static void perft(int depth) {
        PieceColor color = Game.getTurn() ? PieceColor.WHITE : PieceColor.BLACK;
        ArrayList<int[][]> first_moves = PieceHandler.getAllMoves(Game.board, color);

        int total_nodes = 0;

        long start = System.nanoTime();

        for (int[][] move : first_moves) {
            Piece[] board_info = MoveHandler.pseudoMoveState(Game.board, move[0], move[1]);
            int move_count = Scoria.perftCount(Game.board, depth - 1, !Game.getTurn());
            MoveHandler.pseudoUndoState(Game.board, move[0], move[1], board_info);

            total_nodes += move_count;
            System.out.println(Interface.moveToUci(move[0], move[1]) + ": " + move_count);
        }

        long run_time = (System.nanoTime() - start) / 1_000_000;

        System.out.println("total nodes: " + total_nodes);
        System.out.println("total time: " + run_time + "ms");
    }

    public static void eval(int depth) {
        PieceColor color = Game.getTurn() ? PieceColor.WHITE : PieceColor.BLACK;
        ArrayList<int[][]> first_moves = PieceHandler.getAllMoves(Game.board, color);

        int best_eval = Integer.MIN_VALUE;
        String best_move = "";
        Scoria.setCancelMode(false);
        long start = System.nanoTime();

        for (int[][] move : first_moves) {
            long hash = Zobrist.manualHash(Game.board, Game.getTurn());
            Piece[] board_info = MoveHandler.deepMoveState(Game.board, move[0], move[1], hash);
            int eval = Scoria.minimax(Game.board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !Game.getTurn())[0][0];
            MoveHandler.deepUndoState(Game.board, move[0], move[1], board_info, hash);
            System.out.println(Interface.moveToUci(move[0], move[1]) + ": " + eval);
        
            if (eval > best_eval) {
                best_eval = eval;
                best_move = Interface.moveToUci(move[0], move[1]);
            }
        }

        long run_time = (System.nanoTime() - start) / 1_000_000;

        Scoria.setCancelMode(true);
        System.out.println("best move: " + best_move +  ", " + best_eval);
        System.out.println("total time: " + run_time + "ms");
    }
}
