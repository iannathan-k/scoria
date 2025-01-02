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

print_board(board)