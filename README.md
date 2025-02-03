# Scoria AI

Started just after the new year of 2025, introducing Scoria, a rudimentary chess bot, programmed completely in Java. Current with a rating ~1600 on lichess, as it can beat stockfish levels 1-3 with relative ease, but ends up losing by a thin margin to stockfish 4, which is estimated to be rated around 1700.

## Table of Contents

- [Disclaimers](#disclaimers)
    - [Rating](#rating)
    - [Requirements](#requirements)
- [Operation](#operation)
    - [Compiling](#compiling)
    - [Executing](#executing)
    - [Commands](#commands)
    - [Gamemodes](#gamemodes)
- [Mechanism](#mechanism)
    - [Evaluation](#evaluation)
    - [Minimax](#minimax)
    - [Optimization](#optimization)
    - [Logic](#logic)

<br>

# Disclaimers

## Author 
Ian Nathan Kusmiantoro

## Requirements

Java 14+

<br>

# Operation

## Compiling

```bash
git clone https://github.com/iannathan-k/scoria.git
cd scoria
javac src/Main.java
```

## Executing

```bash
java src/Main
```

## Commands

| Field       | Description                                                                                                    | Deafult |
|---------------|----------------------------------------------------------------------------------------------------------------|---------|
| `pos {fen}`    | Setup the game board based on a fen string                                                                              | start   |
| `d`             | Display the current board state in the command line                                                            |         |
| `play {mode}`   | Play from the current position. [See Here](#gamemodes)                                                 | 3       |
| `perft {depth}` | Run a perft for the number of positions after n moves where depth is n                                         | 5       |
| `think {time}`  | Set the think time for the bot in milliseconds. If no time is passed in it will display the current think time |         |
| `exit`          | Exit the program                                                                                               |         |

## Perft

Perft recursively searches the nodes until the certain depth, where it finds the number of final positions which are possible. It is used to check the accuracy of an engine in evaluating every possible position. For each move on the first layer, the engine will print out the possible moves for debugging or reference purposes.

>d3d2: 2304\
>d3e3: 1810\
>d3c3: 1810\
>d3e2: 2414\
>d3c2: 2414

## Moving

To move, you will be prompted to enter your move using UCI format. UCI is the universal chess interface, stating the starting square and the final square. For more information [click here](https://en.wikipedia.org/wiki/Universal_Chess_Interface).

## Gamemodes

### Human vs Bot CLI

This is mode 1, you play as white again a bot with the board as well as other information being printed directly into the command line for convenience.

>\~~~ white to move ~~~
>
>|   | a | b | c | d | e | f | g | h |
>|---|---|---|---|---|---|---|---|---|
>| 8 |   |   |   |   |   |   |   |   |
>| 7 |   |   |   |   |   |   |   |   |
>| 6 |   |   |   | K |   | p | k |   |
>| 5 |   | P |   |   |   |   |   |   |
>| 4 |   |   |   |   |   |   |   |   |
>| 3 |   |   | r |   |   |   |   |   |
>| 2 |   |   |   |   |   |   |   |   |
>| 1 |   |   |   |   |   |   |   | R |
>
>eval: -15\
>depth: 6\
>nodes: 20636\
>time: 1000ms\
>move: f5g6

### Human vs Bot UCI

This is mode 2, where similarly to mode 1 you play as white against the bot, but instead of printing out all information it simply prints out it's move in uci notation.

>e2e4\
>e7e5\
>g1f3\
>g8f6\
>b1c3\
>b8c6

### Bot vs Bot CLI

This is mode 3, where the bot plays against itself and prints out the board as well as useful information into the command line.

>\~~~ black to move ~~~
>
>|   | a | b | c | d | e | f | g | h |
>|---|---|---|---|---|---|---|---|---|
>| 8 |   |   |   |   | r |   | k | r |
>| 7 | p | p |   |   |   |   | p |   |
>| 6 |   |   | b | B |   | n |   | p |
>| 5 |   |   | P |   | p |   |   |   |
>| 4 |   |   | B |   | P |   |   |   |
>| 3 | P |   |   |   |   |   |   | P |
>| 2 |   | P |   |   |   | P | R |   |
>| 1 |   |   | R |   |   |   | K |   |
>
>eval: 293\
>depth: 6\
>nodes: 23012\
>time: 1000ms

### Bot vs Bot UCI

This is mode 4, where the bot plays against itself, but only prints out the UCI notation of what moves it makes to be referenced in some other form of UI like Lichess analysis.

>g1f3\
>g8f6\
>b1c3\
>b8c6\
>e2e3\
>a7a6

<br>

# Mechanism

### Evaluation

Evaluation considers three main variables. Piece points mostly following standard count to prevent sacrifices, positional weighting depending on piecetype with position for strategy and mobility for encouraging more developing movements to win tempo. To see more [click here](https://www.chessprogramming.org/Simplified_Evaluation_Function).

$$ Eval = 2\Sigma Points + 2\Sigma Mobility + \Sigma Weights $$

This also suggests that a higher evaluation, `> 0` means white has an advantage, while a lower evaluation, `< 0` favours black. An evaluation of 10000 or -10000 means one of the sides has won the game and 0 for stalemate.

### Minimax

In principle, the AI makes use of a simple [minimax algorithm](https://www.geeksforgeeks.org/minimax-algorithm-in-game-theory-set-1-introduction/), which essentially is an exhaustive depth-first search of all possible moves within the gives limitation of depth. It basically assumes both players will make the best possible moves, so the minimizing player will take the lowest evaluation and the maximizing player the largest. It will then return the path in which even if the perfect game is played, the best outcome is reached.

```java
public static int[][] minimax(Piece[][] board, int depth, int alpha, int beta, boolean turn) {
    if (depth == 0 || Evaluator.gameWinner(board, turn) != PieceColor.EMPTY) {
        return new int[][] {{Evaluator.boardEval(board, turn)}, {}, {}};
    }

    PieceColor color = turn ? PieceColor.WHITE : PieceColor.BLACK;
    ArrayList<int[][]> possible_moves = PieceHandler.getAllMoves(board, color);

    if (turn) {
        int[][] max_eval = {{Integer.MIN_VALUE}, {}, {}};
        for (int[][] move : possible_moves) {
            Piece[] board_info = MoveHandler.moveState(board, move[0], move[1]);
            int eval = minimax(board, depth - 1, alpha, beta, !turn)[0][0];
            MoveHandler.undoState(board, move[0], move[1], board_info);

            if (eval > max_eval[0][0]) {
                max_eval[0][0] = eval;
                max_eval[1] = move[0];
                max_eval[2] = move[1];
            }
        }

        return max_eval;

    } else {
        int[][] min_eval = {{Integer.MAX_VALUE}, {}, {}};
        for (int[][] move : possible_moves) {
            Piece[] board_info = MoveHandler.moveState(board, move[0], move[1]);
            int eval = minimax(board, depth - 1, alpha, beta, !turn)[0][0];
            MoveHandler.undoState(board, move[0], move[1], board_info);
            if (eval < min_eval[0][0]) {
                min_eval[0][0] = eval;
                min_eval[1] = move[0];
                min_eval[2] = move[1];
            }
        }

        return min_eval;
    }
}
```

### Optimization

[Alpha-Beta pruning](https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning) is the largest optimization made to the minimax algorithm. It prunes branches which are guaranteed to never be chosen as the path in an optimal game, which saves time by not searching those branches. It is implemented using alpha and beta values which have simple logic to tell if the branch is useless.

[Order heuristic](https://www.chessprogramming.org/Move_Ordering) is the second greatest, which works to assists alpha beta pruning by increasing effectivity. It essentially predicts which moves are likely to be good, like pawn promotion or beneficial piece captures and pushes them to be evaluated first. This means that the computer will get a better evaluation first, and is able to cut out the worse evaluations later. Currently it functions off of MVV/LVA or most valuable victim vs least valuable attacker and positions. This makes a guestimate of how good the move is likely to be.

The [transposition table](https://www.chessprogramming.org/Transposition_Table) remembers the board states and the possible moves for each piece in that board state. This means that whenever that same state is revisited, the piece moves don't have to be recalucalted as they have already been stored, and just need to be accessed. The board hash is calculated using [zobrist hashing](https://en.wikipedia.org/wiki/Zobrist_hashing), which is a lightweigth fast hashing method which takes advantage of XORs being only one clock cycle.

[Iterative deepening](https://www.geeksforgeeks.org/iterative-deepening-searchids-iterative-deepening-depth-first-searchiddfs/) allows us to limit a search by time instead of depth, canceling the ongoing search once the time has been exceeded allowing punctual responses. Cruicially, it starts searching at a low depth, and once it finishes it increases the depth and going again. Imagine a mix between a DFS and a BFS algorithm. However, this only works well if a transposition table has been implmeneted already, to skip to the next layer to be examined, insetad of re-evaluating every node.

### Logic

Each piece precomputes the squares which it jumps through, and then filters through which ones are valid and invalid. Range check to check if it loops around or goes out of the board, and the landing spot to see if it is trying to capture on of its own pieces.

King check is done by taking the possible squares attackers can be on, like the diagonals and direct lines of sight, and seeing if certain pieces exist on those squares and provide a check to the king. The king can be found itself as it is stored in an array which removes the need to iterate through the entire board to find the position of the king.

Every time a piece is moved, whether it is for finding whether a move is valid, evaluating the board, or searching in the next depth for the minimax algorithm, there is only ever one board which is modified. Simply, the board is reverted when you backtrack, allowing memory saving because there need not a million different copies of the same board.