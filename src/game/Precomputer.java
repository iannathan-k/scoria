package src.game;

public class Precomputer {
    public static final long[] KNIGHT_MOVE_TABLE = new long[64]; 
    public static final long[] KING_MOVE_TABLE = new long[64];
    public static final long[][] ROOK_MOVE_TABLE = new long[64][];
    public static final long[][] BISHOP_MOVE_TABLE = new long[64][];

    public static final long[] ROOK_MASKS = new long[64];
    public static final long[] ROOK_SHIFTS = new long[64];
    public static final long[] BISHOP_MASKS = new long[64];
    public static final long[] BISHOP_SHIFTS = new long[64];

    public static final long[] ROOK_MAGIC_NUMBERS = {
        0x0080008020400010L, 0x08C0002000411000L, 0x020010400A002080L, 0x2480048800500080L, 
        0x0100050008000210L, 0x8200020004880110L, 0x1C00008110440802L, 0x8200002100884402L, 
        0x2602800040008421L, 0x0022804000802000L, 0xA89A004212008022L, 0x102A004020D20008L, 
        0x0C04800800040080L, 0x2805000803000400L, 0x0101010004020001L, 0x08A4802040800100L, 
        0x8080044002C1200BL, 0x9210024020004000L, 0x4500430013002006L, 0x0000808008001000L, 
        0x4000850010080100L, 0x8084004002010040L, 0x0130840008100201L, 0x0280020000411094L, 
        0x0280408A00210200L, 0x0090200040005000L, 0x0010410100102000L, 0x0000080080801000L, 
        0x1008000980140080L, 0x0000200801041040L, 0x0101001100420024L, 0x2A00840200004081L, 
        0x5080002004400040L, 0x5470004000402000L, 0x8000801000802005L, 0x0010100081802800L, 
        0x0201001005004800L, 0x4044020080800400L, 0x3010020001010004L, 0x000004810A001044L, 
        0x00C0042048808000L, 0x0010102000494000L, 0x0000100020008080L, 0x8401210010030008L, 
        0x1000080100110004L, 0x4826002804120050L, 0x0080488210040001L, 0x080000A100420004L, 
        0x0040800021004100L, 0x0040005000200440L, 0x0140200010028880L, 0x108B831000080080L, 
        0x00E4000800048080L, 0x02E2001008040200L, 0x010A080210010400L, 0x000B000080620100L, 
        0x010820C080011505L, 0x0204402882011102L, 0x2A001022C0888202L, 0x0010002188100501L, 
        0x0002010820049002L, 0x0082000850044102L, 0x4000013000880204L, 0x001B000200402081L
    };

    public static final long[] BISHOP_MAGIC_NUMBERS = {
        0x1372080101240100L, 0x1202102200810020L, 0x0004040400442800L, 0x4212228200000000L, 
        0x0804504010202010L, 0x0002111048102004L, 0x1110420804400A00L, 0x0822020904210400L, 
        0x010C051012080108L, 0x28C3081004208028L, 0xC002080081021000L, 0x1011440408800000L, 
        0x0022111140000220L, 0x2088408210400200L, 0x0820840104022080L, 0x84000300411048A0L, 
        0xC890094404101C00L, 0x0060811004008080L, 0x0201039001020610L, 0x4048000104110001L, 
        0x0E05852400A00010L, 0x00C5004880A0010CL, 0x0801004044422088L, 0x8002080903008A0AL, 
        0x0410040008610410L, 0x0001250008080803L, 0x4000880010004012L, 0x0002080004004088L, 
        0x0101010008914001L, 0x00681201E2410880L, 0x0004090018480290L, 0x0842084032010080L, 
        0x0809042100406803L, 0x1284100402880180L, 0x8000464040080201L, 0x0040020080280082L, 
        0x0004040400001010L, 0x0020241500002080L, 0x8488016100440080L, 0x0804228611002101L, 
        0x0040820820864001L, 0x1001110120091004L, 0x0285008024809000L, 0x400001420802C882L, 
        0x1000040904021210L, 0x0420020882000108L, 0x0014108222040046L, 0x8050810A00800223L, 
        0x4220880848043000L, 0x4404222104205008L, 0x2240020120884380L, 0x00091710C2120280L, 
        0x2008081002020800L, 0x2040400204011800L, 0x0441050400920000L, 0x09101002005C4000L, 
        0x00C8210808040204L, 0x08004200A0841000L, 0x0080040840441000L, 0x0000208000840422L, 
        0x0090010010420200L, 0x0010018A20180088L, 0x2002086208020421L, 0x0638090400840900L
    };

