package src.core;

import java.util.ArrayList;
import src.pieces.PieceData;
import src.pieces.PieceHandler;
import src.scoria.Scoria;
import src.scoria.Zobrist;

public class GameHandler {

	public static int perftCount(byte[] board, int depth, boolean turn) {
    	if (depth == 0) return 1;

    	int color = turn ? PieceData.WHITE : PieceData.BLACK;
    	ArrayList<Integer> possible_moves = PieceHandler.getAllMoves(board, color);

    	int node_count = 0;

    	for (int move : possible_moves) {
        	byte captured = MoveHandler.moveState(board, move, -1);
        	node_count += perftCount(board, depth - 1, !turn);
        	MoveHandler.undoState(board, move, captured, -1);
    	}
    	return node_count;
	}

	public static void perft(int depth) {
    	int color = Game.turn ? PieceData.WHITE : PieceData.BLACK;
    	ArrayList<Integer> first_moves = PieceHandler.getAllMoves(Game.board, color);

    	long total_nodes = 0;

    	long start = System.currentTimeMillis();

    	for (int move : first_moves) {
        	byte captured = MoveHandler.moveState(Game.board, move, -1);
        	int move_count = perftCount(Game.board, depth - 1, !Game.turn);
        	MoveHandler.undoState(Game.board, move, captured, -1);

        	total_nodes += move_count;
        	System.out.println(Interface.moveToUci(move) + ": " + move_count);
    	}

    	System.out.println("total nodes: " + total_nodes);
    	System.out.println("total time: " + (System.currentTimeMillis() - start) + "ms");
	}

	public static void eval(int depth) {
    	byte[] board = Game.board;
    	boolean turn = Game.turn;
    	int color = turn ? PieceData.WHITE : PieceData.BLACK;
    	ArrayList<Integer> first_moves = PieceHandler.getAllMoves(board, color);

    	int best_eval = Integer.MIN_VALUE;
    	String best_move = "";
    	Scoria.preventCancel();
    	long start = System.currentTimeMillis();

    	int alpha = Integer.MIN_VALUE + 1;
    	int beta = Integer.MAX_VALUE - 1;
    	int sign = turn ? 1 : -1;

    	long hash = Zobrist.manualHash(board, turn);
    	for (int move : first_moves) {
        	byte captured = MoveHandler.moveState(board, move, hash);
        	int eval = -Scoria.negamax(board, depth - 1, -beta, -alpha, !turn, -sign, false)[0];
        	MoveHandler.undoState(board, move, captured, hash);
        	System.out.println(Interface.moveToUci(move) + ": " + sign * eval);

        	if (eval > best_eval) {
            	best_eval = eval;
            	best_move = Interface.moveToUci(move);
        	}

        	alpha = Math.max(alpha, best_eval);
    	}

    	System.out.println("best move: " + best_move +  ", " + sign * best_eval);
    	System.out.println("total time: " + (System.currentTimeMillis() - start) + "ms");
	}
}