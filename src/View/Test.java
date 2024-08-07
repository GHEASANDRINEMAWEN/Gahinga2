package View;

import Model.*;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Test {
    private static final Scanner scanner = new Scanner(System.in);
    private static Admin currentAdmin = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        while (true) {
            clearScreen();
            System.out.println("Welcome to the Health Management System");
            System.out.println("1. Log in");
            System.out.println("2. Complete Registration");
            System.out.println("3. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    if (login()) {
                        if (currentAdmin != null) {
                            adminMenu();
                        } else if (currentPatient != null) {
                            patientMenu();
                        }
                    }
                    break;
                case 2:
                    registerPatient();
                    break;
                case 3:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }

    private static boolean login() {
        clearScreen();
        System.out.println("Log in to the Health Management System");
        System.out.print("Email: ");
        String email = scanner.nextLine();
        String password = readPassword("Password: ");

        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath == null) {
                System.out.println("Script not found.");
                return false;
            }
            
            String response = executeScript(scriptPath, "login", email, password);
            
            // Check if the user is an admin or patient
            if (response.startsWith("ADMIN")) {
                currentAdmin = new Admin("", "", email, "");
                return true;
            } else if (response.startsWith("PATIENT")) {
                String[] parts = response.split(",");
                String storedUuid = parts[1];
                currentPatient = new Patient(storedUuid, "", "", email, "", null, false, null, false, null, "", null);
                return true;
            } else {
                System.out.println("Invalid email or password.");
                pressEnterToContinue();
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    private static void registerPatient() {
        clearScreen();
        System.out.println("Register as Patient");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();
        String password = readPassword("Password: ");
    
        System.out.print("Date of Birth (yyyy-MM-dd): ");
        Date dateOfBirth = parseDate(scanner.nextLine());
    
        System.out.print("Is HIV Positive (true/false): ");
        boolean isHivPositive = scanner.nextBoolean();
        scanner.nextLine(); // consume newline
    
        Date dateOfInfection = null;
        if (isHivPositive) {
            System.out.print("Date of Infection (yyyy-MM-dd): ");
            dateOfInfection = parseDate(scanner.nextLine());
        }
    
        System.out.print("On ART Drugs (true/false): ");
        boolean onARTDrugs = scanner.nextBoolean();
        scanner.nextLine(); // consume newline
    
        Date startARTDate = null;
        if (onARTDrugs) {
            System.out.print("Start ART Date (yyyy-MM-dd): ");
            startARTDate = parseDate(scanner.nextLine());
        }
    
        System.out.print("Country: ");
        String country = scanner.nextLine();
    
        System.out.print("Enter UUID Code: ");
        String uuid_code = scanner.nextLine(); // Ensure this captures the correct input
    
        currentPatient = new Patient(uuid_code, firstName, lastName, null, password, dateOfBirth, isHivPositive,
                dateOfInfection, onARTDrugs, startARTDate, country, null);
    
        String response = currentPatient.completeRegistration();
    
        System.out.println(response);
        pressEnterToContinue();
    }    

    private static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please enter in yyyy-MM-dd format.");
            return null;
        }
    }

    private static void adminMenu() {
        while (true) {
            clearScreen();
            System.out.println("Admin Menu");
            System.out.println("1. Delete Users");
            System.out.println("2. Export User Data");
            System.out.println("3. Aggregate User Data");
            System.out.println("4. Initiate Registration");
            System.out.println("5. Get All Users");
            System.out.println("6. Logout");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    currentAdmin.deleteUsers();
                    break;
                case 2:
                    currentAdmin.exportUserData();
                    break;
                case 3:
                    currentAdmin.aggregateUserData();
                    break;
                case 4:
                    clearScreen();
                    currentAdmin.initiateRegistration("ADMIN");
                    pressEnterToContinue();
                    break;
                case 5:
                    currentAdmin.getAllUsers();
                    break;
                case 6:
                    currentAdmin = null;
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
                    pressEnterToContinue();
            }
        }
    }

    private static void patientMenu() {
        while (true) {
            clearScreen();
            System.out.println("Patient Menu");
            System.out.println("1. Modify Profile");
            System.out.println("2. View Profile");
            System.out.println("3. Compute Life Expectancy");
            System.out.println("4. Logout");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    currentPatient.modifyProfile();
                    pressEnterToContinue();
                    break;
                case 2:
                    currentPatient.viewProfile();
                    pressEnterToContinue();
                    break;
                case 3:
                    currentPatient.computeLifeExpectancy();
                    pressEnterToContinue();
                    break;
                case 4:
                    currentPatient = null;
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
                    pressEnterToContinue();
            }
        }
    }

    private static String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] passwordArray = console.readPassword(prompt);
            return new String(passwordArray);
        } else {
            System.out.print(prompt);
            return scanner.nextLine();
        }
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void pressEnterToContinue() {
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    public static String findScript(String scriptName) throws IOException {
        File currentDir = new File(".");
        return searchForScript(currentDir, scriptName);
    }

    private static String searchForScript(File directory, String scriptName) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String result = searchForScript(file, scriptName);
                    if (result != null) {
                        return result;
                    }
                } else if (file.getName().equals(scriptName)) {
                    return file.getCanonicalPath();
                }
            }
        }
        return null;
    }

    public static String executeScript(String... command) {
        StringBuilder response = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        
        try {
            Process process = processBuilder.start();
            
            // Capture output from the script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append(System.lineSeparator());
            }

            // Capture any errors from the script
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("Error: " + errorLine);
            }

            // Wait for the script to finish
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Script exited with error code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
        return response.toString().trim(); // Trim to remove any trailing new lines
    }
}
