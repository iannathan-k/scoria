package src.engine;

import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.game.Precomputer;
import src.game.Zobrist;
import src.user.Uci;
import src.utils.GameStack;
import src.utils.MoveList;

public class Search {

    public static final int MAX_TIME = Integer.MAX_VALUE;
    public static final int MAX_DEPTH = Integer.MAX_VALUE;
    public static final int NULL_SCORE = Integer.MIN_VALUE;

    public static final int FIRST_KILLER = 0;
    public static final int SECOND_KILLER = 1;

    private static final int INFINITY = 1 << 30;
    public static final int TOFROM_MASK = 0xFFF;
    private static final int MAX_HISTORY = 16384;
    private static final int CONT_SIZE = 768;
    public static final int MAX_PLY = 64;

    public static final int[][] HISTORY_TABLE = new int[2][TOFROM_MASK];
    public static final int[][] KILLER_TABLE = new int[MAX_PLY][2];
    public static final int[][] CMH_TABLE = new int[CONT_SIZE][CONT_SIZE];
    public static final int[][] FMH_TABLE = new int[CONT_SIZE][CONT_SIZE];
    // public static final int[] EXCLUDED_TABLE = new int[MAX_PLY];
    public static final int[][][] CAPTURE_TABLE = new int[6][6][64];

    private static final MoveList[] MOVE_LISTS = new MoveList[MAX_PLY];
    private static final int[][] SEARCHED_QUIETS = new int[MAX_PLY][256];
    private static final int[][] SEARCHED_CAPTURES = new int[MAX_PLY][256];
    private static final int[] SEE_GAIN = new int[32];

    private static long cancel_time;
    private static long node_count;

    // FIXME: Lazily implemented
    // FIXME: Add can return, to break when finding mate or using book in actual games
    // FIXME: Only break when we are giving checkmate, as opponent may miss it, which we want to keep playing in
    public static void iterativeDeepener(int max_depth, int max_time) {
        int start_alpha = -INFINITY;
        int start_beta = INFINITY;
        
        int current_depth = 0;
        long start = System.currentTimeMillis();

        cancel_time = start + max_time;
        node_count = 0;

        while (current_depth < max_depth && System.currentTimeMillis() < cancel_time) {
            current_depth++;
            int eval = negascout(current_depth, 0, start_alpha, start_beta, BitBoard.moving_side);

            if (eval == NULL_SCORE) {
                break;
            }

            // Finish Aspiration Windows
            if (eval <= start_alpha || eval >= start_beta) {
                // FIXME: Remove Later
                System.out.println("RESTART");
                start_alpha = -INFINITY;
                start_beta = INFINITY;
                current_depth--;
                continue;
            }

            start_alpha = eval - 70;
            start_beta = eval + 70;

            System.out.println(
                "info depth " + current_depth +
                " score cp " + eval +
                " nodes " + node_count +
                " nps " + (node_count * 1000 / (System.currentTimeMillis() - start + 1)) +
                " hashfull " + Transposition.countHashFull() +
                " time " + (System.currentTimeMillis() - start) +
                " pv " + Uci.moveListToUciString(collectPV(current_depth))
            );

            Transposition.nextGeneration();
        }

        System.out.println("bestmove " + Uci.moveListToUciString(collectPV(1)));
    }

