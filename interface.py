from pieces import PieceType

def pos_to_square(pos):
    return chr(97 + pos[1]) + str(7 - pos[0] + 1)

def square_to_pos(square):
    return [7 - int(square[1]) + 1, ord(square[0]) - 97]

def move_to_uci(origin_pos, target_pos, board):
    if board[origin_pos[0]][origin_pos[1]].get_type() == PieceType.PAWN:
        if target_pos[0] == 0 or target_pos[0] == 7:
            return pos_to_square(origin_pos) + pos_to_square(target_pos) + "q"

    return pos_to_square(origin_pos) + pos_to_square(target_pos)

def uci_to_move(uci):
    return [square_to_pos(uci[0:2]), square_to_pos(uci[2:4])]