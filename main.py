from pieces import *

board = [[Empty() for i in range(8)] for j in range(8)]

def print_board(game_board):
    piece_chars = {
        PieceType.PAWN : "P",
        PieceType.KNIGHT : "N",
        PieceType.BISHOP : "B",
        PieceType.ROOK : "R",
        PieceType.QUEEN : "Q",
        PieceType.KING : "K",
        PieceType.EMPTY : " "
    }

    print("+---+---+---+---+---+---+---+---+")
    for line in game_board:
        game_line = "| "
        for piece in line:
            if piece.get_color() == PieceColor.BLACK:
                game_line += piece_chars[piece.get_type()].lower()
            else:
                game_line += piece_chars[piece.get_type()]
            game_line += " | "
        print(game_line)
        print("+---+---+---+---+---+---+---+---+")

def set_up(fen_string):
    index = 0
    for letter in fen_string:
        if letter == "P":
            board[index // 8][index % 8] = Pawn([index // 8, index % 8], PieceColor.WHITE, -1)
        elif letter == "N":
            board[index // 8][index % 8] = Knight([index // 8, index % 8], PieceColor.WHITE)
        elif letter == "B":
            board[index // 8][index % 8] = Bishop([index // 8, index % 8], PieceColor.WHITE)
        elif letter == "R":
            board[index // 8][index % 8] = Rook([index // 8, index % 8], PieceColor.WHITE)
        elif letter == "Q":
            board[index // 8][index % 8] = Queen([index // 8, index % 8], PieceColor.WHITE)
        elif letter == "K":
            board[index // 8][index % 8] = King([index // 8, index % 8], PieceColor.WHITE)

        if letter == "p":
            board[index // 8][index % 8] = Pawn([index // 8, index % 8], PieceColor.BLACK, 1)
        elif letter == "n":
            board[index // 8][index % 8] = Knight([index // 8, index % 8], PieceColor.BLACK)
        elif letter == "b":
            board[index // 8][index % 8] = Bishop([index // 8, index % 8], PieceColor.BLACK)
        elif letter == "r":
            board[index // 8][index % 8] = Rook([index // 8, index % 8], PieceColor.BLACK)
        elif letter == "q":
            board[index // 8][index % 8] = Queen([index // 8, index % 8], PieceColor.BLACK)
        elif letter == "k":
            board[index // 8][index % 8] = King([index // 8, index % 8], PieceColor.BLACK)

        if letter in ["1", "2", "3", "4", "5", "6", "7", "8"]:
            index += int(letter) - 1

        if letter != "/":
            index += 1

def move_piece(origin_pos, target_pos):
    board[target_pos[0]][target_pos[1]] = board[origin_pos[0]][origin_pos[1]]
    board[origin_pos[0]][origin_pos[1]] = Empty()
    board[target_pos[0]][target_pos[1]].set_position(target_pos)

fen = input()
set_up(fen)
print_board(board)

while True:
    o1 = int(input())
    o2 = int(input())
    origin = [o1, o2]
    t1 = int(input())
    t2 = int(input())
    target = [t1, t2]
    move_piece(origin, target)
    print_board(board)