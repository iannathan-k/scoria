package src.core;

import src.pieces.PieceData;
import src.pieces.PieceHandler;
import src.scoria.Evaluator;
import src.utils.MoveList;

public class GameHandler {

	public static int perftCount(byte[] board, int depth, int turn) {
    	if (depth == 0) return 1;

    	int color = (turn == 1) ? PieceData.WHITE : PieceData.BLACK;
    	MoveList possible_moves = PieceHandler.getAllMoves(board, color);

    	int node_count = 0;

    	for (int i = 0; i < possible_moves.size(); i++) {
			int move = possible_moves.get(i);
        	byte captured = MoveHandler.moveState(board, move, -1);
        	node_count += perftCount(board, depth - 1, -turn);
        	MoveHandler.undoState(board, move, captured, -1);
    	}
    	return node_count;
	}

	public static void perft(int depth) {
    	int color = (Game.turn == 1) ? PieceData.WHITE : PieceData.BLACK;
    	MoveList first_moves = PieceHandler.getAllMoves(Game.board, color);

    	long total_nodes = 0;

    	long start = System.currentTimeMillis();

    	for (int i = 0; i < first_moves.size(); i++) {
			int move = first_moves.get(i);
        	byte captured = MoveHandler.moveState(Game.board, move, -1);
        	int move_count = perftCount(Game.board, depth - 1, -Game.turn);
        	MoveHandler.undoState(Game.board, move, captured, -1);

        	total_nodes += move_count;
        	DebugLogger.logOut(Interface.moveToUci(move) + ": " + move_count);
    	}

    	DebugLogger.logOut("total nodes: " + total_nodes);
    	DebugLogger.logOut("total time: " + (System.currentTimeMillis() - start) + "ms");
	}

	public static void eval() {
		int total_eval = 0;
		for (int i = 0; i < 8; i++) {
			DebugLogger.logOut("+-------+-------+-------+-------+-------+-------+-------+-------+");

			String piece_line = "|   ";
			String eval_line = "| ";
			for (int j = 0; j < 8; j++) {
				int pos = i << 3 | j;
				int piece = Game.board[i << 3 | j];

				if (piece == PieceData.EMPTY) {
					piece_line += "    |   ";
					eval_line += "      | ";
					continue;
				}

				piece_line += Interface.getPieceCharacter(piece) + "   |   ";

				int eval = Evaluator.pieceEval(piece, pos);

				String eval_string = Integer.toString(eval);
				if (eval > 0) eval_string = "+" + eval_string;

				int length = eval_string.length();
				int left_padding = (5 - length) / 2;
				int right_padding = (length % 2 == 0) ? left_padding + 1 : left_padding;
				
				eval_line += " ".repeat(left_padding) + eval_string + " ".repeat(right_padding);
				eval_line += " | ";

				total_eval += eval;
			}

			DebugLogger.logOut(piece_line);
			DebugLogger.logOut(eval_line);
		}

		DebugLogger.logOut("+-------+-------+-------+-------+-------+-------+-------+-------+");
		DebugLogger.logOut("total eval: " + total_eval);
	}
}