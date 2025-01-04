from pieces import *

def determine_winner(board, turn):
    # turn = True --> White
    # turn = False --> Black

    # Black Win - PieceColor.BLACK
    # White Win - PieceColor.WHITE
    # Stalemate - PieceType.EMPTY
    # No Winner - PieceColor.NULL
    white_move = False
    white_check = False
    black_move = False
    black_check = False

    if king_in_check(board, PieceColor.WHITE):
        white_check = True
    if king_in_check(board, PieceColor.BLACK):
        black_check = True

    for i in range(64):
        piece = board[i // 8][i % 8]

        if piece.get_color() == PieceColor.WHITE:
            if piece.get_moves(board):
                white_move = True

        elif piece.get_color() == PieceColor.BLACK:
            if piece.get_moves(board):
                black_move = True

    if white_move == False and turn and white_check:
        return PieceColor.BLACK
    elif black_move == False and not turn and black_check:
        return PieceColor.WHITE

    if white_move == False and turn and not white_check:
        return PieceType.EMPTY
    elif black_move == False and not turn and not black_check:
        return PieceType.EMPTY

    return PieceColor.NULL

def evaluate_board(board, turn):
    board_state = determine_winner(board, turn)
    if board_state == PieceColor.BLACK:
        return -1000
    elif board_state == PieceColor.WHITE:
        return 1000
    elif board_state == PieceType.EMPTY:
        return 0

    white_advantage = 0
    black_advantage = 0

    for i in range(64):
        piece = board[i // 8][i % 8]
        if piece.get_color() == PieceColor.WHITE:
            white_advantage += piece.get_points() * 2
            white_advantage += len(piece.get_moves(board))
        elif piece.get_color() == PieceColor.BLACK:
            black_advantage += piece.get_points() * 2
            black_advantage += len(piece.get_moves(board))

    return white_advantage - black_advantage