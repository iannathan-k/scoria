# Overview

Introducing Scoria, an open source uci engine programmed completely in Java, with an estimated rating of approximately ~2000 elo.

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

**Debug Log File**\
~ Path to the file should be specified

**Set Move Overhead**\
~ Any time within 0 to 1000 milliseconds

**Ponder**\
~ Ability to think during opponents time

**Clear Hash Table**\
~ Clear all entries in the hash table

## Rating Estimate

An estimation of Scoria's rating was determined by playing against the different Stockfish 17.1 skill levels over a total of 200 games, with 1 second per move for each side. The results were run through [Bayeselo](https://www.remi-coulom.fr/Bayesian-Elo/) to determine a relative elo rating. 

```
Rank Name             Elo    +    - games score oppo. draws
   1 master-skill-5   142   70   69    50   52%   127   12%
   2 Scoria_v3.8.49   127   38   37   200   70%   -32   13%
   3 master-skill-4    17   68   71    50   35%   127   18%
   4 master-skill-3   -85   71   80    50   22%   127   16%
   5 master-skill-2  -201   83  105    50   13%   127    6%
```

The results were adjusted relative to the official [Skill Level Ratings](https://github.com/official-stockfish/Stockfish/commit/a08b8d4) provided by Stockfish, which itself was calibrated against [CCRL](https://computerchess.org.uk/ccrl/4040/).


```
   # PLAYER             :  RATING   ERROR   PLAYED
   1 master-skill-5     :  2203.7    25.3     5422
   2 Scoria_v3.8.49     :  2028.1   126.1      200
   3 master-skill-4     :  1922.9    25.9     5399
   4 master-skill-3     :  1742.3    27.8     4439
   5 master-skill-2     :  1608.4    29.4     4389
```

## Disclaimers

Scoria does not support the 50 Move Rule which states that 50 moves without any pawn move or capture will result in a draw. As well as underpromotion which means promoting to a knight, bishop or rook for both the player and itself. All promotions are automatically assumed to be to a queen.

Java 14+ is required to compile and execute Scoria, due to usage of the newer [Switch Expressions](https://docs.oracle.com/en/java/javase/17/language/switch-expressions-and-statements.html) ("case L ->" Labels) not supported by older Java versions.

Static evaluation of board positions are done using an [HCE](https://www.chessprogramming.org/Simplified_Evaluation_Function) (Handcrafted Evaluation), which accounts for piece values, positions and mobility. There is no use of [NNUE](https://www.chessprogramming.org/NNUE) (Efficiently Updatable Neural Network).

##### Ian Nathan Kusmiantoro, 2025