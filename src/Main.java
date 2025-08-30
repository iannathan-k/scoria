package src;

import src.game.Precomputer;
import src.utils.GameStack;

public class Main {
    public static void main(String[] args) {
        Precomputer.initAllMoveTables();
        GameStack.initGameStack();
        System.out.println("Scoria v4.0.2A");
    }
}