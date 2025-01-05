from enum import Enum

hit_count = 0
searched_count = 0

def print_board(game_board):
    piece_chars_white = {
        PieceType.PAWN : "♟",
        PieceType.KNIGHT : "♞",
        PieceType.BISHOP : "♝",
        PieceType.ROOK : "♜",
        PieceType.QUEEN : "♛",
        PieceType.KING : "♚",
        PieceType.EMPTY : " "
    }

    piece_chars_black = {
        PieceType.PAWN: "♙",
        PieceType.KNIGHT: "♘",
        PieceType.BISHOP: "♗",
        PieceType.ROOK: "♖",
        PieceType.QUEEN: "♕",
        PieceType.KING: "♔",
        PieceType.EMPTY: " "
    }

    print("+---+---+---+---+---+---+---+---+")
    for line in game_board:
        game_line = "| "
        for piece in line:
            if piece.get_color() == PieceColor.BLACK:
                game_line += piece_chars_black[piece.get_type()]
            else:
                game_line += piece_chars_white[piece.get_type()]
            game_line += " | "
        print(game_line)
        print("+---+---+---+---+---+---+---+---+")

def get_king_pos(board, side):

    for i in range(64):
        if board[i // 8][i % 8].get_type() == PieceType.KING:
            if board[i // 8][i % 8].get_color() == PieceColor.BLACK and not side:
                return [i // 8, i % 8]
            elif board[i // 8][i % 8].get_color() == PieceColor.WHITE and side:
                return [i // 8, i % 8]

    print("king not found?")
    print_board(board)

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
    if 0 <= position[0] <= 7 and 0 <= position[1] <= 7:
        return True
    else:
        return False

def king_in_check(board, color):

    if color == PieceColor.WHITE:
        king_pos = get_king_pos(board, True)
    else:
        king_pos = get_king_pos(board, False)

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
        [-1, -1]  # NWv
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
                        else:
                            break

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
            if board[move[0]][move[1]].get_color() != color:
                if board[move[0]][move[1]].get_type() == PieceType.KNIGHT:
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
                if board[move[0]][move[1]].get_type() == PieceType.PAWN:
                    if board[move[0]][move[1]].get_color() == PieceColor.WHITE:
                        return True

    if color == PieceColor.WHITE:
        for move in movesPW:
            if in_range(move):
                if board[move[0]][move[1]].get_type() == PieceType.PAWN:
                    if board[move[0]][move[1]].get_color() == PieceColor.BLACK:
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

    # if origin_pos == target_pos:
    #     print("OOPS")
    #     print(origin_pos)
    #     print(target_pos)

    # if board[origin_pos[0]][origin_pos[1]].get_type() == PieceType.KING:
    #     print(board[origin_pos[0]][origin_pos[1]])
    #     print(board[target_pos[0]][target_pos[1]])

    # if board[origin_pos[0]][origin_pos[1]].get_type() == PieceType.KING:
        # print("MOVE[0]: ", str(origin_pos))
        # print("MOVE[1]: ", str(target_pos))

    # from inspect import getframeinfo, stack

    # for i in range(1, 6):
    #     caller = getframeinfo(stack()[i][0])
    #     print("%s:%d" % (caller.filename, caller.lineno))
    #
    # print("-----------------------------------------")

    # print("OLD: ", str(get_king_pos(board, False)))

    piece = board[origin_pos[0]][origin_pos[1]]
    captured_piece = board[target_pos[0]][target_pos[1]]
    board[target_pos[0]][target_pos[1]] = piece
    board[origin_pos[0]][origin_pos[1]] = Empty()

    # print("NEW: ", str(get_king_pos(board, False)))

    result = king_in_check(board, color)

    board[origin_pos[0]][origin_pos[1]] = piece
    board[target_pos[0]][target_pos[1]] = captured_piece

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
                    if not king_check(board, self._position, move, self._color):
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

    def get_moves(self, board):
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
                elif not king_check(board, self._position, current_move, self._color):
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

    def get_moves(self, board):
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
                elif not king_check(board, self._position, current_move, self._color):
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

    def get_moves(self, board):
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
                    elif not king_check(board, self._position, current_move, self._color):
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

    def get_moves(self, board):
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


        return possible_moves