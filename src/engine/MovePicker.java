package src.engine;

import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.game.Zobrist;
import src.utils.GameStack;
import src.utils.MoveList;

public class MovePicker {
    private static final int HASHMOVE = 0;
    private static final int GENNOISY = 1;
    private static final int GOODNOISY = 2;
    private static final int KILLER1 = 3;
    private static final int KILLER2 = 4;
    private static final int GENQUIETS = 5;
    private static final int QUIETS = 6;
    private static final int BADNOISY = 7;
    private static final int END = 8;

    private static final int QS_HASHMOVE = 0;
    private static final int QS_GENNOISY = 1;
    private static final int QS_GOODNOISY = 2;
    private static final int QS_END = 3;

    private static final int[] HASH_TABLE = new int[Search.MAX_PLY];
    private static final int[] STAGE_TABLE = new int[Search.MAX_PLY];
    private static final int[] LIST_INDEX = new int[Search.MAX_PLY];
    public static final MoveList[] BADNOISY_LISTS = new MoveList[Search.MAX_PLY];

    public static void initNode(int ply) {
        STAGE_TABLE[ply] = HASHMOVE;
        HASH_TABLE[ply] = MoveHandler.NULL_MOVE;
    }

    public static void initQSNode(int ply) {
        STAGE_TABLE[ply] = QS_HASHMOVE;
        HASH_TABLE[ply] = MoveHandler.NULL_MOVE;
    }

    public static void scoreNoisy(MoveList noisy_list) {
        for (int i = 0; i < noisy_list.size(); i++) {
            int move = noisy_list.getMove(i);

            int target = move & MoveHandler.POSITION_MASK;
            int piece = (move >> 12) & MoveHandler.PIECE_MASK;
            int captured = BitBoard.getPieceAt(target);

            if ((move & MoveHandler.PASSANT_FLAG) != 0) {
                captured = BitBoard.PAWN;
            }

            int score = Search.CAPTURE_TABLE[piece >> 1][captured >> 1][target];
            score += 5 * Evaluator.PIECE_VALUES[captured & BitBoard.PIECE_MASK];
            score -= Evaluator.PIECE_VALUES[piece & BitBoard.PIECE_MASK];

            noisy_list.scoreMove(i, score);
        }
    }

    public static void scoreQuiets(MoveList quiet_list) {
        for (int i = 0; i < quiet_list.size(); i++) {
            int move = quiet_list.getMove(i);

            int target = move & MoveHandler.POSITION_MASK;
            int piece = (move >> 12) & MoveHandler.PIECE_MASK;
            int turn = piece & BitBoard.COLOR_MASK;

            int score = Search.HISTORY_TABLE[turn][move & Search.TOFROM_MASK];

            if (GameStack.hasContinuation(GameStack.CMH_INDEX)) {
                int cmh_move = GameStack.peekMove();
                int cmh_piece = (cmh_move >> 12) & MoveHandler.PIECE_MASK;
                int cmh_target = cmh_move & MoveHandler.POSITION_MASK;

                score += Search.CMH_TABLE[cmh_piece << 6 | cmh_target][piece << 6 | target];
            }

            if (GameStack.hasContinuation(GameStack.FMH_INDEX)) {
                int fmh_move = GameStack.getMoveFromBack(GameStack.FMH_INDEX);
                int fmh_piece = (fmh_move >> 12) & MoveHandler.PIECE_MASK;
                int fmh_target = fmh_move & MoveHandler.POSITION_MASK;

                score += Search.FMH_TABLE[fmh_piece << 6 | fmh_target][piece << 6 | target];
            }

            quiet_list.scoreMove(i, score);
        }
    }

    public static int pickBest(MoveList move_list, int s_index) {
        int b_index = s_index;
        int b_score = move_list.getScore(s_index);

        for (int i = s_index + 1; i < move_list.size(); i++) {
            int score = move_list.getScore(i);

            if (score > b_score) {
                b_index = i;
                b_score = score;
            }
        }

        move_list.swap(s_index, b_index);
        return move_list.getMove(s_index);
    }

