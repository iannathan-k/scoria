package src.game;

import java.util.Arrays;
import java.util.Random;

public class Magic {

    private static long findMagicNumber(int square, int bits, long[] moves, long mask) { 
        Random random = new Random();
        long[] used = new long[1 << bits];
        long[] blocker_maps = new long[1 << bits];

        int[] blocker_squares = new int[bits];
        int index = 0;
        for (int i = 0; i < 64; i++) {
            if ((mask & (1L << i)) != 0) {
                blocker_squares[index++] = i;
            }
        }

        for (int i = 0; i < (1 << bits); i++) {
            long blocker_map = 0L;
            for (int j = 0; j < bits; j++) {
                if ((i & (1 << j)) != 0) {
                    blocker_map |= 1L << blocker_squares[j];
                }
            }

            blocker_maps[i] = blocker_map;
        }

        while (true) {
            long magic = random.nextLong() & random.nextLong() & random.nextLong();

            if (Long.bitCount((magic * mask) & 0xFF00000000000000L) < 6) continue;

            Arrays.fill(used, 0L);
            boolean fail = false;

            for (int i = 0; i < blocker_maps.length; i++) {
                int j = (int) ((blocker_maps[i] * magic) >>> (64 - bits));
                if (used[j] == 0L) {
                    used[j] = moves[i];
                } else if (used[j] != moves[i]) {
                    fail = true;
                    break;
                }
            }

            if (!fail) return magic;
        }
    }

    private static boolean inRange(int row, int col) {
        return (row | col) > -1 && (row | col) < 8;
    }

    private static long generateBlockedMap(int square, long blocker_map, int[][] directions) {
        long map = 0L;

        for (int[] dir : directions) {
            int row = (square >> 3) + dir[0];
            int col = (square & 7) + dir[1];

            while (inRange(row, col)) {
                int target = row << 3 | col;
                map |= 1L << target;

                if ((blocker_map & (1L << target)) != 0) {
                    break;
                }

                row += dir[0];
                col += dir[1];
            }
        }

        return map;
    } 

    public static void computeMagics(long[] blocker_masks, int[][] directions) {
        long[] MAGIC_NUMBERS = new long[64];

        for (int square = 0; square < 64; square++) {
            long mask = blocker_masks[square];
            int bits = Long.bitCount(mask);

            long[] moves = new long[1 << bits];
            int[] blocker_squares = new int[bits];

            int index = 0;
            for (int i = 0; i < 64; i++) {
                if ((mask & (1L << i)) != 0) {
                    blocker_squares[index++] = i;
                }
            }

            for (int perm = 0; perm < (1 << bits); perm++) {
                long blocker_map = 0L;
                for (int b = 0; b < bits; b++) {
                    if ((perm & (1 << b)) != 0) {
                        blocker_map |= 1L << blocker_squares[b];
                    }
                }
                moves[perm] = generateBlockedMap(square, blocker_map, directions);
            }

            long magic = findMagicNumber(square, bits, moves, mask);
            MAGIC_NUMBERS[square] = magic;
        }
    }
}