package src.core;

import java.util.ArrayList;
import java.util.Scanner;

import src.pieces.PieceData;
import src.pieces.PieceHandler;
import src.scoria.Scoria;
import src.scoria.Zobrist;

public class GameHandler {

    public static void humanBotCLI() {
        int[] scoria_move = new int[2];
        Scanner scanner = new Scanner(System.in);

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

        long total_nodes = 0;

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
        byte[] board = Game.board;
        boolean turn = Game.getTurn();
        int color = turn ? PieceData.WHITE : PieceData.BLACK;
        ArrayList<Integer> first_moves = PieceHandler.getAllMoves(board, color);

        int best_eval = turn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        String best_move = "";
        Scoria.setCancelMode(false);
        long start = System.nanoTime();

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        long hash = Zobrist.manualHash(board, turn);
        for (int move : first_moves) {
            byte captured = MoveHandler.moveState(board, move, hash);
            int eval = Scoria.minimax(board, depth - 1, alpha, beta, !turn)[0];
            MoveHandler.undoState(board, move, captured, hash);
            System.out.println(Interface.moveToUci(move) + ": " + eval);
        
            if (turn && eval > best_eval) {
                best_eval = eval;
                best_move = Interface.moveToUci(move);
                alpha = Math.max(eval, alpha);
            } else if (!turn && eval < best_eval) {
                best_eval = eval;
                best_move = Interface.moveToUci(move);
                beta = Math.min(eval, beta);
            }
        }

        long run_time = (System.nanoTime() - start) / 1_000_000;

        Scoria.setCancelMode(true);
        System.out.println("best move: " + best_move +  ", " + best_eval);
        System.out.println("total time: " + run_time + "ms");
    }
}
