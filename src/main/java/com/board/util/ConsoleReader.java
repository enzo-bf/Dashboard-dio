package com.board.util;

import java.util.Scanner;

public class ConsoleReader implements AutoCloseable {

    private final Scanner scanner;

    public ConsoleReader() {
        this.scanner = new Scanner(System.in);
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException exception) {
                System.out.println("Informe um número inteiro válido.");
            }
        }
    }

    public long readLong(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException exception) {
                System.out.println("Informe um número válido.");
            }
        }
    }

    public boolean confirm(String prompt) {
        String answer = readLine(prompt + " (s/n): ").toLowerCase();
        return answer.equals("s") || answer.equals("sim");
    }

    @Override
    public void close() {
        scanner.close();
    }
}
