from pieces import *

white_length = 0
black_length = 0

past_states = {}

def get_state(board):
    key_board = ""
    # print(past_states.values())
    for row in board:
        for piece in row:
            key_board += str(piece.get_type())
    if key_board in past_states:
        return past_states[key_board]
    else:
        return 0

def add_state(board):
    key_board = ""
    for row in board:
        for piece in row:
            key_board += str(piece.get_type())
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
    # side = True --> White
    # side = False --> Black
    possible_moves = []
    if side:
        color = PieceColor.WHITE
    else:
        color = PieceColor.BLACK

    for i in range(64):
        piece = board[i // 8][i % 8]
        if piece.get_color() == color:
            for move in piece.get_moves(board):
                possible_moves.append([piece.get_position(), move])

    return possible_moves

def determine_winner(board, turn, caller=False):

    # caller = False --> __main__()
    # caller = True --> evaluate_board()
    if caller:
        if get_state(board) + 2 == 3:
            # print("THIS IS THE GREATEST SHOW")
            return PieceType.EMPTY
    else:
        if get_state(board) == 3:
            return PieceType.EMPTY

    king_pos = get_king_pos(board, turn)

    if not board[king_pos[0]][king_pos[1]].get_moves(board):
        first_length = get_possible_moves(board, turn)
        second_length = get_possible_moves(board, not turn)
        set_possible_length(len(first_length), turn)
        set_possible_length(len(second_length), not turn)
        if not first_length:

            if turn:
                if king_in_check(board, PieceColor.WHITE):
                    return PieceColor.BLACK
            else:
                if king_in_check(board, PieceColor.BLACK):
                    return PieceColor.WHITE

            return PieceType.EMPTY

    return PieceColor.NULL

def evaluate_board(board, turn):
    board_state = determine_winner(board, turn, True)
    if board_state == PieceColor.BLACK:
        return -1000
    elif board_state == PieceColor.WHITE:
        return 1000
    elif board_state == PieceType.EMPTY:
        if turn:
            return -100
        else:
            return 100

    white_advantage = 0
    black_advantage = 0

    for i in range(64):
        piece = board[i // 8][i % 8]
        if piece.get_color() == PieceColor.WHITE:
            white_advantage += piece.get_points() * 2
        elif piece.get_color() == PieceColor.BLACK:
            black_advantage += piece.get_points() * 2

    white_advantage += get_possible_length(True)
    black_advantage += get_possible_length(False)

    return white_advantage - black_advantage