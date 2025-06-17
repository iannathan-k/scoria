package src.scoria;

import java.util.Arrays;

import src.core.DebugLogger;
import src.core.Game;
import src.core.Interface;
import src.core.MoveHandler;
import src.pieces.*;
import src.utils.*;

public class Scoria {
	private static final int INFINITY = Integer.MAX_VALUE - 2000;

	public static long cancel_time;
	public static int current_depth;
	public static int[] principle_variation;

	private static int[][] history_table = new int[2][4095];
	// private static int[][] killer_table = new int[64][2];

	private static int[] razor_margin = {0, 200, 1000};
	private static int[] futility_margin = {0, 150, 750};

	public static void preventCancel() {
    	cancel_time = Long.MAX_VALUE;
	}

	public static void clearHistoryTable() {
    	history_table = new int[2][4095];
	}

	public static int[] iterativeDeepener(byte[] board, int turn, int MAX_DEPTH, long MAX_TIME) {
    	cancel_time = System.currentTimeMillis() + MAX_TIME;
		current_depth = 0;

		int start_alpha = -INFINITY;
		int start_beta = INFINITY;

    	long start = System.currentTimeMillis();
    	while (System.currentTimeMillis() < cancel_time && current_depth < MAX_DEPTH) {
        	current_depth++;
			int[] move = new int[current_depth];
			int eval = negascout(board, current_depth, start_alpha, start_beta, turn, move, true);

			if (eval == Integer.MIN_VALUE) continue;

			if (eval <= start_alpha || eval >= start_beta) {
				start_alpha = -INFINITY;
				start_beta = INFINITY;
				current_depth--;
				continue;
			}

			start_alpha = eval - 70;
			start_beta = eval + 70;
			principle_variation = move;

			if (Math.abs(eval) > 9000) {
				int distance = (10001 - Math.abs(eval)) >> 1;
				if (eval < 0) distance = -distance;

				DebugLogger.logOut(
                	"info depth " + current_depth +
                	" score mate " + distance +
                	" nodes " + Game.node_count +
                	" time " + (System.currentTimeMillis() - start) +
                	" pv " + Interface.getVariationString(move)
            	);
			} else {
				DebugLogger.logOut(
					"info depth " + current_depth +
					" score cp " + eval +
					" nodes " + Game.node_count +
					" time " + (System.currentTimeMillis() - start) +
					" pv " + Interface.getVariationString(move)
				);
			}
    	}

		Game.node_count = 0;
    	return principle_variation;
	}

	private static int heuristicScore(byte[] board, int move, int color, int depth, int hash_move) {
		if (hash_move == move) return 10000;

    	int origin_pos = (move >> 6) & MoveHandler.POS_MASK;
    	int target_pos = move & MoveHandler.POS_MASK;
    	int piece_type = board[origin_pos] & PieceData.TYPE_MASK;
    	byte captured = board[target_pos];
		
    	int score = 3 * Evaluator.posWeight(piece_type, color, target_pos);
    	score -= Evaluator.posWeight(piece_type, color, origin_pos);

    	if (captured != PieceData.EMPTY) {
        	score += 100;
        	score += 3 * Evaluator.piece_points[captured & PieceData.TYPE_MASK];
        	score -= Evaluator.piece_points[piece_type];
    	}
		
    	score += Math.min(history_table[color >> 3][move & 0xFFF], 80);

		// if (depth > 2 && principle_variation[current_depth - depth] == move) score += 1000;

		// if (killer_table[current_depth - depth][0] == move) score += 100;
		// else if (killer_table[current_depth - depth][1] == move) score += 80;

    	return score;
	}

	private static void sortMoves(byte[] board, MoveList possible_moves, int color, int depth, long hash) {
		int hash_move = -1;
		Transposition.BoardState hash_entry = Transposition.getTransposition(hash);
		if (hash_entry != null) hash_move = hash_entry.getBestMove();

		long[] scored_moves = new long[possible_moves.size()];
		for (int i = 0; i < scored_moves.length; i++) {
			int move = possible_moves.get(i);
			scored_moves[i] = (0xFFFFL - (long) heuristicScore(board, move, color, depth, hash_move)) << 40 | (long) i << 32 | move;
		}

		Arrays.sort(scored_moves);

		possible_moves.clear();
		for (long scored : scored_moves) {
			possible_moves.add((int) scored);
		}

	}

	private static int quiescence(byte[] board, int alpha, int beta, int turn, int ply) {
		long board_hash = Zobrist.manualHash(board, turn);
		int static_eval = turn * Evaluator.boardEval(board, turn, board_hash, ply);
		
		if (static_eval >= beta) return static_eval;
		if (alpha < static_eval) alpha = static_eval;

		int color = (turn == 1) ? PieceData.WHITE : PieceData.BLACK;
		MoveList captures_moves = PieceHandler.getAllCaptures(board, color);
		sortMoves(board, captures_moves, color, -1, board_hash);

		int best_eval = static_eval;
		for (int i = 0; i < captures_moves.size(); i++) {
			int move = captures_moves.get(i);
			byte captured = MoveHandler.moveState(board, move, board_hash);

			int gain = Evaluator.piece_points[captured & PieceData.TYPE_MASK];
			if (static_eval + gain + 100 < alpha) {
				MoveHandler.undoState(board, move, captured, board_hash);
				continue;
			}

			int eval = -quiescence(board, -beta, -alpha, -turn, ply + 1);
			MoveHandler.undoState(board, move, captured, board_hash);

			if (eval >= beta) return eval;
			best_eval = Math.max(eval, best_eval);
			alpha = Math.max(eval, alpha);
		}

		return best_eval;
	}

