from pieces import *

white_length = 0
black_length = 0

past_states = {}

def print_board(game_board):
    piece_chars_white = {
        PieceType.PAWN : "♟",
        PieceType.KNIGHT : "♞",
        PieceType.BISHOP : "♝",
        PieceType.ROOK : "♜",
        PieceType.QUEEN : "♛",
        PieceType.KING : "♚",
        PieceType.EMPTY : " "
    }

    piece_chars_black = {
        PieceType.PAWN: "♙",
        PieceType.KNIGHT: "♘",
        PieceType.BISHOP: "♗",
        PieceType.ROOK: "♖",
        PieceType.QUEEN: "♕",
        PieceType.KING: "♔",
        PieceType.EMPTY: " "
    }

    print("+---+---+---+---+---+---+---+---+")
    for line in game_board:
        game_line = "| "
        for piece in line:
            if piece.get_color() == PieceColor.BLACK:
                game_line += piece_chars_black[piece.get_type()]
            else:
                game_line += piece_chars_white[piece.get_type()]
            game_line += " | "
        print(game_line)
        print("+---+---+---+---+---+---+---+---+")

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
        print("BOOM")
        pos = get_king_pos(board, False)
        print(board[pos[0]][pos[1]].get_moves(board, True))
        print_board(board)
        return 1000
    elif board_state == PieceType.EMPTY:
        return 0

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