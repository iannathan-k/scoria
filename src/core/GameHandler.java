package src.core;

import java.util.ArrayList;
import src.pieces.PieceData;
import src.pieces.PieceHandler;

public class GameHandler {

	public static int perftCount(byte[] board, int depth, int turn) {
    	if (depth == 0) return 1;

    	int color = (turn == 1) ? PieceData.WHITE : PieceData.BLACK;
    	ArrayList<Integer> possible_moves = PieceHandler.getAllMoves(board, color);

    	int node_count = 0;

    	for (int move : possible_moves) {
        	byte captured = MoveHandler.moveState(board, move, -1);
        	node_count += perftCount(board, depth - 1, -turn);
        	MoveHandler.undoState(board, move, captured, -1);
    	}
    	return node_count;
	}

	public static void perft(int depth) {
    	int color = (Game.turn == 1) ? PieceData.WHITE : PieceData.BLACK;
    	ArrayList<Integer> first_moves = PieceHandler.getAllMoves(Game.board, color);

    	long total_nodes = 0;

    	long start = System.currentTimeMillis();

    	for (int move : first_moves) {
        	byte captured = MoveHandler.moveState(Game.board, move, -1);
        	int move_count = perftCount(Game.board, depth - 1, -Game.turn);
        	MoveHandler.undoState(Game.board, move, captured, -1);

        	total_nodes += move_count;
        	DebugLogger.logOut(Interface.moveToUci(move) + ": " + move_count);
    	}

    	DebugLogger.logOut("total nodes: " + total_nodes);
    	DebugLogger.logOut("total time: " + (System.currentTimeMillis() - start) + "ms");
	}
}