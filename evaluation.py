from pieces import *
from information import *

white_length = 0
black_length = 0

def get_state(board):
    key_board = hash_board(board)
    if key_board in past_states:
        return past_states[key_board]
    else:
        return 0

def add_state(board):
    key_board = hash_board(board)
    if key_board in past_states:
        past_states[key_board] += 1
    else:
        past_states[key_board] = 1

def set_possible_length(new, side):
    global white_length
    global black_length
    if side:
        white_length = new
    else:
        black_length = new

def get_possible_length(side):
    if side:
        return white_length
    else:
        return black_length

def get_possible_moves(board, side):
    possible_moves = []
    if side:
        color = PieceColor.WHITE
    else:
        color = PieceColor.BLACK

    for i in range(64):
        piece = board[i // 8][i % 8]

        if piece.get_color() == color:
            piece_pos = piece.get_position()
            for move in piece.get_moves(board):
                possible_moves.append([piece_pos, move])

    return possible_moves

def determine_winner(board, turn, caller=False):
    if caller and get_state(board) + 2 == 3:
        return PieceType.EMPTY
    elif not caller and get_state(board) == 3:
        return PieceType.EMPTY

    king_pos = get_king_pos(turn)
    king_moves = board[king_pos[0]][king_pos[1]].get_moves(board)

    if not king_moves:
        first_length = get_possible_moves(board, turn)
        second_length = get_possible_moves(board, not turn)
        set_possible_length(len(first_length), turn)
        set_possible_length(len(second_length), not turn)

        if not first_length:
            if turn and king_in_check(board, PieceColor.WHITE):
                return PieceColor.BLACK
            elif not turn and king_in_check(board, PieceColor.BLACK):
                return PieceColor.WHITE

            return PieceType.EMPTY

    return PieceColor.NULL

def evaluate_board(board, turn):
    board_state = determine_winner(board, turn, True)
    if board_state == PieceColor.BLACK:
        return -10000
    elif board_state == PieceColor.WHITE:
        return 10000
    elif board_state == PieceType.EMPTY:
        return 0

    white_advantage = 0
    black_advantage = 0

    for i in range(64):
        piece = board[i // 8][i % 8]
        piece_type = piece.get_type()
        if piece_type == PieceType.EMPTY:
            continue

        piece_pos = piece.get_position()
        piece_board = piece_weights[piece_type]
        if piece.get_color() == PieceColor.WHITE:
            white_advantage += piece.get_points() + piece_board[piece_pos[0]][piece_pos[1]]
        else:
            black_advantage += piece.get_points() + piece_board[7 - piece_pos[0]][piece_pos[1]]

    white_advantage += get_possible_length(True) * 5
    black_advantage += get_possible_length(False) * 5

    return white_advantage - black_advantage