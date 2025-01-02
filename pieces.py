from copy import deepcopy
from enum import Enum

def in_range(position):
    if 0 <= position[0] <= 7 and 0 <= position[1] <= 7:
        return True
    else:
        return False

def king_check(board, origin_pos, target_pos, color):
    new_board = deepcopy(board)
    piece = new_board[origin_pos[0]][origin_pos[1]]
    new_board[target_pos[0]][target_pos[1]] = piece
    new_board[origin_pos[0]][origin_pos[1]] = Empty()
    piece.set_position(target_pos)

    king_pos = []
    opponent_moves = []
    for row in new_board:
        for square in row:
            if square.get_type() == PieceType.EMPTY:
                continue
            if square.get_type() == PieceType.KING and square.get_color() == color:
                king_pos = square.get_position()
            if square.get_color() != color:
                opponent_moves += square.get_moves(new_board, True)

    if king_pos in opponent_moves:
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

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

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
            if in_range(move) and (recursive or not king_check(board, self._position, moves[0], self._color)):
                if board[move[0]][move[1]].get_color() != self._color:
                    possible_moves.append(move)

        return possible_moves

class Bishop:
    def __init__(self, position, color):
        self._position = position
        self._type = PieceType.BISHOP
        self._color = color

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

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

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

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

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

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

    def get_position(self):
        return self._position

    def get_type(self):
        return self._type

    def get_color(self):
        return self._color

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