    private static final int[][] KNIGHT_DIRECTIONS = {
        {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
        {1, -2}, {-1, -2}, {1, 2}, {-1, 2}
    };

    private static final int[][] ALL_DIRECTIONS = {
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    private static boolean inRange(int row, int col) {
        return (row | col) > -1 && (row | col) < 8;
    }

    private static void generateKnightMoveTable() {
        for (int i = 0; i < 64; i++) {
            long map = 0L;

            for (int[] dir : KNIGHT_DIRECTIONS) {
                int row = (i >> 3) + dir[0];
                int col = (i & 7) + dir[1];

                if (inRange(row, col)) {
                    map |= 1L << (row << 3 | col);
                }
            }

            KNIGHT_MOVE_TABLE[i] = map;
        }
    }

    private static void generateKingMoveTable() {
        for (int i = 0; i < 64; i++) {
            long map = 0L;

            for (int[] dir : ALL_DIRECTIONS) {
                int row = (i >> 3) + dir[0];
                int col = (i & 7) + dir[1];

                if (inRange(row, col)) {
                    map |= 1L << (row << 3 | col);
                }
            }

            KING_MOVE_TABLE[i] = map;
        }
    }

    // Magic Bitboards

    private static long generateRookBlockerMask(int square) {
        int row = square >> 3;
        int col = square & 7;
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

        return mask;
    }

    private static long generateBlockedRookMap(int square, long blocker_map) {
        int row = square >> 3;
        int col = square & 7;
        long moves = 0L;

        for (int r = row + 1; r < 8; r++) {
            long bit = 1L << (r << 3 | col);
            moves |= bit;
            if ((blocker_map & bit) != 0) break;
        }

        for (int r = row - 1; r >= 0; r--) {
            long bit = 1L << (r << 3 | col);
            moves |= bit;
            if ((blocker_map & bit) != 0) break;
        }

        for (int c = col + 1; c < 8; c++) {
            long bit = 1L << (row << 3 | c);
            moves |= bit;
            if ((blocker_map & bit) != 0) break;
        }

        for (int c = col - 1; c >= 0; c--) {
            long bit = 1L << (row << 3 | c);
            moves |= bit;
            if ((blocker_map & bit) != 0) break;
        }

        return moves;
    }

    private static void generateRookMoveTable() {
        for (int i = 0; i < 64; i++) {
            long mask = generateRookBlockerMask(i);
            int bits = Long.bitCount(mask);
            int permutations = 1 << bits;

            ROOK_MASKS[i] = mask;
            ROOK_SHIFTS[i] = 64 - bits;

            int[] blocker_squares = new int[bits];
            int index = 0;
            while (mask != 0L) {
                int square = Long.numberOfTrailingZeros(mask);
                blocker_squares[index++] = square;
                mask &= mask - 1;
            }

            long[] table = new long[permutations];
            for (int j = 0; j < permutations; j++) {
                long blocker_map = 0L;
                for (int k = 0; k < bits; k++) {
                    if ((j & (1 << k)) != 0) {
                        blocker_map |= 1L << blocker_squares[k];
                    }
                }

                int l = (int) ((blocker_map * ROOK_MAGIC_NUMBERS[i]) >>> ROOK_SHIFTS[i]);
                table[l] = generateBlockedRookMap(i, blocker_map);
            }

            ROOK_MOVE_TABLE[i] = table;
        }
    }

    private static long generateBishopBlockerMask(int square) {
        int row = square >> 3;
        int col = square & 7;
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

        return mask;
    }

    private static long generateBlockedBishopMap(int square, long blocker_map) {
        int row = square >> 3;
        int col = square & 7;
        long map = 0L;

        for (int r = row + 1, c = col + 1; r < 8 && c < 8; r++, c++) {
            long bit = 1L << (r << 3 | c);
            map |= bit;
            if ((blocker_map & bit) != 0) break;
        }

        for (int r = row + 1, c = col - 1; r < 8 && c >= 0; r++, c--) {
            long bit = 1L << (r << 3 | c);
            map |= bit;
            if ((blocker_map & bit) != 0) break;
        }

        for (int r = row - 1, c = col + 1; r >= 0 && c < 8; r--, c++) {
            long bit = 1L << (r << 3 | c);
            map |= bit;
            if ((blocker_map & bit) != 0) break;
        }

        for (int r = row - 1, c = col - 1; r >= 0 && c >= 0; r--, c--) {
            long bit = 1L << (r << 3 | c);
            map |= bit;
            if ((blocker_map & bit) != 0) break;
        }

        return map;
    }

    private static void generateBishopMoveTable() {
        for (int i = 0; i < 64; i++) {
            long mask = generateBishopBlockerMask(i);
            int bits = Long.bitCount(mask);
            int permutations = 1 << bits;

            BISHOP_MASKS[i] = mask;
            BISHOP_SHIFTS[i] = 64 - bits;

            // Collect all attacker positions
            int[] blocker_squares = new int[bits];
            int index = 0;
            while (mask != 0L) {
                int square = Long.numberOfTrailingZeros(mask);
                blocker_squares[index++] = square;
                mask &= mask - 1;
            }

            // Loop through every permutation
            long[] table = new long[permutations];
            for (int j = 0; j < permutations; j++) {
                long blocker_map = 0L;

                // Set bits in permutation
                for (int k = 0; k < bits; k++) {
                    if ((j & (1 << k)) != 0) {
                        blocker_map |= 1L << blocker_squares[k];
                    }
                }

                int l = (int) ((blocker_map * BISHOP_MAGIC_NUMBERS[i]) >>> BISHOP_SHIFTS[i]);
                table[l] = generateBlockedBishopMap(i, blocker_map);
            }

            BISHOP_MOVE_TABLE[i] = table;
        }
    }

    public static void initAllMoveTables() {
        generateKnightMoveTable();
        generateKingMoveTable();
        generateRookMoveTable();
        generateBishopMoveTable();
    }
}