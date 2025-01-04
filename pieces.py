from copy import deepcopy
from enum import Enum

black_king_pos = [0, 4]
white_king_pos = [7, 4]
hit_count = 0

def get_king_pos(side):
    if side:
        return white_king_pos
    else:
        return black_king_pos

def set_king_pos(new_pos, side):
    global white_king_pos
    global black_king_pos
    if side:
        white_king_pos = new_pos
    else:
        black_king_pos = new_pos

def get_hit_count():
    global hit_count
    temp_count = hit_count
    hit_count = 0
    return temp_count

def in_range(position):
    global hit_count
    hit_count += 1
    if 0 <= position[0] <= 7 and 0 <= position[1] <= 7:
        return True
    else:
        return False

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
        current_move = king_pos
        while in_range(current_move):
            if current_move != king_pos:
                if board[current_move[0]][current_move[1]].get_type() != PieceType.EMPTY:
                    if board[current_move[0]][current_move[1]].get_color() == color:
                        break
                    if board[current_move[0]][current_move[1]].get_color() != color:
                        if board[current_move[0]][current_move[1]].get_type() == PieceType.ROOK:
                            return True
                        elif board[current_move[0]][current_move[1]].get_type() == PieceType.QUEEN:
                            return True

            current_move = [current_move[0] + direction[0], current_move[1] + direction[1]]

    # Bishop Capture
    for direction in directionsB:
        current_move = king_pos
        while in_range(current_move):
            if current_move != king_pos:
                if board[current_move[0]][current_move[1]].get_type() != PieceType.EMPTY:
                    if board[current_move[0]][current_move[1]].get_color() == color:
                        break
                    if board[current_move[0]][current_move[1]].get_color() != color:
                        if board[current_move[0]][current_move[1]].get_type() == PieceType.BISHOP:
                            return True
                        elif board[current_move[0]][current_move[1]].get_type() == PieceType.QUEEN:
                            return True

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
            if board[move[0]][move[1]].get_color() != color:
                if board[move[0]][move[1]].get_type() == PieceType.KNIGHT:
                    return True

    movesPB = [
        [king_pos[0] - 1, king_pos[1] - 1],
        [king_pos[0] - 1, king_pos[1] + 1]
    ]

    movesPW = [
        [king_pos[0] + 1, king_pos[1] - 1],
        [king_pos[0] + 1, king_pos[1] + 1]
    ]

    # Pawn Capture
    if color == PieceColor.BLACK:
        for move in movesPB:
            if in_range(move):
                if board[move[0]][move[1]].get_type() == PieceType.PAWN:
                    if board[move[0]][move[1]].get_color() == PieceColor.WHITE:
                        return True

    if color == PieceColor.WHITE:
        for move in movesPW:
            if in_range(move):
                if board[move[0]][move[1]].get_type() == PieceType.PAWN:
                    if board[move[0]][move[1]].get_color() == PieceColor.BLACK:
                        return True

    return False


def king_check(board, origin_pos, target_pos, color):
    new_board = deepcopy(board)
    piece = new_board[origin_pos[0]][origin_pos[1]]
    new_board[target_pos[0]][target_pos[1]] = piece
    new_board[origin_pos[0]][origin_pos[1]] = Empty()
    piece.set_position(target_pos)

    return king_in_check(new_board, color)

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
        self._points = 1

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

    def get_moves(self, board, recursive = False):
        possible_moves = []
        moves = [
            [self._position[0] + self._direction, self._position[1]], # advance 1
            [self._position[0] + self._direction * 2, self._position[1]], # advance 2
            [self._position[0] + self._direction, self._position[1] + 1], # capture right
            [self._position[0] + self._direction, self._position[1] - 1] # capture left
        ]

        if in_range(moves[0]) and (recursive or not king_check(board, self._position, moves[0], self._color)):
            if board[moves[0][0]][moves[0][1]].get_type() == PieceType.EMPTY:
                possible_moves.append(moves[0])

        if in_range(moves[1]) and (recursive or not king_check(board, self._position, moves[1], self._color)):
            if board[moves[1][0]][moves[1][1]].get_type() == PieceType.EMPTY:
                if possible_moves and (self._position[0] == 6 or self._position[0] == 1):
                    possible_moves.append(moves[1])

        if in_range(moves[2]) and (recursive or not king_check(board, self._position, moves[2], self._color)):
            if board[moves[2][0]][moves[2][1]].get_type() != PieceType.EMPTY:
                if board[moves[2][0]][moves[2][1]].get_color() != self._color:
                    possible_moves.append(moves[2])

        if in_range(moves[3]) and (recursive or not king_check(board, self._position, moves[3], self._color)):
            if board[moves[3][0]][moves[3][1]].get_type() != PieceType.EMPTY:
                if board[moves[3][0]][moves[3][1]].get_color() != self._color:
                    possible_moves.append(moves[3])

        return possible_moves