	public static int negascout(byte[] board, int depth, int alpha, int beta, int turn, int[] variation, boolean allow_null) {
    	long board_hash = Zobrist.manualHash(board, turn);
    	Transposition.BoardState entry = Transposition.getTransposition(board_hash);
    	if (entry != null && entry.getDepth() >= depth) {
			System.arraycopy(entry.getBestLine(), 0, variation, current_depth - depth, depth);
        	if (entry.isExact()) {
            	return entry.getBestScore();
        	}
        	if (entry.isBeta() && entry.getBestScore() >= beta) {
            	return entry.getBestScore();
        	}
        	if (entry.isAlpha() && entry.getBestScore() <= alpha) {
            	return entry.getBestScore();
        	}
    	}
		
		if (depth == 0 || Evaluator.isGameOver(board, turn, board_hash)) {
			Game.node_count++;
			return quiescence(board, alpha, beta, turn, current_depth - depth);
		}

		int color = (turn == 1) ? PieceData.WHITE : PieceData.BLACK;
		int static_eval = Evaluator.staticEval(board, turn);
		boolean in_check = PieceHandler.kingUnderAttack(board, color);

		if (depth < 3 && static_eval < alpha - razor_margin[depth] && !in_check) {
			Game.node_count++;
			return quiescence(board, alpha, beta, turn, current_depth - depth);
		}

		if (depth < 3 && static_eval >= beta + futility_margin[depth] && !in_check) {
			return quiescence(board, alpha, beta, turn, current_depth - depth);
		}

		boolean is_futile = false;
		if (depth < 3 && static_eval < alpha - futility_margin[depth] && !in_check) {
			is_futile = true;
		}

		/* It might be worth noting a few of these conditions required
		 * 1. You should not do a consecutive null move, so if you made a null move you should not make another one.
		 * 2. The board should not only have pawns and kings left.
		 */
		
		if (depth > 2 && static_eval >= beta && !in_check && allow_null) {
			int null_eval = -negascout(board, depth - 3, -beta, -beta + 1, -turn, variation, false);
			if (null_eval >= beta) return beta;
		}
    	
		MoveList possible_moves = PieceHandler.getAllMoves(board, color);
		sortMoves(board, possible_moves, color, depth, board_hash);

		int best_score = Integer.MIN_VALUE;
    	int parent_alpha = alpha;
		boolean first_move = true;

		int[] child_variation = new int[current_depth];

    	for (int i = 0; i < possible_moves.size(); i++) {
			int move = possible_moves.get(i);
			boolean is_quiet = board[move & MoveHandler.POS_MASK] == PieceData.EMPTY;

			if (!first_move && is_futile && is_quiet) {
				continue;
			}

        	byte captured = MoveHandler.moveState(board, move, board_hash);

			int eval;
			if (first_move) {
				eval = -negascout(board, depth - 1, -beta, -alpha, -turn, child_variation, allow_null);
				first_move = false;
			} else {
				eval = -negascout(board, depth - 1, -alpha - 1, -alpha, -turn, child_variation, allow_null);

				if (eval > alpha && eval < beta) {
					eval = -negascout(board, depth - 1, -beta, -alpha, -turn, child_variation, allow_null);
				}
			}

        	MoveHandler.undoState(board, move, captured, board_hash);

        	if (System.currentTimeMillis() > cancel_time) {
            	return Integer.MIN_VALUE;
        	}

        	if (eval > best_score) {
				best_score = eval;
				System.arraycopy(child_variation, 0, variation, 0, variation.length);
            	variation[current_depth - depth] = move;
        	}

			if (eval >= beta && is_quiet) {
				history_table[color >> 3][move & 0xFFF] += depth * depth;

				// int ply = current_depth - depth;
				// if (killer_table[ply][0] != move) {
				// 	killer_table[ply][1] = killer_table[ply][0];
				// 	killer_table[ply][0] = move;
				// }
			}

        	alpha = Math.max(eval, alpha);
        	if (beta <= alpha) {
            	break;
        	}
    	}

		int node_type = 
        (best_score >= beta) ? Transposition.BETA_NODE :
    	(best_score <= parent_alpha) ? Transposition.ALPHA_NODE :
    	Transposition.EXACT_NODE;

    	Transposition.addTransposition(board_hash, new Transposition.BoardState(depth, best_score, node_type, Arrays.copyOfRange(variation, current_depth - depth, variation.length)));

        return best_score;
	}
}