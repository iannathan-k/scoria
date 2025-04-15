package src.scoria;

import java.util.ArrayList;

import src.core.Game;
import src.core.Interface;
import src.core.MoveHandler;
import src.pieces.*;

public class Scoria {
	private static final int INITIAL_ALPHA = Integer.MIN_VALUE + 1;
	private static final int INITIAL_BETA  = Integer.MAX_VALUE - 1;

	public static long cancel_time;

	private static int[][] history_table = new int[2][16191];

	public static void preventCancel() {
    	cancel_time = Long.MAX_VALUE;
	}

	public static void clearHistoryTable() {
    	history_table = new int[2][16191];
	}

	public static int[] iterativeDeepener(byte[] board, boolean turn, int MAX_DEPTH, long MAX_TIME) {
    	int sign = turn ? 1 : -1;
    	int current_depth = 0;
    	int[] current_best_move = new int[2];
    	cancel_time = System.currentTimeMillis() + MAX_TIME;

    	long start = System.currentTimeMillis();
    	while (System.currentTimeMillis() < cancel_time && current_depth < MAX_DEPTH) {
        	current_depth++;
        	int[] move = negamax(board, current_depth, INITIAL_ALPHA, INITIAL_BETA, turn, sign, false);
       	 
        	if (move[0] != Integer.MIN_VALUE) {
            	current_best_move = move;
            	long time = System.currentTimeMillis() - start;
            	int nodes = Game.getNodeCount();
            	System.out.println(
                	"info depth " + current_depth +
                	" score " + move[0] +
                	" nodes " + nodes +
                	" time " + time +
                	" pv " + Interface.moveToUci(move[1])
            	);
        	}
    	}

    	return current_best_move;
	}

	private static int heuristicScore(byte[] board, int move, int color) {
    	int origin_pos = (move >> 8) & MoveHandler.POS_MASK;
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

    	score += Math.min(history_table[color >> 3][move & 0xFFFF], 100);

    	return score;
	}

	public static int[] negamax(byte[] board, int depth, int alpha, int beta, boolean turn, int sign, boolean is_null) {
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

    	if (depth == 0 || Evaluator.gameWinner(board, turn, board_hash) != Evaluator.NOT_OVER) {
			Game.node_count++;
        	return new int[] {sign * Evaluator.boardEval(board, turn, board_hash, depth)};
    	}

    	int color = turn ? PieceData.WHITE : PieceData.BLACK;

    	ArrayList<Integer> possible_moves = PieceHandler.getAllMoves(board, color);
    	possible_moves.sort((move1, move2) -> Integer.compare(
        	heuristicScore(board, move2, color),
        	heuristicScore(board, move1, color)
    	));

    	int parent_alpha = alpha;
    	boolean is_first = true;

    	int[] best_eval = {Integer.MIN_VALUE, -1};
    	for (int move : possible_moves) {
        	byte captured = MoveHandler.moveState(board, move, board_hash);

        	int eval;
        	if (is_first) {
            	eval = -negamax(board, depth - 1, -beta, -alpha, !turn, -sign, false)[0];
            	is_first = false;
        	} else {
            	eval = -negamax(board, depth - 1, -alpha - 1, -alpha, !turn, -sign, true)[0];

            	if (eval > alpha && eval < beta) {
                	eval = -negamax(board, depth - 1, -beta, -alpha, !turn, -sign, false)[0];
            	}
        	}

        	MoveHandler.undoState(board, move, captured, board_hash);

        	if (System.currentTimeMillis() > cancel_time) {
            	return new int[] {Integer.MIN_VALUE};
        	}

        	if (eval > best_eval[0]) {
            	best_eval[0] = eval;
            	best_eval[1] = move;
        	}

        	alpha = Math.max(eval, alpha);
        	if (beta <= alpha) {
            	history_table[color >> 3][move & 0xFFFF] += depth * depth;
            	break;
        	}
    	}

		int node_type = 
        (best_eval[0] >= beta) ? Transposition.BETA_NODE :
    	(best_eval[0] <= parent_alpha) ? Transposition.ALPHA_NODE :
    	Transposition.EXACT_NODE;

    	Transposition.addState(board_hash, new Transposition.BoardState(depth, best_eval, node_type));

        return best_eval;
    }
}