from scoria import *
from math import inf
from interface import *

board = [[Empty() for i in range(8)] for j in range(8)]

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
        rook = board[target_pos[0]][target_pos[1] - 2]
        board[origin_pos[0]][origin_pos[1] - 1] = rook
        board[target_pos[0]][target_pos[1] - 2] = Empty()
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

def en_passant(origin_pos, target_pos):
    piece = board[origin_pos[0]][origin_pos[1]]
    board[target_pos[0]][target_pos[1]] = piece
    board[target_pos[0] - 1][target_pos[1]] = Empty()
    piece.set_position(target_pos)
    piece.set_passant(False, 0)
    piece.set_passant(False, 1)

def move_piece(origin_pos, target_pos):
    piece = board[origin_pos[0]][origin_pos[1]]
    captured_piece = board[target_pos[0]][target_pos[1]]

    if piece.get_type() == PieceType.PAWN:
        if target_pos[1] != origin_pos[1]:
            if captured_piece.get_type() == PieceType.EMPTY:
                en_passant(origin_pos, target_pos)

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

    # En Passant Logic
    if piece.get_type() == PieceType.PAWN:
        if abs(origin_pos[0] - target_pos[0]) > 1:
            if in_range([target_pos[0], target_pos[1] + 1]):
                right_piece = board[target_pos[0]][target_pos[1] + 1]
                if right_piece.get_type() == PieceType.PAWN and right_piece.get_color() != piece.get_color():
                    right_piece.set_passant(True, 0)
                    right_piece.set_count()
            if in_range([target_pos[0], target_pos[1] - 1]):
                left_piece = board[target_pos[0]][target_pos[1] - 1]
                if left_piece.get_type() == PieceType.PAWN and left_piece.get_color() != piece.get_color():
                    left_piece.set_passant(True, 1)
                    left_piece.set_count()

    # Promotion Logic
    if board[target_pos[0]][target_pos[1]].get_type() == PieceType.PAWN:
        if target_pos[0] == 7 or target_pos[0] == 0:
            board[target_pos[0]][target_pos[1]] = Queen(target_pos, board[target_pos[0]][target_pos[1]].get_color())

def get_move_input():
    while True:
        uci = input("Enter Move: ")
        if len(uci) < 4:
            print("Invalid Move")
            continue

        move = [uci_to_move(uci)[0], uci_to_move(uci)[1]]
        piece = board[move[0][0]][move[0][1]]
        if piece.get_color() != PieceColor.WHITE:
            print("Invalid Move")
            continue
        if move[1] not in piece.get_moves(board):
            print("Invalid Move")
            continue
        break

    return move

def __main__():
    depth = input("Recursion Depth?: ")
    fen = input("fen?: ")
    mode = input("mode?: ")
    # Mode 1 - Human vs Bot
    # Mode 2 - Human vs Bot UCI
    # Mode 3 - Bot vs Bot
    # Mode 4 - Bot vs Bot UCI
    if not mode.isdigit(): mode = 3
    if not fen: fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq"
    if not depth.isdigit(): depth = 2
    depth = int(depth)
    mode = int(mode)
    turn = set_up(fen)
    move_count = 0

    if mode == 1 or mode == 3:
        print_board(board)

    while True:
        if turn:
            if mode == 1 or mode == 2:
                move = get_move_input()
                move_piece(move[0], move[1])

            if mode == 1:
                print_board(board)

            if mode == 3 or mode == 4:
                coulee_move = minimax(board, depth, -inf, inf, turn)
                move_piece(coulee_move[1][0], coulee_move[1][1])

            if mode == 3:
                print("~~~~~BLACK TO MOVE~~~~~")
                print_board(board)

                print("Evaluation,", coulee_move[0])
                print("Hit count: ", str(get_hit_count()))
                print("Branches Searched: ", str(get_search_count()))

            if mode == 4:
                print(move_to_uci(coulee_move[1][0], coulee_move[1][1], board))

            move_count += 1

        else:
            coulee_move = minimax(board, depth, -inf, inf, turn)
            move_piece(coulee_move[1][0], coulee_move[1][1])

            if mode == 3 or mode == 1:
                print("~~~~~WHITE TO MOVE~~~~~")
                print_board(board)

                print("Evaluation,", coulee_move[0])
                print("Hit count: ", str(get_hit_count()))
                print("Branches Searched: ", str(get_search_count()))

            if mode == 2 or mode == 4:
                print(move_to_uci(coulee_move[1][0], coulee_move[1][1], board))

        turn = not turn
        add_state(board)
        set_current_count()

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