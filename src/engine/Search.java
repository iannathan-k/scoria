package src.engine;

import java.util.Arrays;

import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.game.Zobrist;
import src.user.Uci;
import src.utils.GameStack;
import src.utils.MoveList;
import src.utils.Logger;

public class Search {
    public static final int NULL_SCORE = Integer.MIN_VALUE;
    public static final int MAX_TIME = Integer.MAX_VALUE;
    public static final int MAX_DEPTH = 245;
    public static final int MAX_PLY = 256;

    public static final int FIRST_KILLER = 0;
    public static final int SECOND_KILLER = 1;
    public static final int TOFROM_MASK = 0xFFF;
    public static final int MAX_LEGALINDEX = 63;

    private static final int INFINITY = 1 << 30;
    private static final int MAX_HISTORY = 16384;
    private static final int CONT_SIZE = 768;
    private static final int ROOT_PLY = 0;

    public static final int[][] HISTORY_TABLE = new int[2][TOFROM_MASK];
    public static final int[][] KILLER_TABLE = new int[MAX_PLY][2];
    public static final int[][] CMH_TABLE = new int[CONT_SIZE][CONT_SIZE];
    public static final int[][] FMH_TABLE = new int[CONT_SIZE][CONT_SIZE];
    public static final int[][][] CAPTURE_TABLE = new int[6][6][64];
    public static final int[][] LMR_TABLE = new int[MAX_PLY][MAX_LEGALINDEX + 1];

    private static final MoveList[] MOVE_LISTS = new MoveList[MAX_PLY];
    private static final int[][] SEARCHED_QUIETS = new int[MAX_PLY][256];
    private static final int[][] SEARCHED_CAPTURES = new int[MAX_PLY][256];
    private static final int[] SEE_GAIN = new int[32];

    public static volatile boolean is_searching;
    private static long nodes;

    public static void iterativeDeepener() {
        is_searching = true;
        int start_alpha = -INFINITY;
        int start_beta = INFINITY;
        
        int current_depth = 1;
        nodes = 0;

        while (!Timer.hitSoftLimit(current_depth)) {
            int eval = negascout(current_depth, ROOT_PLY, start_alpha, start_beta, BitBoard.moving_side);

            // Hard Time Limit Hit
            if (eval == NULL_SCORE) {
                break;
            }

            if (eval <= start_alpha) {
                start_alpha = -INFINITY;
                continue;
            }
            if (eval >= start_beta) {
                start_beta = INFINITY;
                continue;
            }

            start_alpha = eval - 70;
            start_beta = eval + 70;

            String score_str = 
                (eval > Evaluator.MATE_BOUND) ? "mate " + (Evaluator.MATE_SCORE - eval + 1) / 2
                : (eval < -Evaluator.MATE_BOUND) ? "mate " + (-Evaluator.MATE_SCORE - eval) / 2
                : "cp " + eval;

            Logger.outln(
                "info depth " + current_depth +
                " score " + score_str +
                " nodes " + nodes +
                " nps " + (nodes * 1000 / (Timer.elapsed() + 1)) +
                " hashfull " + Transposition.countHashFull() +
                " time " + Timer.elapsed() +
                " pv " + Uci.moveListToUciString(collectPV(current_depth))
            );

            Transposition.nextGeneration();
            current_depth++;
        }

        MoveList pv = collectPV(2);
        Logger.out("bestmove " + Uci.moveToUci(pv.getMove(0)));

        String ponder_str = (Timer.doPonder() && pv.size() > 1) 
            ? " ponder " + Uci.moveToUci(pv.getMove(1)) 
            : "";
            
        Logger.outlnn(ponder_str);

        is_searching = false;
    }

