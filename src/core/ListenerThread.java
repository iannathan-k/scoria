package src.core;

import java.util.Scanner;

import src.scoria.Scoria;

public class ListenerThread extends Thread {

    public static Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        String input = scanner.nextLine().trim();
        DebugLogger.logIn(input);
        while (!input.equals("stop")) {
            input = scanner.nextLine().trim();
            DebugLogger.logIn(input);
        }
        Scoria.cancel_time = 0;
    }
}
