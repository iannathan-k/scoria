# Overview

Introducing Scoria, an open source uci engine programmed completely in Java, with an estimated rating of approximately 2723 elo.

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

Additionally, Scoria does not include any form of GUI (Graphical User Interface) and purely operates on the command line. It is recommended to use compatible program to display the board and play, such as [CuteChess](https://cutechess.com/) or [SCID vs. PC](https://scidvspc.sourceforge.net/). You could also use command line tools such as [FastChess](https://github.com/Disservin/fastchess) to run tournaments.

## Supported Options

Debug Log File (path)\
~ Output file to write logs

Move Overhead (ms)\
~ Latency for interface

Clear Hash\
~ Reset transposition table

Ponder\
~ Enable permanent thinking

Hash (MiB)\
~ Set transposition table size in megabytes


## Rating Estimate

Official testers have placed Scoria 4 on the [CCRL Rating List](https://computerchess.org.uk/404/cgi/engine_details.cgi?print=Details&each_game=1&eng=Scoria%204.4.7%2064-bit#Scoria_4_4_7_64-bit) and given it an ELO of approximately 2723 ± 21. This was based on 626 games as of Aug 15 2026, but more may be played in the future against stronger opponents.

## Disclaimers

Java 14+ is required to compile and execute Scoria, due to usage of the newer [Switch Expressions](https://docs.oracle.com/en/java/javase/17/language/switch-expressions-and-statements.html) ("case L ->" Labels) not supported by older Java versions.

Static evaluation of board positions are done using an [HCE](https://www.chessprogramming.org/Simplified_Evaluation_Function) (Handcrafted Evaluation), which accounts for piece values, positions, mobility as well as structure. There is no use of [NNUE](https://www.chessprogramming.org/NNUE) (Efficiently Updatable Neural Network).

Scoria is distributed under the MIT license. See the LICENSE file for more details.

## Acknowledgements

Thank you to the team at [CCRL](https://computerchess.org.uk/) for testing engines, the [TalkChess](https://talkchess.com/) community for support, the [Chess Programming Wiki](https://chessprogramming.org/) as well as the [r/chessprogramming](https://www.reddit.com/r/chessprogramming/) subreddit for guidance and open source engines such as [Weiss](https://github.com/TerjeKir/weiss) and [Stockfish](https://github.com/official-stockfish/Stockfish) for inspiration.

## Changelog

Scoria v4.4.9
1. Better Time Management
2. README Update

##### Ian Nathan Kusmiantoro, 2026