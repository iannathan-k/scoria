from evaluation import *
from math import inf

def move_state(board, origin_pos, target_pos):
    piece = board[origin_pos[0]][origin_pos[1]]
    captured_piece = board[target_pos[0]][target_pos[1]]
    board[target_pos[0]][target_pos[1]] = piece
    board[origin_pos[0]][origin_pos[1]] = Empty()

    if origin_pos == target_pos:
        print("OOPS")
        print("OLD: ", str(origin_pos))
        print("NEW: ", str(target_pos))

    new_board = [row[:] for row in board]
    # print("OLD: ", str(get_king_pos(board, False)))
    # print("NEW: ", str(get_king_pos(new_board, False)))
    # print(new_board)

    board[origin_pos[0]][origin_pos[1]] = piece
    board[target_pos[0]][target_pos[1]] = captured_piece

    return new_board

def minimax(board, depth, alpha, beta, turn):
    if depth == 0 or determine_winner(board, turn) != PieceColor.NULL:
        # evaluation = evaluate_board(board, turn)
        # if turn and evaluation < 20:
        #     return [evaluate_board(board, turn), []]
        # elif not turn and evaluation > -20:
        #     return [evaluate_board(board, turn), []]
        # else:
        #     depth += 1

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