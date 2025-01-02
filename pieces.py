from enum import Enum

def in_range(position):
    if 0 <= position[0] <= 7 and 0 <= position[1] <= 7:
        return True
    else:
        return False

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

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

    def get_direction(self):
        return self._direction

    def set_position(self):
        return self._position

    def get_moves(self, board):
        possible_moves = []
        moves = [
            [self._position[0] + self._direction, self._position[1]], # advance 1
            [self._position[0] + self._direction * 2, self._position[1]], # advance 2
            [self._position[0] + self._direction, self._position[1] + 1], # capture right
            [self._position[0] + self._direction, self._position[1] - 1] # capture left
        ]

        if in_range(moves[0]):
            if board[moves[0][0]][moves[0][1]].get_type() == PieceType.EMPTY:
                possible_moves.append(moves[0])

        if in_range(moves[1]):
            if board[moves[1][0]][moves[1][1]].get_type() == PieceType.EMPTY:
                if possible_moves and (self._position[0] == 6 or self._position[0] == 1):
                    possible_moves.append(moves[1])

        if in_range(moves[2]):
            if board[moves[2][0]][moves[2][1]].get_type() != PieceType.EMPTY:
                if board[moves[2][0]][moves[2][1]].get_color() != self._color:
                    possible_moves.append(moves[2])

        if in_range(moves[3]):
            if board[moves[3][0]][moves[3][1]].get_type() != PieceType.EMPTY:
                if board[moves[3][0]][moves[3][1]].get_color() != self._color:
                    possible_moves.append(moves[3])

        return possible_moves

class Knight:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.KNIGHT
        self._color = color

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

    def set_position(self):
        return self._position

    def get_moves(self, board):
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
                    possible_moves.append(move)

        return possible_moves