package src.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import src.engine.Evaluator;
import src.game.BitBoard;

public class Tuner {
    // File Path
    private static final String PATH = "lichess-quiet.txt";

    // Tuning Paramters
    private static final double K = 0.00663;
    private static final double LEARNING_RATE = 0.15;
    private static final double BETA_1 = 0.9;
    private static final double BETA_2 = 0.999;
    private static final double EPSILON = 1e-8;
    private static final int MAX_EPOCHS = 5000;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    // Paramter Map
    private static final int PIECEVAL_FRONT = 0;
    private static final int PSQT_FRONT = 5;
    private static final int KINGEG_FRONT = 389;
    private static final int SCALAR_FRONT = 453;
    private static final int PASSEDMG_FRONT = 461;
    private static final int PASSEDEG_FRONT = 469;
    private static final int PARAM_COUNT = 477;

    private static final int MISSING_INDEX = 0;
    private static final int CENTER_INDEX = 1;
    private static final int BISHOPMG_INDEX = 2;
    private static final int BISHOPEG_INDEX = 3;
    private static final int FULLOPEN_INDEX = 4;
    private static final int SEMIOPEN_INDEX = 5;
    private static final int ISOLATED_INDEX = 6;
    private static final int DOUBLED_INDEX = 7;

    // Sigmoid Table
    private static final int TABLE_SIZE = 16384;
    private static final double MAX_SIGMOID = 4.0;
    private static final double[] SIGMOID_TABLE = new double[TABLE_SIZE];

    // Arrays
    private double[] weights = new double[PARAM_COUNT];
    private final AtomicBoolean RUNNING = new AtomicBoolean(true);
    private final ArrayList<Entry> DATASET = new ArrayList<Entry>();

    // Constants
    private static final String TAB = "    ";

    private static final int CWHITE = 1;
    private static final int CBLACK = -1;
    private static final int CKING_TYPE = 5;

    private static final int FILE_E = 4;
    private static final int FILE_D = 3;

    private static final long WHITE_SHIELD = BitBoard.ROW_2 | BitBoard.ROW_3;
    private static final long BLACK_SHIELD = BitBoard.ROW_6 | BitBoard.ROW_7;

    private static final long[] NEIGHBOR_COL = {
        BitBoard.COL_B,
        BitBoard.COL_A | BitBoard.COL_C,
        BitBoard.COL_B | BitBoard.COL_D,
        BitBoard.COL_C | BitBoard.COL_E,
        BitBoard.COL_D | BitBoard.COL_F,
        BitBoard.COL_E | BitBoard.COL_G,
        BitBoard.COL_F | BitBoard.COL_H,
        BitBoard.COL_G
    };

    private static final long[] SELF_COL = {
        BitBoard.COL_A,
        BitBoard.COL_B,
        BitBoard.COL_C,
        BitBoard.COL_D,
        BitBoard.COL_E,
        BitBoard.COL_F,
        BitBoard.COL_G,
        BitBoard.COL_H
    };

    private static class Entry {
        short[] mg_feats;
        short[] eg_feats;
        float phase;
        float result;
    }

    private static void initSigmoidTable() {
        for (int i = 0; i < TABLE_SIZE; i++) {
            double x = ((double) i / TABLE_SIZE) * 2 * MAX_SIGMOID - MAX_SIGMOID;
            SIGMOID_TABLE[i] = 1.0 / (1.0 + Math.exp(-K * x));
        }
    }

    private void initWeights() {
        // Piece Values
        for (int i = 0; i < CKING_TYPE; i++) {
            weights[PIECEVAL_FRONT + i] = Evaluator.PIECE_VALUES[i << 1];
        }

        // PSQT
        for (int i = 0; i <= CKING_TYPE; i++) {
            for (int j = 0; j < 64; j++) {
                weights[PSQT_FRONT + i * 64 + j] = Evaluator.POSITIONAL_WEIGHTS[i][j];
            }
        }

        // PSQT King EG
        for (int i = 0; i < 64; i++) {
            weights[KINGEG_FRONT + i] = Evaluator.KINGWEIGHTS_EG[i];
        }

        // Scalars
        weights[SCALAR_FRONT + MISSING_INDEX] = Evaluator.MISSING_PAWN;
        weights[SCALAR_FRONT + CENTER_INDEX] = Evaluator.UNCASTLED_KING;
        weights[SCALAR_FRONT + BISHOPMG_INDEX] = Evaluator.BISHOP_MG;
        weights[SCALAR_FRONT + BISHOPEG_INDEX] = Evaluator.BISHOP_EG;
        weights[SCALAR_FRONT + FULLOPEN_INDEX] = Evaluator.ROOK_OPEN;
        weights[SCALAR_FRONT + SEMIOPEN_INDEX] = Evaluator.ROOK_SEMI;
        weights[SCALAR_FRONT + ISOLATED_INDEX] = Evaluator.ISOLATED_PAWN;
        weights[SCALAR_FRONT + DOUBLED_INDEX] = Evaluator.DOUBLED_PAWN;

        // Passed Pawns
        for (int i = 0; i < 8; i++) {
            weights[PASSEDMG_FRONT + i] = Evaluator.PASSED_MG[i];
            weights[PASSEDEG_FRONT + i] = Evaluator.PASSED_EG[i];
        }
    }

