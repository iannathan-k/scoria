from enum import Enum

hit_count = 0
searched_count = 0
king_pieces = [None, None]
past_states = {}
past_moves = {}
# current_count = 0
#
# def set_current_count():
#     global current_count
#     current_count += 1

def hash_board(board):
    return hash(tuple(tuple(row) for row in board))

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

    directionsR = [
        [-1, 0],  # N
        [1, 0],  # S
        [0, 1],  # E
        [0, -1],  # W
    ]

    directionsB = [
        [1, 1],  # SE
        [1, -1],  # SW
        [-1, 1],  # NE
        [-1, -1]  # NW
    ]

    # Rook Capture
    for direction in directionsR:
        current_move = [king_pos[0] + direction[0], king_pos[1] + direction[1]]
        while in_range(current_move):
            square = board[current_move[0]][current_move[1]]
            if square.get_type() != PieceType.EMPTY:
                if square.get_color() == color:
                    break
                if square.get_color() != color:
                    if square.get_type() == PieceType.ROOK:
                        return True
                    elif square.get_type() == PieceType.QUEEN:
                        return True
                    else:
                        break

            current_move = [current_move[0] + direction[0], current_move[1] + direction[1]]

    # Bishop Capture
    for direction in directionsB:
        current_move = [king_pos[0] + direction[0], king_pos[1] + direction[1]]
        while in_range(current_move):
            square = board[current_move[0]][current_move[1]]
            if square.get_type() != PieceType.EMPTY:
                if square.get_color() == color:
                    break
                if square.get_color() != color:
                    if square.get_type() == PieceType.BISHOP:
                        return True
                    elif square.get_type() == PieceType.QUEEN:
                        return True
                    else:
                        break

            current_move = [current_move[0] + direction[0], current_move[1] + direction[1]]

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

    # Knight Capture
    for move in movesK:
        if in_range(move):
            square = board[move[0]][move[1]]
            if square.get_color() != color:
                if square.get_type() == PieceType.KNIGHT:
                    return True

    movesPB = [
        [king_pos[0] + 1, king_pos[1] - 1],
        [king_pos[0] + 1, king_pos[1] + 1]
    ]

    movesPW = [
        [king_pos[0] - 1, king_pos[1] - 1],
        [king_pos[0] - 1, king_pos[1] + 1]
    ]

    # Pawn Capture
    if color == PieceColor.BLACK:
        for move in movesPB:
            if in_range(move):
                square = board[move[0]][move[1]]
                if square.get_type() == PieceType.PAWN:
                    if square.get_color() == PieceColor.WHITE:
                        return True

    if color == PieceColor.WHITE:
        for move in movesPW:
            if in_range(move):
                square = board[move[0]][move[1]]
                if square.get_type() == PieceType.PAWN:
                    if square.get_color() == PieceColor.BLACK:
                        return True

    # King Capture
    for direction in directionsB + directionsR:
        current_move = [king_pos[0] + direction[0], king_pos[1] + direction[1]]
        if in_range(current_move):
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

class PieceType(Enum):
    EMPTY = 0
    PAWN = 1
    KNIGHT = 2
    BISHOP = 3
    ROOK = 4
    QUEEN = 5
    KING = 6

class PieceColor(Enum):
    NULL = 0
    BLACK = 1
    WHITE = 2

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
        # self._passant = [False, False]
        # self._count = 0

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

    # def set_passant(self, passant, index):
    #     self._passant[index] = passant
    #
    # def get_deep_passant(self):
    #     return self._passant[0]
    #
    # def set_count(self):
    #     self._count = current_count

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

        # if self._passant[1] and self._count + 1 == current_count:
        #     move = [self._position[0] + self._direction, self._position[1] + 2]
        #     possible_moves.append(move)
        #
        # if self._passant[1] and self._count + 1 == current_count:
        #     move = [self._position[0] + self._direction, self._position[1] - 2]
        #     possible_moves.append(move)

        if in_range(moves[0]):
            if board[moves[0][0]][moves[0][1]].get_type() == PieceType.EMPTY:
                if not king_check(board, self._position, moves[0], self._color):
                    possible_moves.append(moves[0])

        if in_range(moves[1]):
            if board[moves[1][0]][moves[1][1]].get_type() == PieceType.EMPTY:
                if possible_moves and (self._position[0] == 6 or self._position[0] == 1):
                    if not king_check(board, self._position, moves[1], self._color):
                        possible_moves.append(moves[1])

        if in_range(moves[2]):
            if board[moves[2][0]][moves[2][1]].get_type() != PieceType.EMPTY:
                if board[moves[2][0]][moves[2][1]].get_color() != self._color:
                    if not king_check(board, self._position, moves[2], self._color):
                        possible_moves.append(moves[2])

        if in_range(moves[3]):
            if board[moves[3][0]][moves[3][1]].get_type() != PieceType.EMPTY:
                if board[moves[3][0]][moves[3][1]].get_color() != self._color:
                    if not king_check(board, self._position, moves[3], self._color):
                        possible_moves.append(moves[3])

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
            if in_range(move):
                if board[move[0]][move[1]].get_color() != self._color:
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
                if current_move != self._position:
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
            if in_range(current_move):
                if board[current_move[0]][current_move[1]].get_color() != self._color:
                    if not king_check(board, self._position, current_move, self._color):
                        possible_moves.append(current_move)

        castle_long = [
            [0, -1],
            [0, -2],
            [0, -3]
        ]

        castle_short = [
            [0, 1],
            [0, 2]
        ]

        if in_range([self._position[0], self._position[1] + 3]):
            piece = board[self._position[0]][self._position[1] + 3]

            if piece.get_type() == PieceType.ROOK and not piece.get_moved()[0] and not self._moved[0]:
                for square in castle_short:
                    new_pos = [self._position[0] + square[0], self._position[1] + square[1]]
                    if board[new_pos[0]][new_pos[1]].get_type() != PieceType.EMPTY:
                        break
                    if king_check(board, self._position, new_pos, self._color):
                        break
                else:
                    possible_moves.append([self._position[0], self._position[1] + 2])

        if in_range([self._position[0], self._position[1] - 4]):
            piece = board[self._position[0]][self._position[1] - 4]

            if piece.get_type() == PieceType.ROOK and not piece.get_moved()[0] and not self._moved[0]:
                for square in castle_long:
                    new_pos = [self._position[0] + square[0], self._position[1] + square[1]]
                    if board[new_pos[0]][new_pos[1]].get_type() != PieceType.EMPTY:
                        break
                    if king_check(board, self._position, new_pos, self._color):
                        break
                else:
                    possible_moves.append([self._position[0], self._position[1] - 3])

        if board_hash in past_moves:
            past_moves[board_hash][self] = possible_moves
        else:
            past_moves[board_hash] = {board_hash: possible_moves}

        return possible_moves