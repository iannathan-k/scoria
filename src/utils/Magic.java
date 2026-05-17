package src.utils;

import java.util.Arrays;
import java.util.Random;

public class Magic {

    private static final long MAGIC_MASK = 0xFF00000000000000L;
    private static final String TAB = "    ";
    private static final String BISHOP_HEADER = "public static final long[] BISHOP_MAGIC_NUMBERS = {";
    private static final String ROOK_HEADER = "public static final long[] ROOK_MAGIC_NUMBERS = {";

    private static final int[][] BISHOP_DIRECTIONS = {
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private static final int[][] ROOK_DIRECTIONS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    private static long[] generateRookMasks() {
        long[] masks = new long[64];

        for (int i = 0; i < 64; i++) {
            int row = i >> 3;
            int col = i & 7;
            long mask = 0L;

            for (int r = row + 1; r < 7; r++) {
                mask |= 1L << (r << 3 | col);
            }

            for (int r = row - 1; r > 0; r--) {
                mask |= 1L << (r << 3 | col);
            }

            for (int c = col + 1; c < 7; c++) {
                mask |= 1L << (row << 3 | c);
            }

            for (int c = col - 1; c > 0; c--) {
                mask |= 1L << (row << 3 | c);
            }

            masks[i] = mask;
        }

        return masks;
    }

    private static long[] generateBishopMasks() {
        long[] masks = new long[64];

        for (int i = 0; i < 64; i++) {
            int row = i >> 3;
            int col = i & 7;
            long mask = 0L;

            for (int r = row + 1, c = col + 1; r < 7 && c < 7; r++, c++) {
                mask |= 1L << (r << 3 | c);
            }

            for (int r = row + 1, c = col - 1; r < 7 && c > 0; r++, c--) {
                mask |= 1L << (r << 3 | c);
            }

            for (int r = row - 1, c = col + 1; r > 0 && c < 7; r--, c++) {
                mask |= 1L << (r << 3 | c);
            }

            for (int r = row - 1, c = col - 1; r > 0 && c > 0; r--, c--) {
                mask |= 1L << (r << 3 | c);
            }

            masks[i] = mask;
        }

        return masks;
    }

    private static boolean inRange(int row, int col) {
        return (row | col) > -1 && (row | col) < 8;
    }

    private static long generateXrayMap(int square, long occupancy, int[][] directions) {
        long attacks = 0L;

        for (int[] dir : directions) {
            int row = (square >> 3) + dir[0];
            int col = (square & 7) + dir[1];

            while (inRange(row, col)) {
                int target = row << 3 | col;
                attacks |= 1L << target;

                // Iterate until xray is blocked
                if ((occupancy & (1L << target)) != 0) {
                    break;
                }

                row += dir[0];
                col += dir[1];
            }
        }

        return attacks;
    }

    private static long findMagic(int bits, long[] occupancies, long[] attacks, long mask) {
        Random random = new Random();
        long[] used_attacks = new long[1 << bits];
        int shift = 64 - bits;

        while (true) {
            // Increase 1 Bits
            long magic = random.nextLong() & random.nextLong() & random.nextLong();

            // Prune Low Quality Magics
            if (Long.bitCount((magic * mask) & MAGIC_MASK) < 6) {
                continue;
            }

            Arrays.fill(used_attacks, 0L);
            boolean fail = false;

            for (int i = 0; i < occupancies.length; i++) {
                int index = (int) ((occupancies[i] * magic) >>> shift);

                // Map Attacks To Move Table
                if (used_attacks[index] == 0L) {
                    used_attacks[index] = attacks[i];
                } else if (used_attacks[index] != attacks[i]) {
                    fail = true;
                    break;
                }
            }

            if (!fail) {
                return magic;
            }
        }
    }

    public static long[] computeMagics(long[] blocker_masks, int[][] directions) {
        long[] magics = new long[64];

        for (int i = 0; i < 64; i++) {
            long mask = blocker_masks[i];
            int bits = Long.bitCount(mask);

            // 2^n Possible Arrangements
            int permutations = 1 << bits;
            long[] occupancies = new long[permutations];
            long[] attacks = new long[permutations];

            // Index Blocking Squares
            int[] blocker_squares = new int[bits];
            int bit_index = 0;
            for (int j = 0; j < 64; j++) {
                if ((mask & (1L << j)) != 0) {
                    blocker_squares[bit_index++] = j;
                }
            }

            // Go Through Every Blocker Permutation
            for (int j = 0; j < permutations; j++) {
                long occupancy = 0L;
                for (int k = 0; k < bits; k++) {

                    // If bit K is turned on in permutation J
                    if ((j & (1 << k)) != 0) {
                        occupancy |= 1L << blocker_squares[k];
                    }
                }

                occupancies[j] = occupancy;
                attacks[j] = generateXrayMap(i, occupancy, directions);
            }

            magics[i] = findMagic(bits, occupancies, attacks, mask);
        }

        return magics;
    }

    public static void printMagics(long[] magics, String header) {
        System.out.println("\n" + header);

        for (int i = 0; i < 64; i++) {
            if (i % 4 == 0) {
                System.out.print(TAB);
            }

            System.out.printf("0x%016XL", magics[i]);

            if (i != 63) {
                System.out.print(", ");
            }

            if (i % 4 == 3) {
                System.out.println();
            }
        }
        System.out.println("};");
    }

    public static void main(String[] args) {
        System.out.println("Computing Rook Magics...");
        long[] rook_masks = generateRookMasks();
        long[] rook_magics = computeMagics(rook_masks, ROOK_DIRECTIONS);

        System.out.println("Computing Bishop Magics...");
        long[] bishop_masks = generateBishopMasks();
        long[] bishop_magics = computeMagics(bishop_masks, BISHOP_DIRECTIONS);

        printMagics(rook_magics, ROOK_HEADER);
        printMagics(bishop_magics, BISHOP_HEADER);
    }
}