    private static MoveList collectPV(int depth) {
        MoveList pv = new MoveList();
        long[] seen = new long[depth];

        for (int i = 0; i < depth; i++) {
            long hash = Zobrist.getZobristHash();

            int tt_index = Transposition.probe(hash);
            if (tt_index == Transposition.NULL_ENTRY) {
                break;
            }

            boolean is_repetition = false;
            for (int j = 0; j < i; j++) {
                if (seen[j] == hash) {
                    is_repetition = true;
                    break;
                }
            }

            if (is_repetition) {
                break;
            }

            if (i > 0) {
                if (Evaluator.isThreeFoldRepetition(hash)) {
                    break;
                }

                if (Evaluator.isFiftyMoves()) {
                    break;
                }

                if (Evaluator.isInsufficientMaterial()) {
                    break;
                }
            }

            int move = Transposition.getBestMove(tt_index);
            pv.add(move);

            seen[i] = hash;

            MoveHandler.doMove(move);
        }

        for (int i = 0; i < pv.size(); i++) {
            MoveHandler.undoMove();
        }

        return pv;
    }

    public static void initSearchTables() {
        for (int i = 0; i < MAX_PLY; i++) {
            MOVE_LISTS[i] = new MoveList();
            KILLER_TABLE[i][FIRST_KILLER] = MoveHandler.NULL_MOVE;
            KILLER_TABLE[i][SECOND_KILLER] = MoveHandler.NULL_MOVE;
            MovePicker.BADNOISY_LISTS[i] = new MoveList();
        }

        for (int depth = 1; depth < 64; depth++) {
            for (int legal_count = 1; legal_count < 64; legal_count++) {

                double r = 0.93 + Math.log(depth) * Math.log(legal_count) / 2.7;
                int reduction = (int) r;
                
                reduction = Math.max(0, reduction);
                reduction = Math.min(reduction, depth - 2);

                LMR_TABLE[depth][legal_count] = reduction;
            }
        }
    }

    public static void clearHeuristics() {
        for (int[] arr : HISTORY_TABLE) {
            Arrays.fill(arr, 0);
        }

        for (int[] arr : KILLER_TABLE) {
            Arrays.fill(arr, MoveHandler.NULL_MOVE);
        }

        for (int[] arr : CMH_TABLE) {
            Arrays.fill(arr, 0);
        }

        for (int[] arr : FMH_TABLE) {
            Arrays.fill(arr, 0);
        }

        for (int[][] row : CAPTURE_TABLE) {
            for (int[] col : row) {
                Arrays.fill(col, 0);
            }
        }
    }

    public static int see(int move) { 
        int origin = (move >> 6) & MoveHandler.POSITION_MASK;
        int target = move & MoveHandler.POSITION_MASK;
        int piece = (move >> 12) & MoveHandler.PIECE_MASK;
        int captured = BitBoard.getPieceAt(target);

        int piece_value = Evaluator.PIECE_VALUES[piece & BitBoard.PIECE_MASK];

        int depth = 0;
        long occupancy = BitBoard.occupancy_bitboard;
        int turn = piece & BitBoard.COLOR_MASK;

        int captured_value = 0;
        if ((move & MoveHandler.PASSANT_FLAG) != 0) {
            captured_value = 65;

            int ep_square = (turn == BitBoard.WHITE)
                ? target + BitBoard.SOUTH : target + BitBoard.NORTH;

            occupancy &= ~(1L << ep_square);
        } else if (captured != BitBoard.NO_PIECE) {
            captured_value = Evaluator.PIECE_VALUES[captured & BitBoard.PIECE_MASK];
        }

        if ((move & MoveHandler.PROMOTED_MASK) != 0) {
            int promoted_piece = (move & MoveHandler.PROMOTED_MASK) >> 16;
            captured_value += Evaluator.PIECE_VALUES[promoted_piece & BitBoard.PIECE_MASK] - 65;
        }
        
        SEE_GAIN[0] = captured_value;
        occupancy &= ~(1L << origin); 

        long attadef = MoveGenerator.getAttackers(target);

        while (true) {
            depth++;
            turn ^= 1;

            long attackers = attadef & BitBoard.color_bitboards[turn] & occupancy;
            
            if (attackers == 0L) {
                break;
            }

            int lva_square = -1;
            int lva_value = 0;
            
            for (int i = 0; i < 12; i += 2) {
                long subset = attackers & BitBoard.piece_bitboards[i | turn];
                if (subset != 0L) {
                    lva_square = Long.numberOfTrailingZeros(subset);
                    lva_value = Evaluator.PIECE_VALUES[i];
                    break;
                }
            }
            
            SEE_GAIN[depth] = piece_value - SEE_GAIN[depth - 1];
            piece_value = lva_value;
            occupancy &= ~(1L << lva_square);

            attadef |= MoveGenerator.getXRayAttackers(target, occupancy);
        }

        while (--depth > 0) {
            SEE_GAIN[depth - 1] = -Math.max(-SEE_GAIN[depth - 1], SEE_GAIN[depth]);
        }

        return SEE_GAIN[0];
    }
   
