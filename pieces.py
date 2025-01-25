from information import *

hit_count = 0
searched_count = 0
king_pieces = [None, None]
past_states = {}
past_moves = {}
current_count = 0

def set_current_count():
    global current_count
    current_count += 1

def get_king_pos(side):
    if side:
        return king_pieces[0].get_position()
    else:
        return king_pieces[1].get_position()

def get_hit_count():
    global hit_count
    temp_count = hit_count
    hit_count = 0
    return temp_count

def get_search_count():
    global searched_count
    temp_count = searched_count
    searched_count = 0
    return temp_count

def in_range(position):
    global hit_count
    hit_count += 1
    return 0 <= position[0] <= 7 and 0 <= position[1] <= 7

def king_in_check(board, color):
    if color == PieceColor.WHITE:
        king_pos = get_king_pos(True)
    else:
        king_pos = get_king_pos(False)

    directions = [
        [-1, 0],  # N
        [1, 0],  # S
        [0, 1],  # E
        [0, -1],  # W
        [1, 1],  # SE
        [1, -1],  # SW
        [-1, 1],  # NE
        [-1, -1]  # NW
    ]

    movesK = [
        [king_pos[0] + 2, king_pos[1] + 1],
        [king_pos[0] + 2, king_pos[1] - 1],
        [king_pos[0] - 2, king_pos[1] + 1],
        [king_pos[0] - 2, king_pos[1] - 1],
        [king_pos[0] + 1, king_pos[1] - 2],
        [king_pos[0] - 1, king_pos[1] - 2],
        [king_pos[0] + 1, king_pos[1] + 2],
        [king_pos[0] - 1, king_pos[1] + 2]
    ]

    movesP = [
        [king_pos[0] + 1, king_pos[1] - 1],
        [king_pos[0] + 1, king_pos[1] + 1],
        [king_pos[0] - 1, king_pos[1] - 1],
        [king_pos[0] - 1, king_pos[1] + 1]
    ]

    def sliding_piece(dirs, attack_pieces):
        for direction in dirs:
            current_move = [king_pos[0] + direction[0], king_pos[1] + direction[1]]
            while in_range(current_move):
                square = board[current_move[0]][current_move[1]]
                if square.get_type() == PieceType.EMPTY:
                    current_move = [current_move[0] + direction[0], current_move[1] + direction[1]]
                    continue
                if square.get_color() == color:
                    break
                if square.get_type() in attack_pieces:
                    return True
                break

        return False

    # Rook Capture
    if sliding_piece(directions[:4], {PieceType.ROOK, PieceType.QUEEN}): return True

    # Bishop Capture
    if sliding_piece(directions[4:], {PieceType.BISHOP, PieceType.QUEEN}): return True

    # Knight Capture
    for move in movesK:
        if not in_range(move):
            continue
        square = board[move[0]][move[1]]
        if square.get_color() == color:
            continue
        if square.get_type() == PieceType.KNIGHT:
            return True

    def pawn_piece(moves, attack_color):

        for move in moves:
            if not in_range(move):
                continue
            square = board[move[0]][move[1]]
            if square.get_type() != PieceType.PAWN:
                continue
            if square.get_color() == attack_color:
                return True

        return False

    # Pawn Capture
    if color == PieceColor.BLACK:
        if pawn_piece(movesP[:2], PieceColor.WHITE): return True
    else:
        if pawn_piece(movesP[2:], PieceColor.BLACK): return True

    # King Capture
    for direction in directions:
        current_move = [king_pos[0] + direction[0], king_pos[1] + direction[1]]
        if not in_range(current_move):
            continue
        if board[current_move[0]][current_move[1]].get_type() == PieceType.KING:
            return True

    return False

def king_check(board, origin_pos, target_pos, color):

    global searched_count
    searched_count += 1

    if board[target_pos[0]][target_pos[1]].get_type() == PieceType.KING:
        return True

    piece = board[origin_pos[0]][origin_pos[1]]
    captured_piece = board[target_pos[0]][target_pos[1]]

    if piece.get_type() == PieceType.PAWN:
        if target_pos[0] == 7 or target_pos[0] == 0:
            board[origin_pos[0]][origin_pos[1]] = Empty()
            board[target_pos[0]][target_pos[1]] = Queen(target_pos, piece.get_color())

            result = king_in_check(board, color)

            board[origin_pos[0]][origin_pos[1]] = piece
            board[target_pos[0]][target_pos[1]] = captured_piece

            return result

    board[target_pos[0]][target_pos[1]] = piece
    board[origin_pos[0]][origin_pos[1]] = Empty()
    piece.set_position(target_pos)

    result = king_in_check(board, color)

    board[origin_pos[0]][origin_pos[1]] = piece
    board[target_pos[0]][target_pos[1]] = captured_piece
    piece.set_position(origin_pos)

    return result

class Empty:
    def __init__(self):
        self._type = PieceType.EMPTY
        self._color = PieceColor.NULL

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

