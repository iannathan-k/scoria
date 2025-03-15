package src.pieces;

import java.util.ArrayList;

public abstract class Knight {

    public static ArrayList<Integer> getMoves(byte[] board, int pos) {
        ArrayList<Integer> possible_moves = new ArrayList<Integer>();
        int color = board[pos] & PieceData.COLOR_MASK;

        for (int target : PreComputer.KNIGHT_PREMOVES[pos]) {
            byte target_piece = board[target];
            int move = pos << 8 | target;

            if (target_piece == PieceData.EMPTY) {
                if (!PieceHandler.kingCheck(board, move, color)) {
                    possible_moves.add(move);
                }
            } else if ((target_piece & PieceData.COLOR_MASK) != color) {
                if (!PieceHandler.kingCheck(board, move, color)) {
                    possible_moves.add(move);
                }
            }
        }

        // for (int[] dir : PieceData.KNIGHT_DIRECTIONS) {
        //     int row = (pos >> 3) + dir[0];
        //     int col = (pos & 7) + dir[1];
        //     if (!PieceHandler.inRange(row, col)) {
        //         continue;
        //     }

        //     int target = row << 3 | col;
        //     int move = pos << 8 | target;
        //     byte target_piece = board[target];

        //     if (target_piece == PieceData.EMPTY) {
        //         if (!PieceHandler.kingCheck(board, move, color)) {
        //             possible_moves.add(move);
        //         }
        //     } else if ((target_piece & PieceData.COLOR_MASK) != color) {
        //         if (!PieceHandler.kingCheck(board, move, color)) {
        //             possible_moves.add(move);
        //         }
        //     }
        // }

        return possible_moves;

    }
    
}