    private static double fastSigmoid(double eval) {
        if (eval <= -MAX_SIGMOID) {
            return 0.0;
        }

        if (eval >= MAX_SIGMOID) {
            return 1.0;
        }

        int i = (int) ((eval + MAX_SIGMOID) * (TABLE_SIZE / (2.0 *  MAX_SIGMOID)));
        return SIGMOID_TABLE[i];
    }

    private short[] toShortArray(ArrayList<Integer> list) {
        short[] array = new short[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).shortValue();
        }

        return array;
    }

    private Entry extractFeatures(float result) {
        Entry e = new Entry();
        e.result = result;
        ArrayList<Integer> mg = new ArrayList<Integer>();
        ArrayList<Integer> eg = new ArrayList<Integer>();

        // PSQT & PieceVals
        for (int i = 0; i < 12; i++) {
            long board = BitBoard.piece_bitboards[i];
            int sign = ((i & BitBoard.COLOR_MASK) == BitBoard.WHITE) ? CWHITE : CBLACK;
            int type = (i & BitBoard.PIECE_MASK) >> 1;

            while (board != 0) {
                int square = Long.numberOfTrailingZeros(board);
                int sq_index = (sign == CWHITE) ? square ^ 56 : square;

                // Exclude Kings
                if (type < CKING_TYPE) {
                    mg.add((PIECEVAL_FRONT + type) * sign);
                    eg.add((PIECEVAL_FRONT + type) * sign);
                }

                mg.add((PSQT_FRONT + type * 64 + sq_index) * sign);

                if (type == CKING_TYPE) {
                    eg.add((KINGEG_FRONT + sq_index) * sign);
                } else {
                    eg.add((PSQT_FRONT + type * 64 + sq_index) * sign);
                }

                board &= board - 1;
            }
        }

        // Scalars
        for (int i = 0; i < 2; i++) {
            int sign = (i == BitBoard.WHITE) ? CWHITE : CBLACK;
            long m_pawns = BitBoard.piece_bitboards[BitBoard.PAWN | i];
            long e_pawns = BitBoard.piece_bitboards[BitBoard.PAWN | (i ^ 1)];

            // Pawn Eval
            long t_pawns = m_pawns;
            while (t_pawns != 0) {
                int square = Long.numberOfTrailingZeros(t_pawns);
                int col = square & 7;

                // Isolated Pawns
                if ((m_pawns & NEIGHBOR_COL[col]) == 0) {
                    mg.add((SCALAR_FRONT + ISOLATED_INDEX) * sign);
                    eg.add((SCALAR_FRONT + ISOLATED_INDEX) * sign);
                }

                // Doubled Pawns
                long d_mask = (i == BitBoard.WHITE)
                    ? SELF_COL[col] & (~0L << (square + 1))
                    : SELF_COL[col] & ~((~0L) << square);

                if ((m_pawns & d_mask) != 0) {
                    mg.add((SCALAR_FRONT + DOUBLED_INDEX) * sign);
                    eg.add((SCALAR_FRONT + DOUBLED_INDEX) * sign);
                }

                // Passed Pawns
                long p_mask = (i == BitBoard.WHITE) 
                    ? (SELF_COL[col] | NEIGHBOR_COL[col]) & (~0L << (square + 1))
                    : (SELF_COL[col] | NEIGHBOR_COL[col]) & ~((~0L) << square);

                if ((e_pawns & p_mask) == 0) {
                    int row = (i == BitBoard.WHITE) ? square >> 3 : 7 - (square >> 3);
                    mg.add((PASSEDMG_FRONT + row) * sign);
                    eg.add((PASSEDEG_FRONT + row) * sign);
                }

                t_pawns &= t_pawns - 1;
            }

            // Rook Files
            long rooks = BitBoard.piece_bitboards[BitBoard.ROOK | i];
            while (rooks != 0) {
                int square = Long.numberOfTrailingZeros(rooks);
                int col = square & 7;

                if ((m_pawns & SELF_COL[col]) == 0) {
                    if ((e_pawns & SELF_COL[col]) == 0) {
                        // Fully Open
                        mg.add((SCALAR_FRONT + FULLOPEN_INDEX) * sign);
                        eg.add((SCALAR_FRONT + FULLOPEN_INDEX) * sign);
                    } else {
                        // Semi Open
                        mg.add((SCALAR_FRONT + SEMIOPEN_INDEX) * sign);
                        eg.add((SCALAR_FRONT + SEMIOPEN_INDEX) * sign);
                    }
                }

                rooks &= rooks - 1;
            }

            // Bishop Pair
            if (Long.bitCount(BitBoard.piece_bitboards[BitBoard.BISHOP | i]) >= 2) {
                mg.add((SCALAR_FRONT + BISHOPMG_INDEX) * sign);
                eg.add((SCALAR_FRONT + BISHOPEG_INDEX) * sign);
            }

            // Kings
            long kings = BitBoard.piece_bitboards[BitBoard.KING | i];
            int k_square = Long.numberOfTrailingZeros(kings);
            int k_col = k_square & 7;

            // King Centrism
            if (k_col == FILE_D || k_col == FILE_E) {
                mg.add((SCALAR_FRONT + CENTER_INDEX) * sign);
            }

            // King Shield
            long shield_mask = (i == BitBoard.WHITE) 
                ? (SELF_COL[k_col] | NEIGHBOR_COL[k_col]) & WHITE_SHIELD
                : (SELF_COL[k_col] | NEIGHBOR_COL[k_col]) & BLACK_SHIELD;

            int missing = Math.max(3 - Long.bitCount(m_pawns & shield_mask), 0);
            for (int j = 0; j < missing; j++) {
                mg.add((SCALAR_FRONT + MISSING_INDEX) * sign);
            }
        }

        e.mg_feats = toShortArray(mg);
        e.eg_feats = toShortArray(eg);
        
        Evaluator.manualEvaluation();
        e.phase = (float) (Math.min(Evaluator.getPhase(), 24) / 24.0);

        return e;
    }

    public void loadData(String path) throws IOException {
        System.out.println("Loading Data...");

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\|");

                if (words.length < 2) {
                    System.out.println("Error loading a position...");
                    continue;
                }

                String fen = words[0];
                String win = words[1].trim();

                float result;
                if (win.equalsIgnoreCase("white")) {
                    result = 1.0f;
                } else if (win.equalsIgnoreCase("black")) {
                    result = 0.0f;
                } else {
                    result = 0.5f;
                }

                BitBoard.initBoardByFen(fen);
                DATASET.add(extractFeatures(result));
            }
        }

        System.out.println("Finished Loading " + DATASET.size() + " Positions...");
    }

    private void printValues() {
        System.out.println("\n// Texel's Tuning Method\n");

        // Scalars
        System.out.println("public static int MISSING_PAWN = " + (int) Math.round(weights[SCALAR_FRONT + MISSING_INDEX]));
        System.out.println("public static int UNCASTLED_KING = " + (int) Math.round(weights[SCALAR_FRONT + CENTER_INDEX]));
        System.out.println("public static int BISHOP_MG = " + (int) Math.round(weights[SCALAR_FRONT + BISHOPMG_INDEX]));
        System.out.println("public static int BISHOP_EG = " + (int) Math.round(weights[SCALAR_FRONT + BISHOPEG_INDEX]));
        System.out.println("public static int ROOK_OPEN = " + (int) Math.round(weights[SCALAR_FRONT + FULLOPEN_INDEX]));
        System.out.println("public static int ROOK_SEMI = " + (int) Math.round(weights[SCALAR_FRONT + SEMIOPEN_INDEX]));
        System.out.println("public static int ISOLATED_PAWN = " + (int) Math.round(weights[SCALAR_FRONT + ISOLATED_INDEX]));
        System.out.println("public static int DOUBLED_PAWN = " + (int) Math.round(weights[SCALAR_FRONT + DOUBLED_INDEX]));
        System.out.println();

        System.out.print("public static int[] PASSED_MG = {");
        for (int i = 0; i < 8; i++) {
            System.out.print((int) Math.round(weights[PASSEDMG_FRONT + i]));
            System.out.print(i == 7 ? "" : ", ");
        }
        System.out.println("};");

        System.out.print("public static int[] PASSED_EG = {");
        for (int i = 0; i < 8; i++) {
            System.out.print((int) Math.round(weights[PASSEDEG_FRONT + i]));
            System.out.print(i == 7 ? "" : ", ");
        }
        System.out.println("};\n");

        // Piece Values
        System.out.println("public static final int[] PIECE_VALUES = {");
        for (int i = 0; i < CKING_TYPE; i++) {
            int value = (int) Math.round(weights[PIECEVAL_FRONT + i]);
            System.out.printf("%s%5d, %6d\n", TAB, value, -value);
        }
        System.out.println(TAB + "20000, -20000");
        System.out.println("};\n");

        // PSQT
        System.out.println("public static final int[][] POSITIONAL_WEIGHTS = {");
        for (int i = 0; i <= CKING_TYPE; i++) {
            System.out.println(TAB + "{");

            for (int j = 0; j < 64; j++) {
                if (j % 8 == 0) {
                    System.out.print(TAB + TAB);
                }

                System.out.printf("%4d, ", (int) Math.round(weights[PSQT_FRONT + i * 64 + j]));

                if (j % 8 == 7) {
                    System.out.println();
                };
            }

            if (i == CKING_TYPE) {
                System.out.println(TAB + "}");
            } else {
                System.out.println(TAB + "},\n");
            }
        }
        System.out.println("};\n");

        // PSQT King EG
        System.out.println("public static final int[] KINGWEIGHTS_EG = {");
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.print(TAB);
            }

            System.out.printf("%4d, ", (int) Math.round(weights[KINGEG_FRONT + i]));

            if (i % 8 == 7) {
                System.out.println();
            }
            
        }
        System.out.println("};");
    }

    public void tune() {
        double[] m = new double[PARAM_COUNT];
        double[] v = new double[PARAM_COUNT];

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int epoch = 1; epoch <= MAX_EPOCHS && RUNNING.get(); epoch++) {
            double[][] local_gradients = new double[THREAD_COUNT][PARAM_COUNT];
            double[] local_error = new double[THREAD_COUNT];
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            int batch_size = DATASET.size() / THREAD_COUNT;

            for (int t = 0; t < THREAD_COUNT; t++) {
                final int THREAD_ID = t;
                final int START = t * batch_size;
                final int END = (t == THREAD_COUNT - 1)
                    ? DATASET.size()
                    : (t + 1) * batch_size;

                executor.submit(() -> {
                    double[] gradients = local_gradients[THREAD_ID];
                    double error = 0;

                    for (int i = START; i < END; i++) {
                        Entry e = DATASET.get(i);
                        double mg = 0;
                        double eg = 0;

                        for (int idx : e.mg_feats) {
                            mg += (idx >= 0) 
                                ? weights[idx]
                                : -weights[-idx];
                        }

                        for (int idx : e.eg_feats) {
                            eg += (idx >= 0)
                                ? weights[idx]
                                : -weights[-idx];
                        }

                        double eval = (mg * e.phase) + (eg * (1.0 - e.phase));
                        double sigmoid = fastSigmoid(eval);
                        double diff = e.result - sigmoid;
                        
                        // Mean Squared Error
                        error += diff * diff;

                        double error_term = diff * sigmoid * (1.0 - sigmoid);
                        double mg_term = error_term * e.phase;
                        double eg_term = error_term * (1.0 - e.phase);
                        for (int idx : e.mg_feats) {
                            if (idx >= 0) {
                                gradients[idx] += mg_term;
                            } else {
                                gradients[-idx] += -mg_term;
                            }
                        }

                        for (int idx : e.eg_feats) {
                            if (idx >= 0) {
                                gradients[idx] += eg_term;
                            } else {
                                gradients[-idx] += -eg_term;
                            }
                        }
                    }

                    local_error[THREAD_ID] = error;
                    latch.countDown();
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                System.out.println("LATCH ERROR...");
                break;
            }

            for (int i = 0; i < PARAM_COUNT; i++) {
                double g = 0;
                for (int t = 0; t < THREAD_COUNT; t++) {
                    g += local_gradients[t][i];
                }

                m[i] = BETA_1 * m[i] + (1 - BETA_1) * g;
                v[i] = BETA_2 * v[i] + (1 - BETA_2) * (g * g);
                weights[i] += LEARNING_RATE * (m[i] / (1 - Math.pow(BETA_1, epoch))) 
                    / (Math.sqrt(v[i] / (1 - Math.pow(BETA_2, epoch))) + EPSILON);
            }

            // Print Updates
            if (epoch % 10 == 0) {
                double total_error = 0;

                for (double error : local_error) {
                    total_error += error;
                }

                System.out.println("Epoch " + epoch + ": MSE = " + total_error / DATASET.size());
            }
        }

        executor.shutdown();
    }

    public void startListener() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (RUNNING.get()) {
                if (scanner.hasNextLine() && scanner.nextLine().equalsIgnoreCase("stop")) {
                    RUNNING.set(false);
                }
            }

            scanner.close();
        }).start();
    }

    public static void main(String[] args) throws IOException {
        initSigmoidTable();
        System.out.println("Type 'stop' to halt execution and print current values");

        Tuner tuner = new Tuner();
        tuner.loadData(PATH);
        tuner.initWeights();
        tuner.startListener();
        tuner.tune();
        tuner.printValues();
    }
}