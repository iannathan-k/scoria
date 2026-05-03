package src.engine;

import java.util.Arrays;

import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.game.Precomputer;
import src.game.Zobrist;
import src.user.Uci;
import src.utils.MoveList;

public class Search {

    private static final int INFINITY = 1 << 30;
    private static final int BIG_DELTA = 975;
    private static final int TO_FROM_MASK = 0xFFF;
    private static final int HISTORY_AGE = 8191;
    private static final int MAX_HISTORY = 512;
    
    private static final int[] RAZOR_MARGIN = {0, 200, 1000};
    private static final int[] FUTILITY_MARGIN = {0, 150, 750};

    private static final int[][] HISTORY_TABLE = new int[2][TO_FROM_MASK];
    private static final int[][] KILLER_TABLE = new int[128][2];

    private static long cancel_time;
    private static long node_count;

    // FIXME: Lazily implemented
    // FIXME: Add can return, to break when finding mate or using book in actual games
    // Only break when we are giving checkmate, as opponent may miss it, which we want to keep playing in
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

            if (eval == Integer.MIN_VALUE) {
                break;
            }

            // Aspiration Windows
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
                " pv " + Uci.moveListToUciString(collectPrincipalVariation(current_depth))
            );

            Transposition.nextGeneration();
        }

        System.out.println("bestmove " + Uci.moveListToUciString(collectPrincipalVariation(1)));
    }

    private static MoveList collectPrincipalVariation(int depth) {
        long hash = Zobrist.getZobristHash();
        MoveList principal_variation = new MoveList(depth);

        if (!Transposition.doesExist(hash) || depth == 0) {
            return principal_variation;
        }

        int move = Transposition.getBestMove(hash);

        principal_variation.add(move);

        MoveHandler.doMove(move);

        principal_variation.addAll(collectPrincipalVariation(depth - 1));

        MoveHandler.undoMove();

        return principal_variation;
    }

    private static int see(int move) {
        int origin = (move >> 6) & MoveHandler.POSITION_MASK;
        int target = move & MoveHandler.POSITION_MASK;
        int piece = (move >> 12) & MoveHandler.PIECE_MASK;
        int captured = BitBoard.getPieceAt(target);

        int piece_value = Evaluator.PIECE_VALUES[piece & BitBoard.PIECE_MASK];

        // FIXME: Remove Captured Pawn from Occupancy
        int captured_value;
        if ((move & MoveHandler.PASSANT_FLAG) != 0) {
            captured_value = 100; 
        } 
        else {
            captured_value = Evaluator.PIECE_VALUES[captured & BitBoard.PIECE_MASK];
        }
        

        int[] gain = new int[32];
        int depth = 0;
        long occupancy = BitBoard.occupancy_bitboard;
        int turn = piece & BitBoard.COLOR_MASK;
        
        gain[0] = captured_value;
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
            
            gain[depth] = piece_value - gain[depth - 1];
            piece_value = lva_value;
            occupancy &= ~(1L << lva_square);

            attadef |= MoveGenerator.getXRayAttackers(target, occupancy);
        }

        while (--depth > 0) {
            gain[depth - 1] = -Math.max(-gain[depth - 1], gain[depth]);
        }

        return gain[0];
    }

    // Use SEE, MVVLVA, Promotion, Capture
    // Hash Move > Winning Captures > Killer Moves > Losing Captures > Non-Captures
    private static int heuristicScore(int move, int hash_move, int ply) {
        if (move == hash_move) {
            return 10000;
        }
        if (move == KILLER_TABLE[ply][0]) {
            return 5000;
        }
        if (move == KILLER_TABLE[ply][1]) {
            return 4000;
        }

        int origin = (move >> 6) & MoveHandler.POSITION_MASK;
        int target = move & MoveHandler.POSITION_MASK;
        int piece = (move >> 12) & MoveHandler.PIECE_MASK;
        int captured = BitBoard.getPieceAt(target);
        int turn = piece & BitBoard.COLOR_MASK;
        
        int score = 2000;
        score += Evaluator.getPositionalWeight(piece, target);
        score -= Evaluator.getPositionalWeight(piece, origin);

        if (captured != BitBoard.NO_PIECE) {
            // score += 1000;
            // score -= Evaluator.PIECE_VALUES[piece & BitBoard.PIECE_MASK];
            // score += Evaluator.PIECE_VALUES[captured & BitBoard.PIECE_MASK];

            // FIXME: Consider disabling SEE at high depths
            int seeScore = see(move);
            if (seeScore >= 0) {
                score += 5000;
            }
            score += seeScore;
        } else {
            score += HISTORY_TABLE[turn][move & TO_FROM_MASK];
        }

        return score;
    }

    // FIXME: Consider partial sorting
    // FIXME: Max number is 2^31
    private static void sortMoveList(MoveList move_list, int ply) {
        int hash_move = Transposition.getBestMove(Zobrist.getZobristHash());

        long[] scored_moves = new long[move_list.size()];
        for (int i = 0; i < scored_moves.length; i++) {
            int move = move_list.get(i);
            scored_moves[i] = (((long) heuristicScore(move, hash_move, ply)) << 32) | move;
        }

        Arrays.sort(scored_moves);
        move_list.clear();

        for (int i = scored_moves.length - 1; i >= 0; i--) {
            move_list.add((int) scored_moves[i]);
        }
    }

    private static int quiescence(int alpha, int beta, int turn) {
        int static_eval = Evaluator.getRelativeEvaluation(turn);

        if (static_eval >= beta) {
            return static_eval;
        }

        if (alpha < static_eval) {
            alpha = static_eval;
        }

        MoveList noisy_list = MoveGenerator.generateNoisyMoves(turn);
        sortMoveList(noisy_list, 0);

        int best_eval = static_eval;
        for (int i = 0; i < noisy_list.size(); i++) {
            int move = noisy_list.get(i);

            if (see(move) < 0) {
                continue;
            }

            MoveHandler.doMove(move);

            // Verify Pseudo Legality
            if (MoveGenerator.isKingInCheck(turn)) {
                MoveHandler.undoMove();
                continue;
            }

            // Delta Pruning
            if (static_eval + BIG_DELTA < alpha) {
                MoveHandler.undoMove();
                continue;
            }

            int eval = -quiescence(-beta, -alpha, turn ^ 1);

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

        if (ply > 0 && Evaluator.isThreeFoldRepetition(board_hash)) {
            return Evaluator.DRAW_SCORE;
        }

        // Transposition Probe
        if (Transposition.doesExist(board_hash) && Transposition.getDepth(board_hash) >= depth) {
            int tt_score = Transposition.getScore(board_hash, ply);
            if (Transposition.isExact(board_hash)) {
                return tt_score;
            }
            if (Transposition.isBeta(board_hash) && tt_score >= beta) {
                return tt_score;
            }
            if (Transposition.isAlpha(board_hash) && tt_score <= alpha) {
                return tt_score;
            }
        }

        if (depth == 0) {
            node_count++;
            return quiescence(alpha, beta, turn);
        }

        int static_eval = Evaluator.getRelativeEvaluation(turn);
        boolean in_check = MoveGenerator.isKingInCheck(turn);

        // Razoring & Deep Razoring
        if (depth < 3
            && static_eval < alpha - RAZOR_MARGIN[depth]
            && !in_check) {

            node_count++;
            return quiescence(alpha, beta, turn);
        }

        // Reverse Futility Pruning
        if (depth < 3
            && static_eval >= beta + FUTILITY_MARGIN[depth]
            && !in_check) {

            node_count++;
            return quiescence(alpha, beta, turn);
        }

        // Futility Pruning
        // boolean is_futile = false;
        // if (depth < 3
        //     && static_eval < alpha - FUTILITY_MARGIN[depth]
        //     && !in_check) {

        //     is_futile = true;
        // }
        
        // Null Move Pruning
        // FIXME: Maybe don't prune if PV node
        // FIXME: Takes longer to find mates
        // FIXME: Maybe eval can come from the TT
        if (depth >= 3 
            && static_eval >= beta
            && !in_check
            && BitBoard.hasNonPawnPiece(turn)) {
                
            MoveHandler.doNullMove();
            
            int null_eval = -negascout(depth - 3, ply + 1, -beta, -beta + 1, turn ^ 1);
            
            MoveHandler.undoNullMove();
            
            if (null_eval >= beta) {
                node_count++;
                return beta;
            }
        }

        int parent_alpha = alpha;
        int best_score = Integer.MIN_VALUE;
        int best_move = 0;

        MoveList move_list = MoveGenerator.generateAllMoves(turn);
        sortMoveList(move_list, ply);

        for (int i = 0; i < move_list.size(); i++) {
            int move = move_list.get(i);
            boolean is_quiet = BitBoard.getPieceAt(move & MoveHandler.POSITION_MASK, turn ^ 1) == BitBoard.NO_PIECE;

            // Futility Pruning
            // if (i != 0
            //     && is_futile
            //     && is_quiet) {

            //     continue;
            // }

            // Late Move Reduction
            // Consider Precalculating the Logs
            int reduction = 0;
            // if (depth > 2
            //     && i > 3
            //     && is_quiet
            //     && !in_check) {

            //     // reduction = (int) (Math.log(depth) * Math.log(i)) >> 1;
            //     reduction = 1;
            // }

            MoveHandler.doMove(move);

            // Verify Pseudo Legality
            if (MoveGenerator.isKingInCheck(turn)) {
                MoveHandler.undoMove();
                continue;
            }

            // Null Window Search
            int eval;
            if (i == 0) {
                eval = -negascout(depth - 1, ply + 1, -beta, -alpha, turn ^ 1);
            } else {
                eval = -negascout(depth - 1 - reduction, ply + 1, -alpha - 1, -alpha, turn ^ 1);

                if (eval > alpha && eval < beta) {
                    eval = -negascout(depth - 1, ply + 1, -beta, -alpha, turn ^ 1);
                }
            }

            MoveHandler.undoMove();

            if (System.currentTimeMillis() > cancel_time) {
                return Integer.MIN_VALUE;
            }

            if (eval > best_score) {
                best_move = move;
                best_score = eval;
            }

            alpha = Math.max(alpha, eval);

            if (alpha >= beta) {
                if (is_quiet) {
                    int current = HISTORY_TABLE[turn][move & TO_FROM_MASK];
                    int bonus = depth * depth;

                    HISTORY_TABLE[turn][move & TO_FROM_MASK] += bonus - current * bonus / MAX_HISTORY;

                    if (KILLER_TABLE[ply][0] != move) {
                        KILLER_TABLE[ply][1] = KILLER_TABLE[ply][0];
                        KILLER_TABLE[ply][0] = move;
                    }
                }

                break;
            }
        }

        // Checkmate Condition
        if (best_move == 0) {
            return Evaluator.getMateScore(turn, ply);
        }
       
        if ((node_count & HISTORY_AGE) == 0) {
            for (int i = 0; i < TO_FROM_MASK; i++) {
                HISTORY_TABLE[0][i] >>= 1;
                HISTORY_TABLE[1][i] >>= 1;
            }
        }

        byte node_type;
        if (best_score >= beta) {
            node_type = Transposition.BETA_NODE;
        } else if (best_score <= parent_alpha) {
            node_type = Transposition.ALPHA_NODE;
        } else {
            node_type = Transposition.EXACT_NODE;
        }

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

        iterativeDeepener(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
}