from evaluation import *
from math import inf

def move_state(board, origin_pos, target_pos):
    new_board = deepcopy(board)
    piece = new_board[origin_pos[0]][origin_pos[1]]
    new_board[target_pos[0]][target_pos[1]] = piece
    new_board[origin_pos[0]][origin_pos[1]] = Empty()
    piece.set_position(target_pos)
    return new_board

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

def minimax(board, depth, alpha, beta, turn):
    if depth == 0 or determine_winner(board, turn) != PieceColor.NULL:
        return [evaluate_board(board, turn), []]

    if turn:
        max_eval = [-inf, []]
        for move in get_possible_moves(board, turn):
            new_board = move_state(board, move[0], move[1])
            evaluation = minimax(new_board, depth - 1, alpha, beta, False)[0]
            if evaluation > max_eval[0]:
                max_eval = [evaluation, move]
            alpha = max(alpha, evaluation)
            if beta <= alpha:
                break
        return max_eval

    else:
        min_eval = [+inf, []]
        for move in get_possible_moves(board, turn):
            new_board = move_state(board, move[0], move[1])
            evaluation = minimax(new_board, depth - 1, alpha, beta, True)[0]
            if evaluation < min_eval[0]:
                min_eval = [evaluation, move]
            beta = min(beta, evaluation)
            if beta <= alpha:
                break
        return min_eval