from evaluation import *
from math import inf

def move_state(board, origin_pos, target_pos):
    piece = board[origin_pos[0]][origin_pos[1]]
    captured_piece = board[target_pos[0]][target_pos[1]]

    # if promotion
    if piece.get_type() == PieceType.PAWN:
        if target_pos[0] == 7 or target_pos[0] == 0:
            board[origin_pos[0]][origin_pos[1]] = Empty()
            board[target_pos[0]][target_pos[1]] = Queen(target_pos, piece.get_color())
            return [captured_piece, piece]

    # if not promotion
    board[target_pos[0]][target_pos[1]] = piece
    board[origin_pos[0]][origin_pos[1]] = Empty()
    piece.set_position(target_pos)
    return [captured_piece, piece]

def undo_move(board, origin_pos, target_pos, board_info):
    piece = board[target_pos[0]][target_pos[1]]
    board[origin_pos[0]][origin_pos[1]] = board_info[1]
    board[target_pos[0]][target_pos[1]] = board_info[0]
    piece.set_position(origin_pos)

def minimax(board, depth, alpha, beta, turn):
    if depth == 0 or determine_winner(board, turn) != PieceColor.NULL:
        return [evaluate_board(board, turn), []]

    if turn:
        max_eval = [-inf, []]
        for move in get_possible_moves(board, turn):
            board_info = move_state(board, move[0], move[1])
            evaluation = minimax(board, depth - 1, alpha, beta, False)[0]
            undo_move(board, move[0], move[1], board_info)
            if evaluation > max_eval[0]:
                max_eval = [evaluation, move]
            alpha = max(alpha, evaluation)
            if beta <= alpha:
                break
        return max_eval

    else:
        min_eval = [+inf, []]
        for move in get_possible_moves(board, turn):
            board_info = move_state(board, move[0], move[1])
            evaluation = minimax(board, depth - 1, alpha, beta, True)[0]
            undo_move(board, move[0], move[1], board_info)
            if evaluation < min_eval[0]:
                min_eval = [evaluation, move]
            beta = min(beta, evaluation)
            if beta <= alpha:
                break
        return min_eval