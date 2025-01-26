# Scoria AI

An *extremely* rudimentary chess bot, designed and programmed completely in Python.

## Rating

Note that these are only estimations, and are not reflective of what the true ratings may be.

Depth 1: < 250 elo\
Depth 2: ~ 250 elo\
Depth 3: ~ 400 elo\
Depth 4: ~ 700 elo

## How to Operate

1. Start the program, whether in an IDE, terminal or some other dedicated interpreter.


2. After launching, you should see a prompt which asks you to enter your desired search depth for the algorithm. Enter your desired depth in the form of an integer greater than 0. The AI runs within a reasonable time with depths ranging from 1 to 4. Recursion depth is how many moves into the future the AI will be able to see. If you choose to leave this blank, it will default to 2.

> **Recursion Depth?:**
> 3

3. Next, the program should prompt you to enter a FEN string, to set up the board for play. Paste in a fen string which consists of a valid board state, without multiple kings, missing kings, etc. You should include until the point where castling eligibility is defined (KQkq) If you choose to leave this blank it will default to a starting position.
    (https://www.chess.com/terms/fen-chess)

> **fen?:**
> rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq

4. You will then be prompted on the mode. There are 4 modes which can be played in.\
\
   Mode 1: Human vs Bot displayed in terminal\
   Mode 2: Human vs Bot in full UCI\
   Mode 3: Bot vs Bot displayed in terminal\
   Mode 4: Bot vs Bot in full UCI

### Mode 1

To move, you will be prompted to enter your move using UCI format. UCI is the universal chess interface, stating the starting square and the final square.
(https://en.wikipedia.org/wiki/Universal_Chess_Interface)

|   | a | b | c | d | e | f | g | h |
|---|---|---|---|---|---|---|---|---|
| 8 |   |   |   |   |   |   |   |   |
| 7 |   |   |   |   |   |   |   |   |
| 6 |   |   |   |   |   |   |   |   |
| 5 |   |   |   | N |   |   |   |   |
| 4 |   |   |   |   |   |   |   |   |
| 3 |   |   |   |   | q |   |   |   |
| 2 |   |   |   |   |   |   |   |   |
| 1 |   |   |   |   |   |   |   |   |

> **Enter Move:** d5e3

|   | a | b | c | d | e | f | g | h |
|---|---|---|---|---|---|---|---|---|
| 8 |   |   |   |   |   |   |   |   |
| 7 |   |   |   |   |   |   |   |   |
| 6 |   |   |   |   |   |   |   |   |
| 5 |   |   |   |   |   |   |   |   |
| 4 |   |   |   |   |   |   |   |   |
| 3 |   |   |   |   | N |   |   |   |
| 2 |   |   |   |   |   |   |   |   |
| 1 |   |   |   |   |   |   |   |   |

5. If it's the bot's turn, it will make its move automatically, and render the board for you to see. It will also give a few pieces of information. Including, the current board evaluation, the hit_count and the branches_searched.

> **Evaluation,** -43\
> **Hit count:**  77500\
> **Branches Searched:**  1913

6. When an end-game state has been reached, either Checkmate or Stalemate, the game will automatically end, and the winner/result will be printed out along with the number of moves (white + black pairs).

> BLACK WON\
> 21

## How it Works

### Evaluation

Board states are evaluated using an evaluation function, which considers a number of things. Most importantly, pieces are given map weightings, favouring some squares and disfavouring others. For example, knights should be in the center and not at the edges, so the weightings towards the center and high and towards the ends negative. Secondly is the piece values, with some adjustments to make them more reflective rather than the standard piece values, and the mobility, meaning number of possible moves favouring positions where the bot is in control.

(https://www.chessprogramming.org/Simplified_Evaluation_Function)

$$ Eval = \Sigma Points + 5\Sigma Mobility + \Sigma Weights $$

This also suggests that a higher evaluation, `> 0` means white has an advantage, while a lower evaluation, `< 0` favours black. An evaluation of 10000 or -10000 means one of the sides has won the game and 0 for stalemate.

### Recursive Search

In principle, the AI makes use of a simple minimax algorithm, which essentially is an exhaustive depth-first search of all possible moves within the gives limitation of depth. It basically assumes both players will make the best possible moves, so the minimizing player will take the lowest evaluation and the maximizing player the largest. It will then return the path in which even if the perfect game is played, the best outcome is reached.

~~~
def minimax(board, depth, turn):
    if depth == 0 or determine_winner(board, turn) != PieceColor.NULL:
        return [evaluate_board(board, turn), []]

    if turn:
        max_eval = [-inf, []]
        for move in get_possible_moves(board, turn):
            board_info = move_state(board, move[0], move[1])
            evaluation = minimax(board, depth - 1, False)[0]
            undo_move(board, move[0], move[1], board_info)
            if evaluation > max_eval[0]:
                max_eval = [evaluation, move]
        return max_eval

    else:
        min_eval = [+inf, []]
        for move in get_possible_moves(board, turn):
            board_info = move_state(board, move[0], move[1])
            evaluation = minimax(board, depth - 1, True)[0]
            undo_move(board, move[0], move[1], board_info)
            if evaluation < min_eval[0]:
                min_eval = [evaluation, move]
        return min_eval
~~~

### Optimization

Alpha-Beta pruning is the largest optimization made to the minimax algorithm. It prunes branches which are guaranteed to never be chosen as the path in an optimal game, which saves time by not searching those branches. It is implemented using alpha and beta values which have simple logic to tell if the branch is useless.

Order heuristic is the second greatest, which works to assists alpha beta pruning by increasing effectivity. It essentially predicts which moves are likely to be good, like pawn promotion or beneficial piece captures and pushes them to be evaluated first. This means that the computer will get a better evaluation first, and is able to cut out the worse evaluations later.

History heuristic remembers the board states and the possible moves for each piece in that board state. This means that whenever that same state is revisited, the piece moves don't have to be recalucalted as they have already been stored, and just need to be accessed.

~~~
def minimax(board, depth, alpha, beta, turn):
    board_key = hash_board(board)
    if board_key in transposition_table and transposition_table[board_key][0] >= depth:
        return transposition_table[board_key][1]

    if depth == 0 or determine_winner(board, turn) != PieceColor.NULL:
        return [evaluate_board(board, turn), []]

    possible_moves = heuristic_ordering(get_possible_moves(board, turn), board)

    if turn:
        max_eval = [-inf, []]
        for move in possible_moves:
            board_info = move_state(board, move[0], move[1])
            evaluation = minimax(board, depth - 1, alpha, beta, False)[0]
            undo_move(board, move[0], move[1], board_info)
            if evaluation > max_eval[0]:
                max_eval = [evaluation, move]
            alpha = max(alpha, evaluation)
            if beta <= alpha:
                break
        transposition_table[board_key] = [depth, max_eval]
        return max_eval

    else:
        min_eval = [+inf, []]
        for move in possible_moves:
            board_info = move_state(board, move[0], move[1])
            evaluation = minimax(board, depth - 1, alpha, beta, True)[0]
            undo_move(board, move[0], move[1], board_info)
            if evaluation < min_eval[0]:
                min_eval = [evaluation, move]
            beta = min(beta, evaluation)
            if beta <= alpha:
                break
        transposition_table[board_key] = [depth, min_eval]
        return min_eval
~~~

### Logic

Each piece precomputes the squares which it jumps through, and then filters through which ones are valid and invalid. Range check to check if it loops around or goes out of the board, and the landing spot to see if it is trying to capture on of its own pieces.

King check is done by taking the possible squares attackers can be on, like the diagonals and direct lines of sight, and seeing if certain pieces exist on those squares and provide a check to the king. The king can be found itself as it is stored in an array which removes the need to iterate through the entire board to find the position of the king.

Every time a piece is moved, whether it is for finding whether a move is valid, evaluating the board, or searching in the next depth for the minimax algorithm, there is only ever one board which is modified. Simply, the board is reverted when you backtrack, allowing memory saving because there need not a million different copies of the same board.

## Author
### Ian Nathan Kusmiantoro