    public static int nextQSMove(MoveList move_list, int ply, int turn) {
        while (true) {
            switch(STAGE_TABLE[ply]) {
                case QS_HASHMOVE:
                    STAGE_TABLE[ply] = QS_GENNOISY;

                    long hash = Zobrist.getZobristHash();
                    if (Transposition.doesExist(hash)) {
                        int move = Transposition.getBestMove(hash);
                        long target_mask = 1L << (move & MoveHandler.POSITION_MASK);

                        HASH_TABLE[ply] = move;

                        if ((BitBoard.occupancy_bitboard & target_mask) != 0) {
                            return move;
                        } 

                        if ((move & MoveHandler.PASSANT_FLAG) != 0) {
                            return move;
                        }
                    }
                    break;

                case QS_GENNOISY:
                    STAGE_TABLE[ply] = QS_GOODNOISY;
                    LIST_INDEX[ply] = 0;

                    MoveGenerator.generateNoisyMoves(turn, move_list);
                    scoreNoisy(move_list);
                    break;
                
                case QS_GOODNOISY:
                    if (LIST_INDEX[ply] < move_list.size()) {
                        int move = pickBest(move_list, LIST_INDEX[ply]++);
                        if (move == HASH_TABLE[ply]) {
                            continue;
                        }

                        if (Search.see(move) < 0) {
                            continue;
                        }

                        return move;
                    } else {
                        STAGE_TABLE[ply] = QS_END;
                    }
                    break;

                case QS_END:
                    return MoveHandler.NULL_MOVE;
            }
        }
    }

    public static int nextMove(MoveList move_list, int ply, int turn) {

        // int excluded_move = Search.EXCLUDED_TABLE[ply];
        // int k_ply = (excluded_move == MoveHandler.NULL_MOVE) ? ply : ply - 1;

        while (true) {
            switch (STAGE_TABLE[ply]) {
                case HASHMOVE:
                    STAGE_TABLE[ply] = GENNOISY;
                
                    long hash = Zobrist.getZobristHash();
                    if (Transposition.doesExist(hash)) {
                        HASH_TABLE[ply] = Transposition.getBestMove(hash);

                        return HASH_TABLE[ply];
                    }
                    break;

                case GENNOISY:
                    STAGE_TABLE[ply] = GOODNOISY;
                    BADNOISY_LISTS[ply].clear();
                    LIST_INDEX[ply] = 0;

                    MoveGenerator.generateNoisyMoves(turn, move_list);
                    scoreNoisy(move_list);
                    break;

                case GOODNOISY:
                    if (LIST_INDEX[ply] < move_list.size()) {
                        int move = pickBest(move_list, LIST_INDEX[ply]++);
                        if (move == HASH_TABLE[ply]) {
                            continue;
                        }

                        if (Search.see(move) >= 0) {
                            return move;
                        }

                        BADNOISY_LISTS[ply].add(move);
                    } else {
                        STAGE_TABLE[ply] = KILLER1;
                    }

                    break;

                case KILLER1:
                    STAGE_TABLE[ply] = KILLER2;

                    int first_killer = Search.KILLER_TABLE[ply][Search.FIRST_KILLER];
                    if (first_killer != MoveHandler.NULL_MOVE
                        && first_killer != HASH_TABLE[ply]
                        // && first_killer != excluded_move
                        && MoveGenerator.verifyPseudoKiller(first_killer, turn)) {

                        return first_killer;
                    } 
                    break;

                case KILLER2:
                    STAGE_TABLE[ply] = GENQUIETS;

                    int second_killer = Search.KILLER_TABLE[ply][Search.SECOND_KILLER];
                    if (second_killer != MoveHandler.NULL_MOVE
                        && second_killer != HASH_TABLE[ply]
                        // && second_killer != excluded_move
                        && second_killer != Search.KILLER_TABLE[ply][Search.FIRST_KILLER]
                        && MoveGenerator.verifyPseudoKiller(second_killer, turn)) {

                        return second_killer;
                    }
                    break;

                case GENQUIETS:
                    STAGE_TABLE[ply] = QUIETS;
                    LIST_INDEX[ply] = 0;

                    MoveGenerator.generateQuietMoves(turn, move_list);
                    scoreQuiets(move_list);
                    break;

                case QUIETS:
                    if (LIST_INDEX[ply] < move_list.size()) {
                        int move = pickBest(move_list, LIST_INDEX[ply]++);

                        if (move == HASH_TABLE[ply]) {
                            continue;
                        }

                        // Revisit Killers deffered by verifyPseudoKiller
                        if (move == Search.KILLER_TABLE[ply][Search.FIRST_KILLER] 
                            && MoveGenerator.verifyPseudoKiller(move, turn)) {

                            continue;
                        }

                        if (move == Search.KILLER_TABLE[ply][Search.SECOND_KILLER]
                            && MoveGenerator.verifyPseudoKiller(move, turn)) {

                            continue;
                        }

                        return move;
                    } else {
                        STAGE_TABLE[ply] = BADNOISY;
                        LIST_INDEX[ply] = 0;
                    }
                    break;

                case BADNOISY:
                    if (LIST_INDEX[ply] < BADNOISY_LISTS[ply].size()) {
                        return BADNOISY_LISTS[ply].getMove(LIST_INDEX[ply]++);
                    } else {
                        STAGE_TABLE[ply] = END;
                    }
                    break;

                case END:
                    return MoveHandler.NULL_MOVE;
                    
            }
        }
    }
}
