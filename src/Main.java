package src;

import src.engine.Uci;
import src.game.BitBoard;
import src.game.MoveGenerator;
import src.game.MoveHandler;
import src.game.Precomputer;
import src.utils.GameStack;
import src.utils.MoveList;

public class Main {
    public static void main(String[] args) {
        Precomputer.initAllMoveTables();
        GameStack.initGameStack();
        System.out.println("Scoria v4.0.3");

        BitBoard.initBoardByFen("8/8/8/4r3/8/8/8/8 b");
        BitBoard.printBoard();
        System.out.println();
        MoveList moves = MoveGenerator.generateAllMoves(1);
        for (int i = 0; i < moves.size(); i++) {
            // MoveHandler.doMove(moves.get(i));
            // BitBoard.printBoard();
            System.out.println(Uci.moveToUci(moves.get(i)));
            
            // MoveHandler.undoMove();

            // System.out.println("-----------------");
        }

        System.out.println(moves.size());
    }
}