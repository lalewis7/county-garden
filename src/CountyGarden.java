package src;
import java.sql.*;
import java.util.*;

public class CountyGarden {

    public static final String[] MONTHS = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};

    public static final boolean DEBUG_MODE = false;

    public static void main(String[] args) {

        System.out.println("--- County Garden Insurance ---");

        Scanner scan = new Scanner(System.in);

        Connection conn;

        // login
        while (true){

            // get username
            System.out.print("Enter user (root): ");
            String username = scan.nextLine();

            // get password
            System.out.print("Enter password for " + username + " (password): ");
            String password = scan.nextLine();

            try {
                // conn = DriverManager.getConnection(("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241"), username, password);
                conn = DriverManager.getConnection(String.format("jdbc:mysql://localhost:3100/county_garden?user=%s&password=%s", username, password));
                break;
            } catch (SQLException e) {
                System.out.println("Error: username/password incorrect. Please try again.");
            }
        }

        // menu
        while (true){
            int uiOption = menu("What user interface would you like?", new String[]{"Customer", "Agent", "Adjuster", "Quit"}, scan);
            if (uiOption == 0){ // customer
                CustomerUI ui = new CustomerUI(scan, conn);
                ui.start();
            }
            else if (uiOption == 1){ // agent
                AgentUI ui = new AgentUI(scan, conn);
                ui.start();
            }
            else if (uiOption == 2){ // adjuster
                AdjusterUI ui = new AdjusterUI(scan, conn);
                ui.start();
            }
            else {
                break;
            }
        }

        scan.close();

    }

    public static int menu(String question, String[] options, Scanner scan){
        do {
            if (question != null)
                System.out.println(question);
            for (int i = 0; i < options.length; i++){
                System.out.println((i+1) + ": " + options[i]);
            }
            try {
                int num = Integer.parseInt(scan.nextLine());
                if (num > 0 && num < options.length + 1)
                    return num-1;
                else
                    throw new Exception("Out of bounds");
            } catch (Exception e) {
                System.out.println("Invalid input. Please try again.");
            }
        } while(true);
    }

    public static boolean question(String question, Scanner scan){
        System.out.print(question+" (Y/n): ");
        return scan.nextLine().equalsIgnoreCase("y");
    }

    public static int number(String question, Scanner scan){
        do {
            System.out.print(question+": ");
            try {
                int num = Integer.parseInt(scan.nextLine());
                return num;
            } catch (Exception e) {
                System.out.println("Invalid input. Please try again.");
            }
        } while(true);
    }

    public static long getLong(String question, Scanner scan){
        do {
            System.out.print(question+": ");
            try {
                long num = Long.parseLong(scan.nextLine());
                return num;
            } catch (Exception e) {
                System.out.println("Invalid input. Please try again.");
            }
        } while(true);
    }

    public static String string(String question, Scanner scan, int minchar, int maxchar){
        do {
            System.out.print(question+": ");
            String answer = scan.nextLine();
            if (answer.length() < minchar || answer.length() > maxchar){
                if (minchar == maxchar)
                    System.out.println("Invalid input. Value must be " + minchar + " characters long.");
                else
                    System.out.println("Invalid input. Value must be " + minchar + "-" + maxchar + " characters long.");
                continue;
            }
            return answer;
        } while (true);
    }

    public static String date(String question, Scanner scan){
        do {
            System.out.print(question+"(dd-MON-yy): ");
            String answer = scan.nextLine();
            String[] values = answer.split("-");
            if (values.length != 3){
                System.out.println("Invalid input. Please try again.");
                continue;
            }
            if (values[0].length() != 2 || values[1].length() != 3 || values[2].length() != 2){
                System.out.println("Invalid input. Please try again.");
                continue;
            }
            try{
                int day = Integer.parseInt(values[0]);
                int year = Integer.parseInt(values[2]);
                if (day < -1 || day > 31){
                    System.out.println("Invalid input. Please try again.");
                    continue;
                }
                if (year < -1 || year > 99){
                    System.out.println("Invalid input. Please try again.");
                    continue;
                }
            } catch (Exception e){
                System.out.println("Invalid input. Please try again.");
                continue;
            }
            for (int i = 0; i < MONTHS.length; i++) {
                if (MONTHS[i].equalsIgnoreCase(values[1]))
                    return answer;
            }
            System.out.println("Invalid input. Please try again.");
        } while (true);
    }

    public static String[] stringlist(String value, Scanner scan, int minchar, int maxchar){
        LinkedList<String> list = new LinkedList<String>();
        do {
            if (question("Would you like to add another " + value + "?", scan))
                list.add(string(value, scan, minchar, maxchar));
            else
                break;
        } while(true);
        return list.toArray(new String[list.size()]);
    }

    public static void handleError(Exception e){
        if (CountyGarden.DEBUG_MODE)
            e.printStackTrace();
        else
            System.out.println("An error has occurred. Please try again later.");
    }

}