package src;

import java.util.Scanner;

import src.core.Command;

public class Main {

    public static void main(String args[]) {
        
        System.out.println("starting...");
        boolean exit = false;

        Scanner scanner = new Scanner(System.in);
        String command;

        while (!exit) {
            command = scanner.nextLine();
            if (command == "exit") {
                exit = true;
            }
            Command.parseCommand(command);
        }
        scanner.close();

    }

}