from pieces import *
from evaluation import *
from scoria import *
from math import inf

board = [[Empty() for i in range(8)] for j in range(8)]

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
    piece = board[origin_pos[0]][origin_pos[1]]
    board[target_pos[0]][target_pos[1]] = piece
    board[origin_pos[0]][origin_pos[1]] = Empty()
    piece.set_position(target_pos)

    if board[target_pos[0]][target_pos[1]].get_type() == PieceType.PAWN:
        if target_pos[0] == 7 or target_pos[0] == 0:
            board[target_pos[0]][target_pos[1]] = Queen(target_pos, board[target_pos[0]][target_pos[1]].get_color())

def __main__():
    depth = int(input("Recursion Depth?: "))
    turn = True

    fen = input("fen?: ")
    set_up(fen)
    print_board(board)

    while True:
        if not turn:
            # origin1 = int(input("Player piece row: "))
            # origin2 = int(input("Player piece column: "))
            # target1 = int(input("Player move row: "))
            # target2 = int(input("Player move column: "))
            # move_piece([origin1, origin2], [target1, target2])
            #
            # print_board(board)

            coulee_move = minimax(board, depth, -inf, inf, turn)
            move_piece(coulee_move[1][0], coulee_move[1][1])

            print("~~~~~WHITE TO MOVE~~~~~")
            print_board(board)

            print("Evaluation,", coulee_move[0])
            print("Hit count: ", str(get_hit_count()))
            print("Branches Searched: ", str(get_search_count()))

        else:
            coulee_move = minimax(board, depth, -inf, inf, turn)
            move_piece(coulee_move[1][0], coulee_move[1][1])

            print("~~~~~BLACK TO MOVE~~~~~")
            print_board(board)

            print("Evaluation,", coulee_move[0])
            print("Hit count: ", str(get_hit_count()))
            print("Branches Searched: ", str(get_search_count()))

        turn = not turn
        add_state(board)
        print(get_king_pos(board, not turn))

        if determine_winner(board, turn) == PieceColor.WHITE:
            print("WHITE WON")
            break
        if determine_winner(board, turn) == PieceColor.BLACK:
            print("BLACK WON")
            break
        if determine_winner(board, turn) == PieceType.EMPTY:
            print("STALEMATE")
            break

if __name__ == __main__():
    __main__()