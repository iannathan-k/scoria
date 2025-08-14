package src.utils;

import src.game.GameState;

public class GameStack {
    private static GameState[] stack = new GameState[256];
    private static int top_pointer = 0;

    public static void initGameStack() {
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new GameState();
        }
    }

    public static void push(int next_move, int moving_side, int castle_rights, int passant_rights, int captured_piece, long zobrist_hash) {
        GameState state = stack[top_pointer++];
        state.next_move = next_move;
        state.moving_side = moving_side;
        state.castle_rights = castle_rights;
        state.passant_rights = passant_rights;
        state.captured_piece = captured_piece;
        state.zobrist_hash = zobrist_hash;
    }

    public static GameState pop() {
        return stack[--top_pointer];
    }

    public static GameState peek() {
        return stack[top_pointer - 1];
    }
}
