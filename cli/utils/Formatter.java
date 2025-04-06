package cli.utils;

public class Formatter {
    public static void printHeader(String[] rows, int buffer) {
        int longestRowLength = calculateLongestLength(rows);
        int totalWidth = longestRowLength + buffer;
        
        // Print top border
        System.out.print("╔");
        printChar('═', totalWidth);
        System.out.println("╗");
        
        // Print centered rows
        printRows(rows, buffer);
        
        // Print bottom border
        System.out.print("╚");
        printChar('═', totalWidth);
        System.out.println("╝");
    }

    public static void printMenu(String[] rows, int buffer) {
        int longestRowLength = calculateLongestLength(rows);
        int totalWidth = longestRowLength + buffer;
        
        // Print top border
        System.out.print("┌");
        printChar('─', totalWidth);
        System.out.println("┐");
        
        // Print centered rows
        for (String row : rows) {
            System.out.print("│");
            
            // Calculate left padding for centering
            int totalPadding = totalWidth - row.length();
            int leftPadding = totalPadding / 2;
            int rightPadding = totalPadding - leftPadding;
            
            printSpaces(leftPadding);
            System.out.print(row);
            printSpaces(rightPadding);
            
            System.out.println("│");
        }
        
        // Print bottom border
        System.out.print("└");
        printChar('─', totalWidth);
        System.out.println("┘");
    }

    private static void printRows(String[] rows, int buffer) {
        int longestRowLength = calculateLongestLength(rows);
        
        // Print each row centered
        for (String row : rows) {
            System.out.print("║");
            
            // Calculate left padding for centering
            int totalPadding = longestRowLength + buffer - row.length();
            int leftPadding = totalPadding / 2;
            int rightPadding = totalPadding - leftPadding;
            
            printSpaces(leftPadding);
            System.out.print(row);
            printSpaces(rightPadding);
            
            System.out.println("║");
        }
    }

    private static int calculateLongestLength(String[] rows) {
        int longestRowLength = 0;
        for (String row : rows) {
            if (row.length() > longestRowLength) {
                longestRowLength = row.length();
            }
        }
        return longestRowLength;
    }

    private static void printSpaces(int length) {
        printChar(' ', length);
    }

    private static void printChar(char c, int length) {
        for (int i = 0; i < length; i++) {
            System.out.print(c);
        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}