package src.core;

import java.util.Scanner;

import src.scoria.Scoria;

public class ListenerThread extends Thread {

    public static Scanner scanner = new Scanner(System.in);

    public static long ponderhit_time = 0;

    @Override
    public void run() {
        String input = scanner.nextLine().trim();
        DebugLogger.logIn(input);
        while (!input.equals("stop") && !input.equals("ponderhit")) {
            input = scanner.nextLine().trim();
            DebugLogger.logIn(input);
        }
        if (input.equals("stop")) {
            Scoria.cancel_time = 0;
        } else if (input.equals("ponderhit")) {
            Scoria.cancel_time = System.currentTimeMillis() + ponderhit_time;
            Command.ponder_hit = true;
        }
        
    }
}