class Pawn:
    def __init__(self, position, color, direction):
        self._position = position
        self._type = PieceType.PAWN
        self._color = color
        self._direction = direction # +1 downward, -1 upward
        self._points = 100
        self._passant = [False, False] # Left, Right
        self._count = 0

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

    def get_direction(self):
        return self._direction

    def get_points(self):
        return self._points

    def set_position(self, position):
        self._position = position

    def set_passant(self, passant, index):
        self._passant[index] = passant

    def get_passant(self):
        return self._passant

    def set_count(self):
        self._count = current_count

    def get_moves(self, board):
        board_hash = hash_board(board)
        if board_hash in past_moves:
            if self in past_moves[board_hash]:
                return past_moves[board_hash][self]

        possible_moves = []
        moves = [
            [self._position[0] + self._direction, self._position[1]], # advance 1
            [self._position[0] + self._direction * 2, self._position[1]], # advance 2
            [self._position[0] + self._direction, self._position[1] + 1], # capture right
            [self._position[0] + self._direction, self._position[1] - 1] # capture left
        ]

        # Left Passant
        if self._passant[0] and self._count + 1 == current_count:
            possible_moves.append(moves[3])

        # Right Passant
        if self._passant[1] and self._count + 1 == current_count:
            possible_moves.append(moves[2])

        if in_range(moves[0]):
            if board[moves[0][0]][moves[0][1]].get_type() == PieceType.EMPTY:
                if not king_check(board, self._position, moves[0], self._color):
                    possible_moves.append(moves[0])

        if in_range(moves[1]):
            if board[moves[1][0]][moves[1][1]].get_type() == PieceType.EMPTY:
                if possible_moves and (self._position[0] == 6 or self._position[0] == 1):
                    if not king_check(board, self._position, moves[1], self._color):
                        possible_moves.append(moves[1])

        for move in moves[2:4]:
            if not in_range(move):
                continue
            if board[move[0]][move[1]].get_type() == PieceType.EMPTY:
                continue
            if board[move[0]][move[1]].get_color() == self._color:
                continue
            if not king_check(board, self._position, move, self._color):
                possible_moves.append(move)

        if board_hash in past_moves:
            past_moves[board_hash][self] = possible_moves
        else:
            past_moves[board_hash] = {board_hash: possible_moves}

        return possible_moves

class Knight:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.KNIGHT
        self._color = color
        self._points = 320

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

    def get_points(self):
        return self._points

    def set_position(self, position):
        self._position = position

    def get_moves(self, board):
        board_hash = hash_board(board)
        if board_hash in past_moves:
            if self in past_moves[board_hash]:
                return past_moves[board_hash][self]

        possible_moves = []
        moves = [
            [self._position[0] + 2, self._position[1] + 1],
            [self._position[0] + 2, self._position[1] - 1],
            [self._position[0] - 2, self._position[1] + 1],
            [self._position[0] - 2, self._position[1] - 1],
            [self._position[0] + 1, self._position[1] - 2],
            [self._position[0] - 1, self._position[1] - 2],
            [self._position[0] + 1, self._position[1] + 2],
            [self._position[0] - 1, self._position[1] + 2],
        ]

        for move in moves:
            if not in_range(move):
                continue
            if board[move[0]][move[1]].get_color() == self._color:
                continue
            if not king_check(board, self._position, move, self._color):
                possible_moves.append(move)

        if board_hash in past_moves:
            past_moves[board_hash][self] = possible_moves
        else:
            past_moves[board_hash] = {board_hash: possible_moves}

        return possible_moves

class Bishop:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.BISHOP
        self._color = color
        self._points = 330

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

    def get_points(self):
        return self._points

    def set_position(self, position):
        self._position = position

    def get_moves(self, board):
        board_hash = hash_board(board)
        if board_hash in past_moves:
            if self in past_moves[board_hash]:
                return past_moves[board_hash][self]

        possible_moves = []

        directions = [
            [1, 1], # SE
            [1, -1], # SW
            [-1, 1], # NE
            [-1, -1] # NW
        ]

        for direction in directions:
            current_move = [self._position[0] + direction[0], self._position[1] + direction[1]]
            while in_range(current_move):
                if board[current_move[0]][current_move[1]].get_color() == self._color:
                    break
                elif board[current_move[0]][current_move[1]].get_type() == PieceType.EMPTY:
                    if not king_check(board, self._position, current_move, self._color):
                        possible_moves.append(current_move)
                else:
                    if not king_check(board, self._position, current_move, self._color):
                        possible_moves.append(current_move)
                    break

                current_move = [current_move[0] + direction[0], current_move[1] + direction[1]]

        if board_hash in past_moves:
            past_moves[board_hash][self] = possible_moves
        else:
            past_moves[board_hash] = {board_hash: possible_moves}

        return possible_moves

