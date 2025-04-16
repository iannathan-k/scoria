package src.core;

import java.io.FileWriter;
import java.io.IOException;

public class DebugLogger {
    public static void logIn(String line) {
        if (!Command.debug_log) return;
        try {
            FileWriter writer = new FileWriter("scoria.log", true);
            writer.write("<< " + line + "\n");
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void logOut(String line) {
        System.out.println(line);

        if (!Command.debug_log) return;
        try {
            FileWriter writer = new FileWriter("scoria.log", true);
            writer.write(">> " + line + "\n");
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
