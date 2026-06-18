# Overview

Introducing Scoria, an open source uci engine programmed completely in Java, with an estimated rating of approximately ~2500 elo.

## Compiling

```
git clone https://github.com/iannathan-k/scoria.git
cd scoria
javac src/Main.java
```

```
java src/Main
```

## Operation

Scoria follows [UCI](https://official-stockfish.github.io/docs/stockfish-wiki/UCI-&-Commands.html) (Universal Chess Interface) protocol for all games, communication and notation. It does not follow standard [Algebraic Notation](https://en.wikipedia.org/wiki/Algebraic_notation_(chess)) unlike most chess websites.

However, Scoria does not include any form of GUI (Graphical User Interface) and purely operates on the command line. It is recommended to use compatible program to display the board and play, such as [CuteChess](https://cutechess.com/), [SCID vs. PC](https://scidvspc.sourceforge.net/) or [Arena](http://www.playwitharena.de/).

## Supported Options

No options are supported at the moment, as Scoria 4 is still a work in progress.

## Rating Estimate

Through rudimentary self-testing, Scoria 4 is currently estimated to be around ~2200 ELO, being able to hold its own against bots rated there on CCRL. More information and proper testing coming in future updates.

## Disclaimers

Scoria does not support the 50 Move Rule which states that 50 moves without any pawn move or capture will result in a draw. However, Scoria 4 now supports underpromotion which is an improvement over Scoria 3 which assumed all promotions were queen promotions.

Java 14+ is required to compile and execute Scoria, due to usage of the newer [Switch Expressions](https://docs.oracle.com/en/java/javase/17/language/switch-expressions-and-statements.html) ("case L ->" Labels) not supported by older Java versions.

Static evaluation of board positions are done using an [HCE](https://www.chessprogramming.org/Simplified_Evaluation_Function) (Handcrafted Evaluation), which accounts for piece values, positions, mobility as well as structure. There is no use of [NNUE](https://www.chessprogramming.org/NNUE) (Efficiently Updatable Neural Network).

## Changelog

Scoria v4.4.6

1. Endgame PSQT For All Pieces
2. Preallocated Arrays
3. Partial Move Picking
4. ProbCut
5. Fixed Invalid PVs
6. Capture History Heuristic
7. Move Picker
8. Protect PV Nodes
9. Fixed Delta Pruning

##### Ian Nathan Kusmiantoro, 2026