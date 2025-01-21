from scoria import *
from math import inf

board = [[Empty() for i in range(8)] for j in range(8)]

def print_board():
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

    print("    0   1   2   3   4   5   6   7  ")
    print("  +---+---+---+---+---+---+---+---+")
    for i in range(8):
        game_line = str(i) + " | "
        for piece in board[i]:
            if piece.get_color() == PieceColor.BLACK:
                game_line += piece_chars_black[piece.get_type()]
            else:
                game_line += piece_chars_white[piece.get_type()]
            game_line += " | "
        print(game_line)
        print("  +---+---+---+---+---+---+---+---+")

def enter_piece(letter, index):
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
        king_pieces[0] = board[index // 8][index % 8]

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
        king_pieces[1] = board[index // 8][index % 8]

def set_up(fen_string):
    index = 0
    turn = True
    for letter in fen_string:
        if letter == "/" or letter == " ": continue
        if index < 64:

            enter_piece(letter, index)

            if letter in ["1", "2", "3", "4", "5", "6", "7", "8"]:
                index += int(letter) - 1

            index += 1

        else:

            if letter == "b":
                turn = False
            if letter == "K":
                board[7][7].set_deep_moved(False)
                board[7][7].set_shallow_moved(False)
                board[7][4].set_deep_moved(False)
                board[7][4].set_shallow_moved(False)
            if letter == "Q":
                board[7][0].set_deep_moved(False)
                board[7][0].set_shallow_moved(False)
                board[7][4].set_deep_moved(False)
                board[7][4].set_shallow_moved(False)
            if letter == "k":
                board[0][7].set_deep_moved(False)
                board[0][7].set_shallow_moved(False)
                board[0][4].set_deep_moved(False)
                board[0][4].set_shallow_moved(False)
            if letter == "q":
                board[0][0].set_deep_moved(False)
                board[0][0].set_shallow_moved(False)
                board[0][4].set_deep_moved(False)
                board[0][4].set_shallow_moved(False)

    return turn

def castle(origin_pos, target_pos):
    if origin_pos[1] - target_pos[1] > 1:
        rook = board[target_pos[0]][target_pos[1] - 1]
        board[origin_pos[0]][origin_pos[1] - 1] = rook
        board[target_pos[0]][target_pos[1] - 1] = Empty()
        rook.set_position([origin_pos[0], origin_pos[1] - 1])
        rook.set_deep_moved(True)
        rook.set_shallow_moved(True)
    elif origin_pos[1] - target_pos[1] < -1:
        rook = board[target_pos[0]][target_pos[1] + 1]
        board[origin_pos[0]][origin_pos[1] + 1] = rook
        board[target_pos[0]][target_pos[1] + 1] = Empty()
        rook.set_position([origin_pos[0], origin_pos[1] + 1])
        rook.set_deep_moved(True)
        rook.set_shallow_moved(True)

def move_piece(origin_pos, target_pos):
    piece = board[origin_pos[0]][origin_pos[1]]
    board[target_pos[0]][target_pos[1]] = piece
    board[origin_pos[0]][origin_pos[1]] = Empty()
    piece.set_position(target_pos)

    # If moved logic
    if piece.get_type() == PieceType.ROOK or piece.get_type() == PieceType.KING:
        piece.set_deep_moved(True)
        piece.set_shallow_moved(True)

    # Castling Logic
    if piece.get_type() == PieceType.KING:
        if abs(origin_pos[1] - target_pos[1]) > 1:
            castle(origin_pos, target_pos)

    # Promotion Logic
    if board[target_pos[0]][target_pos[1]].get_type() == PieceType.PAWN:
        if target_pos[0] == 7 or target_pos[0] == 0:
            board[target_pos[0]][target_pos[1]] = Queen(target_pos, board[target_pos[0]][target_pos[1]].get_color())

def __main__():
    depth = int(input("Recursion Depth?: "))
    fen = input("fen?: ")
    if not fen: fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq"
    turn = set_up(fen)
    print_board()
    move_count = 0
    # get_possible_moves(board, True)
    # get_possible_moves(board, False)

    while True:
        if turn:
            # origin1 = int(input("Player piece row: "))
            # origin2 = int(input("Player piece column: "))
            # target1 = int(input("Player move row: "))
            # target2 = int(input("Player move column: "))
            # move_piece([origin1, origin2], [target1, target2])
            #
            # print_board()

            coulee_move = minimax(board, depth, -inf, inf, turn)
            move_piece(coulee_move[1][0], coulee_move[1][1])

            print("~~~~~BLACK TO MOVE~~~~~")
            print_board()

            print("Evaluation,", coulee_move[0])
            print("Hit count: ", str(get_hit_count()))
            print("Branches Searched: ", str(get_search_count()))
            move_count += 1

        else:
            coulee_move = minimax(board, depth, -inf, inf, turn)
            move_piece(coulee_move[1][0], coulee_move[1][1])

            print("~~~~~WHITE TO MOVE~~~~~")
            print_board()

            print("Evaluation,", coulee_move[0])
            print("Hit count: ", str(get_hit_count()))
            print("Branches Searched: ", str(get_search_count()))

        turn = not turn
        add_state(board)

        if determine_winner(board, turn) == PieceColor.WHITE:
            print("WHITE WON")
            print(move_count)
            break
        if determine_winner(board, turn) == PieceColor.BLACK:
            print("BLACK WON")
            print(move_count)
            break
        if determine_winner(board, turn) == PieceType.EMPTY:
            print("STALEMATE")
            print(move_count)
            break

if __name__ == __main__():
    __main__()