    private static MoveList collectPV(int depth) {
        MoveList pv = new MoveList();
        long[] seen = new long[depth];

        for (int i = 0; i < depth; i++) {
            long hash = Zobrist.getZobristHash();

            if (!Transposition.doesExist(hash)) {
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

            if (i > 0 && Evaluator.isThreeFoldRepetition(hash)) {
                break;
            }

            int move = Transposition.getBestMove(hash);
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
            // EXCLUDED_TABLE[i] = MoveHandler.NULL_MOVE;
            KILLER_TABLE[i][FIRST_KILLER] = MoveHandler.NULL_MOVE;
            KILLER_TABLE[i][SECOND_KILLER] = MoveHandler.NULL_MOVE;
            MovePicker.BADNOISY_LISTS[i] = new MoveList();
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

        int captured_value;
        if ((move & MoveHandler.PASSANT_FLAG) != 0) {
            captured_value = Evaluator.PIECE_VALUES[BitBoard.WHITE_PAWN];

            int ep_square = (turn == BitBoard.WHITE)
                ? target + BitBoard.SOUTH : target + BitBoard.NORTH;

            occupancy &= ~(1L << ep_square);
        } else {
            captured_value = Evaluator.PIECE_VALUES[captured & BitBoard.PIECE_MASK];
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

            // FIXME: Tune Margin
            if (static_eval + Evaluator.PIECE_VALUES[captured & BitBoard.PIECE_MASK] + 201 < alpha) {
                continue;
            }

            MoveHandler.doMove(move);

            // Verify Pseudo Legality
            if (MoveGenerator.isKingInCheck(turn)) {
                MoveHandler.undoMove();
                continue;
            }

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

        // FIXME: Implement fifty move rule
        // FIXME: Implement insufficient material
        if (ply > 0) {
            // if (alpha < Evaluator.DRAW_SCORE && Evaluator.isCycle(board_hash, ply)) {
            //     alpha = Evaluator.getDrawScore(node_count);

            //     if (alpha >= beta) {
            //         return alpha;
            //     }
            // } 

            if (Evaluator.isThreeFoldRepetition(board_hash)) {
                return Evaluator.getDrawScore(node_count);
            }

            // Mate Distance Pruning
            // alpha = Math.max(alpha, -Evaluator.MATE_SCORE + ply);
            // beta = Math.min(beta, Evaluator.MATE_SCORE - ply - 1);
            // if (alpha >= beta) {
            //     return alpha;
            // }
        }

        // boolean is_singular = EXCLUDED_TABLE[ply] != MoveHandler.NULL_MOVE;
        boolean is_pv = beta - alpha > 1;
        
        // Transposition Probe
        boolean tt_hit = Transposition.doesExist(board_hash);
        int tt_node = tt_hit ? Transposition.getNodeType(board_hash) : Transposition.NULL_NODE;
        int tt_score = tt_hit ? Transposition.getScore(board_hash, ply) : NULL_SCORE;

        // Transposition Prune
        if (tt_hit 
            && !is_pv 
            && Transposition.getDepth(board_hash) >= depth
            && Transposition.isInformative(tt_node, tt_score, beta)) {

            return tt_score;
        }

        if (depth == 0) {
            node_count++;
            return quiescence(alpha, beta, ply, turn);
        }
        
        boolean in_check = MoveGenerator.isKingInCheck(turn);
        int static_eval = Evaluator.getRelativeEvaluation(turn);

        // Correct Static Evaluation
        // if (tt_hit 
        //     && !in_check 
        //     && Math.abs(tt_score) < Evaluator.MATE_BOUND
        //     && Transposition.isInformative(tt_node, tt_score, static_eval)) {
            
        //     corr_eval = tt_score;
        // }

        // Internal Iterative Reduction
        // if (depth > 5
        //     && is_pv
        //     && !Transposition.doesExist(board_hash)) {
            
        //     depth--;
        // }

        // // Razoring & Deep Razoring
        if (depth < 3
            && !in_check
            && !is_pv
            && static_eval < alpha - 203 - 207 * depth * depth) {

            node_count++;
            return quiescence(alpha, beta, ply, turn);
        }

        // Reverse Futility Pruning
        // FIXME: Maybe return static eval because we are already winning so much
        if (depth < 7
            && !in_check
            && !is_pv
            && static_eval >= beta + 126 + 113 * depth) {

            node_count++;
            return quiescence(alpha, beta, ply, turn);
        }

        // Futility Pruning
        boolean is_futile = false;
        if (depth < 7
            && static_eval < alpha - 221 - 187 * depth
            && !in_check) {

            is_futile = true;
        }
        
        // Null Move Pruning
        // FIXME: Takes longer to find mates
        // FIXME: Maybe eval can come from the TT
        if (depth >= 3 
            && static_eval >= beta
            && !in_check
            && !is_pv
            && BitBoard.hasNonPawnPiece(turn)) {

            int r = 4 + depth / 6;
            r = Math.min(r, depth);
                
            MoveHandler.doNullMove();
            
            int null_eval = -negascout(depth - r, ply + 1, -beta, -beta + 1, turn ^ 1);
            
            MoveHandler.undoNullMove();
            
            if (null_eval >= beta) {
                node_count++;
                return beta;
            }
        }

        // ProbCut
        if (!is_pv
            && !in_check
            && depth > 4
            && Math.abs(beta) < Evaluator.MATE_BOUND) {

            int prob_beta = beta + 200;

            MovePicker.initQSNode(ply);
            int move;
            while ((move = MovePicker.nextQSMove(MOVE_LISTS[ply], ply, turn)) != MoveHandler.NULL_MOVE) {
                MoveHandler.doMove(move);

                if (MoveGenerator.isKingInCheck(turn)) {
                    MoveHandler.undoMove();
                    continue;
                }

                int prob_eval = -quiescence(-prob_beta, -prob_beta + 1, ply + 1, turn ^ 1);

                if (prob_eval >= prob_beta) {
                    prob_eval = -negascout(depth - 4, ply + 1, -prob_beta, -prob_beta + 1, turn ^ 1);
                }

                MoveHandler.undoMove();

                if (prob_eval >= prob_beta) {
                    int score = (prob_eval > Evaluator.MATE_BOUND) ? prob_eval : prob_eval - 200;

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

        // Internal Iterative Deepening
        // if (depth > 5
        //     && is_pv
        //     && !Transposition.doesExist(board_hash)) {

        //     negascout(depth - 2, ply, alpha, beta, turn);
        // }

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
                && legal_count > 1) {

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

            // Late Move Pruning
            // if (depth <= 3
            //     && !in_check
            //     && is_quiet
            //     && legal_count > 3 + 2 * depth * depth) {
                    
            //     continue;
            // }

            // Singular Extensions
            int extension = 0;
            // if (depth > 5
            //     && ply < 2 * depth
            //     && move == Transposition.getBestMove(board_hash)
            //     && EXCLUDED_TABLE[ply] == MoveHandler.NULL_MOVE
            //     && Transposition.getDepth(board_hash) >= depth - 3
            //     && !Transposition.isAlpha(board_hash)
            //     && Math.abs(Transposition.getScore(board_hash, ply)) < Evaluator.MATE_BOUND) {

            //     int singular_beta = Transposition.getScore(board_hash, ply) - 4 * depth;

            //     EXCLUDED_TABLE[ply + 1] = move;

            //     int singular_eval = negascout(depth / 2, ply + 1, singular_beta - 1, singular_beta, turn);

            //     EXCLUDED_TABLE[ply + 1] = MoveHandler.NULL_MOVE;

            //     if (singular_eval < singular_beta) {
            //         extension = 1;
            //     } 
                
            //     // else if (singular_beta >= beta) {

            //     //     // Multicut
            //     //     return singular_beta;
            //     // } else if (Transposition.getScore(board_hash, ply) >= beta) {

            //     //     // Negative Extensions
            //     //     extension = -1;
            //     // }
            // }

            MoveHandler.doMove(move);

            // Verify Pseudo Legality
            if (MoveGenerator.isKingInCheck(turn)) {
                MoveHandler.undoMove();
                continue;
            }

            // Check Extenstions
            // FIXME: Test whether this actually improves performance
            // FIXME: Consider only extending when ply < depth or 2 * ply
            boolean gives_check = MoveGenerator.isKingInCheck(turn ^ 1);
            if (gives_check && ply < 2 * depth) {
                extension = 1;
            }
            
            // Late Move Reduction
            // FIXME: Consider Precalculating the Logs
            // FIXME: Don't reduce killers, captures, etc.
            int reduction = 0;
            if (depth > 2
                && legal_count > 3
                && is_quiet
                && !in_check
                && !gives_check) {

                double r = 0.8 + Math.log(depth) * Math.log(legal_count) / 4.0;

                reduction = (int) r;

                reduction = Math.max(0, reduction);
                reduction = Math.min(reduction, depth - 2);
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

            if ((node_count & 2047) == 0 && System.currentTimeMillis() > cancel_time) {
                return NULL_SCORE;
            }

            if (eval > best_score) {
                best_move = move;
                best_score = eval;
            }

            alpha = Math.max(alpha, eval);

            if (alpha >= beta) {
                // if (is_singular) {
                //     break;
                // }

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
        // FIXME: Return draw score from here
        if (legal_count == 0) {
            return Evaluator.getMateScore(turn, ply);
        }

        // if (is_singular) {
        //     return best_score;
        // }

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

    public static void main(String[] args) {

        // BitBoard.initBoardByFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        // BitBoard.initBoardByFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ");
        // BitBoard.initBoardByFen("8/7k/8/8/8/Q7/K7/8 w - - 0 1");
        // BitBoard.initBoardByFen("8/7k/8/8/2K5/6Q1/8/8 b - - 5 3");
        // BitBoard.initBoardByFen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1");
        // BitBoard.initBoardByFen("2r2rk1/1ppq2b1/1n6/1N1Ppp2/p7/Pn2BP2/1PQ1BP2/3RK2R w K - 0 23");
        // BitBoard.initBoardByFen("6k1/b7/8/2q4R/8/8/8/K1Q5 w - - 0 1");
        BitBoard.initBoardByFen("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10");
        // BitBoard.initBoardByFen("8/6k1/8/4p3/Q2b4/2P5/1K6/3R4 w - - 0 1");

        // BitBoard.initBoardByFen("4r3/6k1/6b1/3p4/4R3/3P4/1K3N2/8 w - - 0 1");
        Precomputer.initAllMoveTables();
        Zobrist.initZobristTable(); 
        initSearchTables();

        iterativeDeepener(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
}