    private static int quiescence(int alpha, int beta, int ply, int turn) {
        int static_eval = Evaluator.getRelativeEvaluation(turn);

        if (ply >= MAX_PLY) {
            return static_eval;
        }

        if (static_eval >= beta) {
            return static_eval;
        }

        if (alpha < static_eval) {
            alpha = static_eval;
        }

        int best_eval = static_eval;

        MovePicker.initQSNode(ply);

        int move;
        while ((move = MovePicker.nextQSMove(MOVE_LISTS[ply], ply, turn)) != MoveHandler.NULL_MOVE) {

            // Delta Pruning
            int target = move & MoveHandler.POSITION_MASK;
            int captured = BitBoard.getPieceAt(target);
            if ((move & MoveHandler.PASSANT_FLAG) != 0) {
                captured = BitBoard.PAWN;
            }

            int delta = 0;
            if ((move & MoveHandler.PROMOTED_MASK) != 0) {
                int promoted = (move & MoveHandler.PROMOTED_MASK) >> 16;
                delta += Evaluator.PIECE_VALUES[promoted & BitBoard.PIECE_MASK] - 65;
            }
            if (captured != BitBoard.NO_PIECE) {
                delta += Evaluator.PIECE_VALUES[captured & BitBoard.PIECE_MASK];
            }
            if (static_eval + delta + 201 < alpha) {
                continue;
            }

            MoveHandler.doMove(move);

            // Verify Pseudo Legality
            if (MoveGenerator.isKingInCheck(turn)) {
                MoveHandler.undoMove();
                continue;
            }
            nodes++;

            int eval = -quiescence(-beta, -alpha, ply + 1, turn ^ 1);

            MoveHandler.undoMove();

            best_eval = Math.max(best_eval, eval);
            alpha = Math.max(alpha, eval);

            if (alpha >= beta) {
                break;
            }
        }

        return best_eval;
    }

