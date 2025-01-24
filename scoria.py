from evaluation import *
from math import inf

transposition_table = {}

def heuristic_ordering(moves, board):
    def move_value(move):
        origin, target = move
        piece = board[origin[0]][origin[1]]
        target_piece = board[target[0]][target[1]]
        value = 0

        if target_piece.get_type() != PieceType.EMPTY:
            value += target_piece.get_points() * 10 - piece.get_points()

        if piece.get_type() == PieceType.PAWN and (target[0] == 0 or target[0] == 7):
            value += 900

        return value

    return sorted(moves, key=move_value, reverse=True)

def move_state(board, origin_pos, target_pos):
    piece = board[origin_pos[0]][origin_pos[1]]
    captured_piece = board[target_pos[0]][target_pos[1]]

    # if en passant
    if piece.get_type() == PieceType.PAWN:
        if target_pos[1] != origin_pos[1]:
            if captured_piece.get_type() == PieceType.EMPTY and board[target_pos[0] - 1][target_pos[1]].get_color() != piece.get_color():
                captured_piece = board[target_pos[0] - 1][target_pos[1]]
                board[target_pos[0]][target_pos[1]] = piece
                board[target_pos[0] - 1][target_pos[1]] = Empty()
                piece.set_position(target_pos)
                return [captured_piece, piece]

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

    # Has moved logic
    if piece.get_type() == PieceType.ROOK or piece.get_type() == PieceType.KING:
        piece.set_shallow_moved(True)

    return [captured_piece, piece]

def undo_move(board, origin_pos, target_pos, board_info):
    piece = board_info[1]

    # If En Passant
    if piece.get_type() == PieceType.PAWN and board_info[0].get_color() != piece.get_color():
        if target_pos[1] != origin_pos[1]:
            if board_info[0].get_position()[0] == piece.get_position()[0] - 1:
                board[origin_pos[0]][origin_pos[1]] = piece
                board[target_pos[0] - 1][target_pos[1]] = board_info[0]
                board[target_pos[0]][target_pos[1]] = Empty()
                piece.set_position(origin_pos)
                return

    board[origin_pos[0]][origin_pos[1]] = piece
    board[target_pos[0]][target_pos[1]] = board_info[0]
    piece.set_position(origin_pos)

    # Undo moved logic
    if piece.get_type() == PieceType.ROOK or piece.get_type() == PieceType.KING:
        if not piece.get_moved()[1]:
            piece.set_shallow_moved(False)

def minimax(board, depth, alpha, beta, turn):
    board_key = hash_board(board)
    if board_key in transposition_table and transposition_table[board_key][0] >= depth:
        return transposition_table[board_key][1]

    if depth == 0 or determine_winner(board, turn) != PieceColor.NULL:
        return [evaluate_board(board, turn), []]

    possible_moves = heuristic_ordering(get_possible_moves(board, turn), board)

    if turn:
        max_eval = [-inf, []]
        for move in possible_moves:
            board_info = move_state(board, move[0], move[1])
            evaluation = minimax(board, depth - 1, alpha, beta, False)[0]
            undo_move(board, move[0], move[1], board_info)
            if evaluation > max_eval[0]:
                max_eval = [evaluation, move]
            alpha = max(alpha, evaluation)
            if beta <= alpha:
                break
        transposition_table[board_key] = [depth, max_eval]
        return max_eval

    else:
        min_eval = [+inf, []]
        for move in possible_moves:
            board_info = move_state(board, move[0], move[1])
            evaluation = minimax(board, depth - 1, alpha, beta, True)[0]
            undo_move(board, move[0], move[1], board_info)
            if evaluation < min_eval[0]:
                min_eval = [evaluation, move]
            beta = min(beta, evaluation)
            if beta <= alpha:
                break
        transposition_table[board_key] = [depth, min_eval]
        return min_eval