package src.scoria;

import java.util.ArrayList;

import src.core.DebugLogger;
import src.core.Game;
import src.core.Interface;
import src.core.MoveHandler;
import src.pieces.*;

public class Scoria {
	private static final int INITIAL_ALPHA = Integer.MIN_VALUE + 1;
	private static final int INITIAL_BETA  = Integer.MAX_VALUE - 1;

	public static long cancel_time;
	public static int current_depth;
	public static int[] principle_variation;

	private static int[][] history_table = new int[2][16191];

	public static void preventCancel() {
    	cancel_time = Long.MAX_VALUE;
	}

	public static void clearHistoryTable() {
    	history_table = new int[2][16191];
	}

	public static int[] iterativeDeepener(byte[] board, int turn, int MAX_DEPTH, long MAX_TIME) {
    	cancel_time = System.currentTimeMillis() + MAX_TIME;
		current_depth = 0;

    	long start = System.currentTimeMillis();
    	while (System.currentTimeMillis() < cancel_time && current_depth < MAX_DEPTH) {
        	current_depth++;
			int[] move = negascout(board, current_depth, INITIAL_ALPHA, INITIAL_BETA, turn, new int[current_depth + 1]);
       	 
        	if (move[0] != Integer.MIN_VALUE) {
				principle_variation = move;
            	DebugLogger.logOut(
                	"info depth " + current_depth +
                	" score " + move[0] +
                	" nodes " + Game.getNodeCount() +
                	" time " + (System.currentTimeMillis() - start) +
                	" pv " + Interface.getVariationString(move)
            	);
        	}
    	}

    	return principle_variation;
	}

	private static int heuristicScore(byte[] board, int move, int color, int depth) {
    	int origin_pos = (move >> 8) & MoveHandler.POS_MASK;
    	int target_pos = move & MoveHandler.POS_MASK;
    	int piece_type = board[origin_pos] & PieceData.TYPE_MASK;
    	byte captured = board[target_pos];

		// Principle Variation
		if (current_depth - depth > 1) {
			if (principle_variation[depth + 1] == move) return 5000;
		}

    	int score = 3 * Evaluator.posWeight(piece_type, color, target_pos);
    	score -= Evaluator.posWeight(piece_type, color, origin_pos);

    	if (captured != PieceData.EMPTY) {
        	score += 100;
        	score += 3 * Evaluator.piece_points[captured & PieceData.TYPE_MASK];
        	score -= Evaluator.piece_points[piece_type];
    	}
		
    	score += Math.min(history_table[color >> 3][move & 0xFFFF], 100);

    	return score;
	}

	public static int[] negascout(byte[] board, int depth, int alpha, int beta, int turn, int[] variation) {
    	long board_hash = Zobrist.manualHash(board, turn);
    	Transposition.BoardState entry = Transposition.getState(board_hash);
    	if (entry != null && entry.getDepth() >= depth) {
        	if (entry.isExact()) {
            	return entry.getBestMove();
        	}
        	if (entry.isBeta() && entry.getBestMove()[0] >= beta) {
            	return entry.getBestMove();
        	}
        	if (entry.isAlpha() && entry.getBestMove()[0] <= alpha) {
            	return entry.getBestMove();
        	}
    	}

		if (depth == 0 || Evaluator.isGameOver(board, turn, board_hash)) {
			Game.node_count++;
			return new int[] {turn * Evaluator.boardEval(board, turn, board_hash, depth)};
		}

    	int color = (turn == 1) ? PieceData.WHITE : PieceData.BLACK;
    	ArrayList<Integer> possible_moves = PieceHandler.getAllMoves(board, color);
    	possible_moves.sort((move1, move2) -> Integer.compare(
        	heuristicScore(board, move2, color, depth),
        	heuristicScore(board, move1, color, depth)
    	));

		int best_score = Integer.MIN_VALUE;
    	int parent_alpha = alpha;
    	boolean is_first = true;

    	for (int move : possible_moves) {
        	byte captured = MoveHandler.moveState(board, move, board_hash);

			int[] child_variation = variation.clone();

			int eval;
			if (is_first) {
				eval = -negascout(board, depth - 1, -beta, -alpha, -turn, child_variation)[0];
				is_first = false;
			} else {
				eval = -negascout(board, depth - 1, -alpha - 1, -alpha, -turn, child_variation)[0];

				if (eval > alpha && eval < beta) {
					eval = -negascout(board, depth - 1, -beta, -alpha, -turn, child_variation)[0];
				}
			}

        	MoveHandler.undoState(board, move, captured, board_hash);

        	if (System.currentTimeMillis() > cancel_time) {
            	return new int[] {Integer.MIN_VALUE};
        	}

        	if (eval > best_score) {
				best_score = eval;
				System.arraycopy(child_variation, 0, variation, 0, variation.length);
				variation[0] = eval;
            	variation[current_depth - depth + 1] = move;
        	}

        	alpha = Math.max(eval, alpha);
        	if (beta <= alpha) {
            	history_table[color >> 3][move & 0xFFFF] += depth * depth;
            	break;
        	}
    	}

		int node_type = 
        (best_score >= beta) ? Transposition.BETA_NODE :
    	(best_score <= parent_alpha) ? Transposition.ALPHA_NODE :
    	Transposition.EXACT_NODE;

    	Transposition.addState(board_hash, new Transposition.BoardState(depth, variation, node_type));

        return variation;
	}
}