class Rook:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.ROOK
        self._color = color
        self._points = 500
        self._moved = [True, True]

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

    def get_points(self):
        return self._points

    def set_position(self, position):
        self._position = position

    def set_deep_moved(self, move):
        self._moved[1] = move

    def set_shallow_moved(self, move):
        self._moved[0] = move

    def get_moved(self):
        return self._moved

    def get_moves(self, board):
        board_hash = hash_board(board)
        if board_hash in past_moves:
            if self in past_moves[board_hash]:
                return past_moves[board_hash][self]

        possible_moves = []

        directions = [
            [-1, 0], # N
            [1, 0], # S
            [0, 1], # E
            [0, -1] # W
        ]

        for direction in directions:
            current_move = [self._position[0] + direction[0], self._position[1] + direction[1]]
            while in_range(current_move):
                if board[current_move[0]][current_move[1]].get_color() == self._color:
                    break
                elif board[current_move[0]][current_move[1]].get_type() == PieceType.EMPTY:
                    if not king_check(board, self._position, current_move, self._color):
                        possible_moves.append(current_move)
                else:
                    if not king_check(board, self._position, current_move, self._color):
                        possible_moves.append(current_move)
                    break

                current_move = [current_move[0] + direction[0], current_move[1] + direction[1]]

        if board_hash in past_moves:
            past_moves[board_hash][self] = possible_moves
        else:
            past_moves[board_hash] = {board_hash: possible_moves}

        return possible_moves

class Queen:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.QUEEN
        self._color = color
        self._points = 900

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

    def get_points(self):
        return self._points

    def set_position(self, position):
        self._position = position

    def get_moves(self, board):
        board_hash = hash_board(board)
        if board_hash in past_moves:
            if self in past_moves[board_hash]:
                return past_moves[board_hash][self]

        possible_moves = []

        directions = [
            [-1, 0], # N
            [1, 0], # S
            [0, 1], # E
            [0, -1], # W
            [1, 1], # SE
            [1, -1], # SW
            [-1, 1], # NE
            [-1, -1] # NW
        ]

        for direction in directions:
            current_move = [self._position[0] + direction[0], self._position[1] + direction[1]]
            while in_range(current_move):
                if board[current_move[0]][current_move[1]].get_color() == self._color:
                    break
                elif board[current_move[0]][current_move[1]].get_type() == PieceType.EMPTY:
                    if not king_check(board, self._position, current_move, self._color):
                        possible_moves.append(current_move)
                else:
                    if not king_check(board, self._position, current_move, self._color):
                        possible_moves.append(current_move)
                    break

                current_move = [current_move[0] + direction[0], current_move[1] + direction[1]]

        if board_hash in past_moves:
            past_moves[board_hash][self] = possible_moves
        else:
            past_moves[board_hash] = {board_hash: possible_moves}

        return possible_moves

class King:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.KING
        self._color = color
        self._points = 10
        self._moved = [True, True]

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

    def get_points(self):
        return self._points

    def set_position(self, position):
        self._position = position

    def set_deep_moved(self, move):
        self._moved[1] = move

    def set_shallow_moved(self, move):
        self._moved[0] = move

    def get_moved(self):
        return self._moved

    def get_moves(self, board):
        board_hash = hash_board(board)
        if board_hash in past_moves:
            if self in past_moves[board_hash]:
                return past_moves[board_hash][self]

        possible_moves = []

        directions = [
            [-1, 0], # N
            [1, 0], # S
            [0, 1], # E
            [0, -1], # W
            [1, 1], # SE
            [1, -1], # SW
            [-1, 1], # NE
            [-1, -1] # NW
        ]

        for direction in directions:
            current_move = [self._position[0] + direction[0], self._position[1] + direction[1]]
            if not in_range(current_move):
                continue
            if board[current_move[0]][current_move[1]].get_color() == self._color:
                continue
            if not king_check(board, self._position, current_move, self._color):
                possible_moves.append(current_move)

        castle_squares = [
            [0, -1],
            [0, -2],
            [0, -3],
            [0, 1],
            [0, 2]
        ]

        def king_castle(rook_offset, castle_list):
            if not in_range([self._position[0], self._position[1] + rook_offset]):
                return
            piece = board[self._position[0]][self._position[1] + rook_offset]
            if piece.get_type() != PieceType.ROOK:
                return
            if piece.get_moved()[0] or self._moved[0]:
                return
            for square in castle_list:
                new_pos = [self._position[0] + square[0], self._position[1] + square[1]]
                if board[new_pos[0]][new_pos[1]].get_type() != PieceType.EMPTY:
                    return
                if king_check(board, self._position, new_pos, self._color):
                    return
            possible_moves.append([self._position[0], self._position[1] + 2])

        if not king_in_check(board, self._color):
            king_castle(3, castle_squares[3:])
            king_castle(-4, castle_squares[:3])

        if board_hash in past_moves:
            past_moves[board_hash][self] = possible_moves
        else:
            past_moves[board_hash] = {board_hash: possible_moves}

        return possible_moves