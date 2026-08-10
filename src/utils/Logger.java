package src.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Logger {
    private static PrintWriter fout;
    private static Scanner is = new Scanner(System.in);

    public static void init(String file_path) {
        if (fout != null) {
            fout.close();
        }

        if (file_path.isBlank() || file_path.equals("<empty>")) {
            fout = null;
            return;
        }

        try {
            fout = new PrintWriter(new FileWriter(file_path, true));
        } catch (IOException e) {
            Logger.outln("info string failed to open file... " + e.getMessage());
            fout = null;
        }
    }

    public static void outlnn(String string) {
        if (fout != null) {
            fout.println(string);
        }

        System.out.println(string);
    }

    public static void outln(String string) {
        if (fout != null) {
            fout.println("<< " + string);
        }

        System.out.println(string);
    }

    public static void out(String string) {
        if (fout != null) {
            fout.print("<< " + string);
        }

        System.out.print(string);
    }

    public static void outln() {
        outln("");
    }

    public static String in() {            
        String string = is.nextLine();
        if (fout != null) {
            fout.println(">> " + string);
        }
        return string;
    }

    public static void close() {
        if (fout != null) {
            fout.close();
        }

        is.close();
    }
}
