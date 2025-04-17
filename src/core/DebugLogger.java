package src.core;

import java.io.FileWriter;
import java.io.IOException;

public class DebugLogger {

    public static String debug_path = "";

    public static void logIn(String line) {
        if (debug_path.isEmpty()) return;
        try {
            FileWriter writer = new FileWriter(debug_path, true);
            writer.write("<< " + line + "\n");
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void logOut(String line) {
        System.out.println(line);

        if (debug_path.isEmpty()) return;
        try {
            FileWriter writer = new FileWriter(debug_path, true);
            writer.write(">> " + line + "\n");
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