class Knight:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.KNIGHT
        self._color = color
        self._points = 3

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

    def get_moves(self, board, recursive = False):
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
            if in_range(move) and (recursive or not king_check(board, self._position, move, self._color)):
                if board[move[0]][move[1]].get_color() != self._color:
                    possible_moves.append(move)

        return possible_moves

class Bishop:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.BISHOP
        self._color = color
        self._points = 3

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

    def get_moves(self, board, recursive = False):
        possible_moves = []

        directions = [
            [1, 1], # SE
            [1, -1], # SW
            [-1, 1], # NE
            [-1, -1] # NW
        ]

        for direction in directions:
            current_move = [self._position[0], self._position[1]]
            while in_range(current_move):
                if recursive or not king_check(board, self._position, current_move, self._color):
                    if current_move != self._position:
                        if board[current_move[0]][current_move[1]].get_color() == self._color:
                            break
                        elif board[current_move[0]][current_move[1]].get_type() == PieceType.EMPTY:
                            possible_moves.append(current_move)
                        else:
                            possible_moves.append(current_move)
                            break

                current_move = [current_move[0] + direction[0], current_move[1] + direction[1]]

        return possible_moves

class Rook:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.ROOK
        self._color = color
        self._points = 5

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

    def get_moves(self, board, recursive = False):
        possible_moves = []

        directions = [
            [-1, 0], # N
            [1, 0], # S
            [0, 1], # E
            [0, -1] # W
        ]

        for direction in directions:
            current_move = [self._position[0], self._position[1]]
            while in_range(current_move):
                if recursive or not king_check(board, self._position, current_move, self._color):
                    if current_move != self._position:
                        if board[current_move[0]][current_move[1]].get_color() == self._color:
                            break
                        elif board[current_move[0]][current_move[1]].get_type() == PieceType.EMPTY:
                            possible_moves.append(current_move)
                        else:
                            possible_moves.append(current_move)
                            break

                current_move = [current_move[0] + direction[0], current_move[1] + direction[1]]

        return possible_moves

class Queen:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.QUEEN
        self._color = color
        self._points = 9

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

    def get_moves(self, board, recursive = False):
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
            current_move = [self._position[0], self._position[1]]
            while in_range(current_move):
                if recursive or not king_check(board, self._position, current_move, self._color):
                    if current_move != self._position:
                        if board[current_move[0]][current_move[1]].get_color() == self._color:
                            break
                        elif board[current_move[0]][current_move[1]].get_type() == PieceType.EMPTY:
                            possible_moves.append(current_move)
                        else:
                            possible_moves.append(current_move)
                            break

                current_move = [current_move[0] + direction[0], current_move[1] + direction[1]]

        return possible_moves

class King:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.KING
        self._color = color
        self._points = 10

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

    def get_moves(self, board, recursive = False):
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
                if recursive or not king_check(board, self._position, current_move, self._color):
                    if board[current_move[0]][current_move[1]].get_color() != self._color:
                        possible_moves.append(current_move)

        return possible_moves