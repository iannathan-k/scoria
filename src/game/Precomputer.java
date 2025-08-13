package src.game;

public class Precomputer {
    public static final long[] KNIGHT_MOVE_TABLE = new long[64]; 
    public static final long[] KING_MOVE_TABLE = new long[64];
    public static final long[][] ROOK_MOVE_TABLE = new long[64][];
    public static final long[][] BISHOP_MOVE_TABLE = new long[64][];

    public static final long[] WHITE_PAWN_ATTACK_TABLE = new long[64];
    public static final long[] BLACK_PAWN_ATTACK_TABLE = new long[64];
    public static final long[] WHITE_PAWN_MOVE_TABLE = new long[64];
    public static final long[] BLACK_PAWN_MOVE_TABLE = new long[64];

    public static final long[] ROOK_MASKS = new long[64];
    public static final long[] ROOK_SHIFTS = new long[64];
    public static final long[] BISHOP_MASKS = new long[64];
    public static final long[] BISHOP_SHIFTS = new long[64];

    public static final long[] ROOK_MAGIC_NUMBERS = {
        36029347315843088L, 630504085275086848L, 144133055307587712L, 2630107164551676032L, 
        72063091730285072L, -9079254649679642352L, 2017613187385657346L, -9079256707036068862L, 
        2738892261956813857L, 9711161582821376L, -6297720845154353118L, 1164743729057234952L, 
        865957800210333824L, 2883711170810741760L, 72340168593506305L, 622763524493410560L, 
        -9187338566865182709L, -7921829270106652672L, 4972047656214863878L, 141287378391040L, 
        4611832253742842112L, -9186217065017442240L, 85713528590172673L, 180146184122339476L, 
        180214946546647552L, 40567582092185600L, 4575072179200000L, 8798248898560L, 
        1155173345224032384L, 35218748870720L, 72339142033408036L, 3026564093717790849L, 
        5800636457563455552L, 6084363371459649536L, -9223231230638546939L, 4521193986074624L, 
        144396731855947776L, 4630828518022841344L, 3463270312488009732L, 4952265068612L, 
        54047732230291456L, 4521329257168896L, 17592722948224L, -8934823901574004728L, 
        1152930304995950596L, 5198842991712796752L, 36108520470609921L, 576461443797483524L, 
        18155136551502080L, 18014742108963904L, 90107177188100224L, 1192190631112867968L, 
        64176329050062976L, 207728601666421248L, 74881148756493312L, 3096226897723648L, 
        74345405004977413L, 145311630706675970L, 3026436691038077442L, 4503743644042497L, 
        564084361957378L, 36591782674579714L, 4611687324106359300L, 7599832965324929L
    };

    public static final long[] BISHOP_MAGIC_NUMBERS = {
        1401191234472771840L, 1297617380859510816L, 1130315137689600L, 4760905697825128448L, 
        577674888288935952L, 581711579586564L, 1229555300470622720L, 586033139258819584L, 
        75440860338585864L, 2937200246857433128L, -4611114270216548352L, 1157781363324092416L, 
        9588914994020896L, 2344194533408899584L, 585613091455246464L, -8935138361076594528L, 
        -3994682681747039232L, 27163503550824576L, 144400580079650320L, 4631952221113745409L, 
        1010360081090347024L, 55450881807614220L, 576742503303225480L, -9222800252103259638L, 
        292738373966169104L, 322157041682435L, 4611835552277217298L, 571746113568904L, 
        72340168670003201L, 29293196878088320L, 1135795918865040L, 595047172576182400L, 
        578998566878472195L, 1334208999016890752L, -9223294795088657919L, 18016599682842754L, 
        1130315133227024L, 9046871867662464L, -8896859547742502784L, 577624611416449281L, 
        18157369926500353L, 1153221676113661956L, 181551910346592256L, 4611687401541257346L, 
        1152925941375308304L, 297239810970485000L, 5647650636693574L, -9200712158759878109L, 
        4764957974907334656L, 4901079819684433928L, 2467974799663055744L, 2558635533271680L, 
        2308103673873565696L, 2323927785124468736L, 306531764385480704L, 653039546750746624L, 
        56331313720066564L, 576533322763866112L, 36033230503415808L, 35734136554530L, 
        40533496430723584L, 4505292382929032L, 2306415176301282337L, 448118075716536576L
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

    private static void generatePawnAttackTable() {
        // White Pawns
        for (int i = 0; i < 64; i++) {
            long attacks = 0L;
            int row = i >> 3;
            int col = i & 7;

            if (col > 0) {
                attacks |= 1L << ((row + 1) << 3 | (col - 1));
            }
            if (col < 7) {
                attacks |= 1L << ((row + 1) << 3 | (col + 1));
            }

            WHITE_PAWN_ATTACK_TABLE[i] = attacks;
        }

        // Black Pawns
        for (int i = 0; i < 64; i++) {
            long attacks = 0L;
            int row = i >> 3;
            int col = i & 7;

            if (col > 0) {
                attacks |= 1L << ((row - 1) << 3 | (col - 1));
            }
            if (col < 7) {
                attacks |= 1L << ((row - 1) << 3 | (col + 1));
            }

            BLACK_PAWN_ATTACK_TABLE[i] = attacks;
        }
    }

    private static void generatePawnMoveTable() {

        // White Pawns
        for (int i = 8; i < 56; i++) {
            long map = 0L;
            int row = i >> 3;
            int col = i & 7;

            map |= 1L << ((row + 1) << 3 | col);
            if (row == 1) {
                map |= 1L << ((row + 2) << 3 | col);
            }

            WHITE_PAWN_MOVE_TABLE[i] = map;
        }

        // Black Pawns
        for (int i = 8; i < 56; i++) {
            long map = 0L;
            int row = i >> 3;
            int col = i & 7;

            map |= 1L << ((row - 1) << 3 | col);
            if (row == 6) {
                map |= 1L << ((row - 2) << 3 | col);
            }

            BLACK_PAWN_MOVE_TABLE[i] = map;
        }
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
            for (int j = 0; j < 64; j++) {
                if ((mask & (1L << j)) != 0) {
                    blocker_squares[index++] = j;
                }
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

            int[] blocker_squares = new int[bits];
            int index = 0;
            for (int j = 0; j < 64; j++) {
                if ((mask & (1L << j)) != 0) {
                    blocker_squares[index++] = j;
                }
            }

            long[] table = new long[permutations];
            for (int j = 0; j < permutations; j++) {
                long blocker_map = 0L;
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
        generatePawnAttackTable();
        generatePawnMoveTable();
    }
}