    private static int negascout(int depth, int ply, int alpha, int beta, int turn) {
        long board_hash = Zobrist.getZobristHash();

        if (ply > ROOT_PLY) {
            if (ply >= MAX_PLY) {
                return Evaluator.getRelativeEvaluation(turn);
            }

            // Fifty Move Rule
            if (Evaluator.isFiftyMoves()) {
                if (!MoveGenerator.isKingInCheck(turn)) {
                    return Evaluator.getDrawScore(nodes);
                }

                // Checkmate Edgecase
                boolean has_move = false;
                MovePicker.initNode(ply);
                int move;
                while ((move = MovePicker.nextMove(MOVE_LISTS[ply], ply, turn)) != MoveHandler.NULL_MOVE) {
                    MoveHandler.doMove(move);
                    has_move = !MoveGenerator.isKingInCheck(turn);
                    MoveHandler.undoMove();

                    if (has_move) {
                        break;
                    }
                }

                return has_move ? Evaluator.getDrawScore(nodes) : Evaluator.getMateScore(ply);
            }
            
            // Threefold Repetition
            if (Evaluator.isThreeFoldRepetition(board_hash)) {
                return Evaluator.getDrawScore(nodes);
            }

            // Insufficient Material
            if (Evaluator.isInsufficientMaterial() && !MoveGenerator.isKingInCheck(turn)) {
                return Evaluator.getDrawScore(nodes);
            }
        }

        boolean is_pv = beta - alpha > 1;
        
        // Transposition Probe
        int tt_index = Transposition.probe(board_hash);
        boolean tt_hit = tt_index != Transposition.NULL_ENTRY;
        int tt_node = tt_hit ? Transposition.getNodeType(tt_index) : Transposition.NULL_NODE;
        int tt_score = tt_hit ? Transposition.getScore(tt_index, ply) : NULL_SCORE;

        // Transposition Prune
        if (tt_hit 
            && !is_pv 
            && Transposition.getDepth(tt_index) >= depth
            && Transposition.isInformative(tt_node, tt_score, beta)) {

            return tt_score;
        }

        if (depth <= 0) {
            return quiescence(alpha, beta, ply, turn);
        }
        
        boolean in_check = MoveGenerator.isKingInCheck(turn);
        int static_eval = Evaluator.getRelativeEvaluation(turn);

        // Razoring & Deep Razoring
        if (depth < 3
            && !in_check
            && !is_pv
            && static_eval < alpha - 203 - 207 * depth * depth
            && Math.abs(alpha) < Evaluator.MATE_BOUND) {

            return quiescence(alpha, beta, ply, turn);
        }

        // Reverse Futility Pruning
        if (depth < 7
            && !in_check
            && !is_pv
            && static_eval >= beta + 126 + 113 * depth
            && Math.abs(beta) < Evaluator.MATE_BOUND) {

            return quiescence(alpha, beta, ply, turn);
        }

        // Futility Pruning
        boolean is_futile = false;
        if (depth < 7
            && !in_check
            && static_eval < alpha - 221 - 187 * depth
            && Math.abs(beta) < Evaluator.MATE_BOUND
            && Math.abs(alpha) < Evaluator.MATE_BOUND) {

            is_futile = true;
        }
        
        // Null Move Pruning
        if (depth >= 3 
            && static_eval >= beta
            && !in_check
            && !is_pv
            && Math.abs(beta) < Evaluator.MATE_BOUND
            && BitBoard.hasNonPawnPiece(turn)) {

            int r = 3 + depth / 6;

            r = Math.min(r, depth);
                
            MoveHandler.doNullMove();
            
            int null_eval = -negascout(depth - r, ply + 1, -beta, -beta + 1, turn ^ 1);
            
            MoveHandler.undoNullMove();
            
            if (null_eval >= beta) {
                return beta;
            }
        }

        // ProbCut
        if (!is_pv
            && !in_check
            && depth > 4
            && Math.abs(beta) < Evaluator.MATE_BOUND) {

            int prob_beta = beta + 203;

            MovePicker.initQSNode(ply);
            int move;
            while ((move = MovePicker.nextQSMove(MOVE_LISTS[ply], ply, turn)) != MoveHandler.NULL_MOVE) {
                MoveHandler.doMove(move);

                if (MoveGenerator.isKingInCheck(turn)) {
                    MoveHandler.undoMove();
                    continue;
                }
                nodes++;

                int prob_eval = -quiescence(-prob_beta, -prob_beta + 1, ply + 1, turn ^ 1);

                if (prob_eval >= prob_beta) {
                    prob_eval = -negascout(depth - 4, ply + 1, -prob_beta, -prob_beta + 1, turn ^ 1);
                }

                MoveHandler.undoMove();

                if (prob_eval >= prob_beta) {
                    int score = (prob_eval > Evaluator.MATE_BOUND) ? prob_eval : prob_eval - 203;

                    Transposition.addTransposition(
                        board_hash,
                        depth - 3,
                        score,
                        Transposition.BETA_NODE,
                        move,
                        ply
                    );

                    return score;
                }
            }
        }

        int parent_alpha = alpha;
        int best_score = NULL_SCORE;
        int best_move = MoveHandler.NULL_MOVE;
        int quiet_count = 0;
        int capture_count = 0;
        int legal_count = 0;

        MovePicker.initNode(ply);

        int move;
        while ((move = MovePicker.nextMove(MOVE_LISTS[ply], ply, turn)) != MoveHandler.NULL_MOVE) {
            boolean is_quiet = 
                BitBoard.isEmpty(move & MoveHandler.POSITION_MASK)
                && (move & MoveHandler.PASSANT_FLAG) == 0;

            // Futility Pruning
            if (legal_count != 0
                && is_futile
                && is_quiet
                && !is_pv
                && move != KILLER_TABLE[ply][FIRST_KILLER]
                && move != KILLER_TABLE[ply][SECOND_KILLER]) {

                continue;
            }

            // History Pruning
            if (depth <= 3
                && is_quiet
                && !in_check
                && legal_count > 1
                && Math.abs(alpha) < Evaluator.MATE_BOUND) {

                int history_score = HISTORY_TABLE[turn][move & TOFROM_MASK];

                int c_piece = (move >> 12) & MoveHandler.PIECE_MASK;
                int c_target = move & MoveHandler.POSITION_MASK;
                int c_index = c_piece << 6 | c_target;

                if (GameStack.hasContinuation(GameStack.CMH_INDEX)) {
                    int cmh_move = GameStack.peekMove();
                    int cmh_piece = (cmh_move >> 12) & MoveHandler.PIECE_MASK;
                    int cmh_target = cmh_move & MoveHandler.POSITION_MASK;

                    history_score += CMH_TABLE[cmh_piece << 6 | cmh_target][c_index];
                }

                if (GameStack.hasContinuation(GameStack.FMH_INDEX)) {
                    int fmh_move = GameStack.getMoveFromBack(GameStack.FMH_INDEX);
                    int fmh_piece = (fmh_move >> 12) & MoveHandler.PIECE_MASK;
                    int fmh_target = fmh_move & MoveHandler.POSITION_MASK;

                    history_score += FMH_TABLE[fmh_piece << 6 | fmh_target][c_index];
                }

                if (history_score < -2000 * depth) {
                    continue;
                }
            }

            MoveHandler.doMove(move);

            // Verify Pseudo Legality
            if (MoveGenerator.isKingInCheck(turn)) {
                MoveHandler.undoMove();
                continue;
            }
            nodes++;

            int extension = 0;
            if (in_check 
                && ply > ROOT_PLY 
                && ply < 2 * depth) {

                extension = 1;
            }

            // Late Move Reduction
            int reduction = 0;
            if (depth > 2
                && legal_count > 3
                && is_quiet
                && !in_check
                && Math.abs(alpha) < Evaluator.MATE_BOUND) {

                reduction = LMR_TABLE[depth][Math.min(legal_count, MAX_LEGALINDEX)];
            }

            // Null Window Search
            int eval;
            if (legal_count == 0) {
                eval = -negascout(depth - 1 + extension, ply + 1, -beta, -alpha, turn ^ 1);
            } else {
                eval = -negascout(depth - 1 - reduction + extension, ply + 1, -alpha - 1, -alpha, turn ^ 1);

                // LMR Research
                if (reduction > 0 && eval > alpha) {
                    eval = -negascout(depth - 1 + extension, ply + 1, -alpha - 1, -alpha, turn ^ 1);
                }

                if (eval > alpha && eval < beta) {
                    eval = -negascout(depth - 1 + extension, ply + 1, -beta, -alpha, turn ^ 1);
                }
            }

            MoveHandler.undoMove();
            legal_count++;

            if ((nodes & 2047) == 0 && Timer.hitHardLimit()) {
                return NULL_SCORE;
            }

            if (eval > best_score) {
                best_move = move;
                best_score = eval;
            }

            alpha = Math.max(alpha, eval);

            if (alpha >= beta) {
                if (is_quiet) {
                    int current = HISTORY_TABLE[turn][move & TOFROM_MASK];
                    int bonus = 300 * depth - 250;

                    // History Heuristic
                    HISTORY_TABLE[turn][move & TOFROM_MASK] += bonus - current * bonus / MAX_HISTORY;

                    // Killer Moves
                    if (KILLER_TABLE[ply][FIRST_KILLER] != move) {
                        KILLER_TABLE[ply][SECOND_KILLER] = KILLER_TABLE[ply][FIRST_KILLER];
                        KILLER_TABLE[ply][FIRST_KILLER] = move;
                    }

                    // Counter Move Heuristic
                    int c_piece = (move >> 12) & MoveHandler.PIECE_MASK;
                    int c_target = move & MoveHandler.POSITION_MASK;
                    int c_index = c_piece << 6 | c_target;

                    boolean has_cmh = GameStack.hasContinuation(GameStack.CMH_INDEX);
                    boolean has_fmh = GameStack.hasContinuation(GameStack.FMH_INDEX);
                    int cmh_index = -1;
                    int fmh_index = -1;

                    if (has_cmh) {
                        int cmh_move = GameStack.peekMove();
                        int cmh_piece = (cmh_move >> 12) & MoveHandler.PIECE_MASK;
                        int cmh_target = cmh_move & MoveHandler.POSITION_MASK;

                        cmh_index = cmh_piece << 6 | cmh_target;
                        current = CMH_TABLE[cmh_index][c_index];
                        CMH_TABLE[cmh_index][c_index] += bonus - current * bonus / MAX_HISTORY;
                    }

                    // Follow Move Heuristic
                    if (has_fmh) {
                        int fmh_move = GameStack.getMoveFromBack(GameStack.FMH_INDEX);
                        int fmh_piece = (fmh_move >> 12) & MoveHandler.PIECE_MASK;
                        int fmh_target = fmh_move & MoveHandler.POSITION_MASK;

                        fmh_index = fmh_piece << 6 | fmh_target;
                        current = FMH_TABLE[fmh_index][c_index];
                        FMH_TABLE[fmh_index][c_index] += bonus - current * bonus / MAX_HISTORY;
                    }

                    // History Maluses
                    for (int j = 0; j < quiet_count; j++) {
                        int f_move = SEARCHED_QUIETS[ply][j];
                        int f_piece = (f_move >> 12) & MoveHandler.PIECE_MASK;
                        int f_target = f_move & MoveHandler.POSITION_MASK;
                        int f_index = f_piece << 6 | f_target;

                        current = HISTORY_TABLE[turn][f_move & TOFROM_MASK];
                        HISTORY_TABLE[turn][f_move & TOFROM_MASK] -= bonus + current * bonus / MAX_HISTORY;
                        
                        if (has_cmh) {
                            current = CMH_TABLE[cmh_index][f_index];
                            CMH_TABLE[cmh_index][f_index] -= bonus + current * bonus / MAX_HISTORY;
                        }

                        if (has_fmh) {
                            current = FMH_TABLE[fmh_index][f_index];
                            FMH_TABLE[fmh_index][f_index] -= bonus + current * bonus / MAX_HISTORY;
                        }
                    }
                } else {
                    int target = move & MoveHandler.POSITION_MASK;
                    int piece = ((move >> 12) & MoveHandler.PIECE_MASK) >> 1;
                    int captured = BitBoard.getPieceAt(target) >> 1;

                    if ((move & MoveHandler.PASSANT_FLAG) != 0) {
                        captured = BitBoard.PAWN;
                    }

                    int current = CAPTURE_TABLE[piece][captured][target];
                    int bonus = 300 * depth - 250;

                    CAPTURE_TABLE[piece][captured][target] += bonus - current * bonus / MAX_HISTORY;

                    // History Maluses
                    for (int j = 0; j < capture_count; j++) {
                        int f_move = SEARCHED_CAPTURES[ply][j];
                        int f_target = f_move & MoveHandler.POSITION_MASK;
                        int f_piece = ((f_move >> 12) & MoveHandler.PIECE_MASK) >> 1;
                        int f_captured = BitBoard.getPieceAt(f_target) >> 1;

                        if ((f_move & MoveHandler.PASSANT_FLAG) != 0) {
                            f_captured = BitBoard.PAWN;
                        }   

                        current = CAPTURE_TABLE[f_piece][f_captured][f_target];
                        CAPTURE_TABLE[f_piece][f_captured][f_target] -= bonus + current * bonus / MAX_HISTORY;
                    }
                }

                break;
            }

            if (is_quiet) {
                SEARCHED_QUIETS[ply][quiet_count++] = move;
            } else {
                SEARCHED_CAPTURES[ply][capture_count++] = move;
            }
        }

        // Checkmate & Stalemate Condition
        if (legal_count == 0) {
            return in_check ? Evaluator.getMateScore(ply) : Evaluator.DRAW_SCORE;
        }

        byte node_type = (best_score >= beta) ? Transposition.BETA_NODE
            : (best_score <= parent_alpha) ? Transposition.ALPHA_NODE
            : Transposition.EXACT_NODE;

        Transposition.addTransposition(
            board_hash, 
            depth, 
            best_score, 
            node_type, 
            best_move, 
            ply
        );

        return best_score;
    }
}