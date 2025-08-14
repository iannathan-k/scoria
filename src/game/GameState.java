package src.game;

public class GameState {
    public int next_move;
    public int moving_side;
    public int castle_rights;
    public int passant_rights;
    public int captured_piece;
    public long zobrist_hash;
}