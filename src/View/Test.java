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
                System.out.println(response);
                return false;
            }
        } catch (IOException e) {
            System.out.println("System error, please contact the administrator");
            return false;
        }
    }
    
    private static boolean isValidName(String name) {
        return name.matches("^[A-Za-z]+([\\s'-][A-Za-z]+)*$");
    }

    private static void registerPatient() {
        clearScreen();

        System.out.print("Enter UUID Code: ");
        String uuid_code = scanner.nextLine().trim();
        while (uuid_code.isEmpty()) {
            System.out.print("UUID Code cannot be empty. Please enter again: ");
            uuid_code = scanner.nextLine().trim();
        }

        // Validate the UUID and get the associated email
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath == null) {
                System.out.println("Script not found.");
                return;
            }

            String response = executeScript(scriptPath, "validate-uuid", uuid_code);
            if (response.startsWith("Invalid") || response.isEmpty()) {
                System.out.println(response);
                pressEnterToContinue();
                return;
            }

            String email = response.trim();
            System.out.println("Complete registration for user with email: " + email);

            System.out.print("First Name: ");
            String firstName = scanner.nextLine().trim();
            while (firstName.isEmpty() || !isValidName(firstName)) {
                if(!isValidName(firstName)){
                    System.out.println("Invalid input. Name should only contain letters. Please enter again: ");
                }else{
                    System.out.print("First Name cannot be empty. Please enter again: ");
                }
                firstName = scanner.nextLine().trim();
            }

            System.out.print("Last Name: ");
            String lastName = scanner.nextLine().trim();
            while (lastName.isEmpty() || !isValidName(lastName)) {
                if(!isValidName(lastName)){
                    System.out.println("Invalid input. Name should only contain letters. Please enter again: ");
                }else{
                    System.out.print("First Name cannot be empty. Please enter again: ");
                }
                lastName = scanner.nextLine().trim();
            }
        
            String password = readPassword("Password: ");
            while (password.isEmpty()) {
                System.out.print("Password cannot be empty. Please enter again: ");
                password = readPassword("Password: ");
            }
        
            System.out.print("Date of Birth (yyyy-MM-dd): ");
            Date dateOfBirth = parseDate(scanner.nextLine().trim());
            while (dateOfBirth == null) {
                System.out.print("Invalid date format. Please enter Date of Birth (yyyy-MM-dd): ");
                dateOfBirth = parseDate(scanner.nextLine().trim());
            }
        
            System.out.print("Is HIV Positive (true/false): ");
            Boolean isHivPositive = parseBoolean(scanner.nextLine().trim());
            while (isHivPositive == null) {
                System.out.print("Invalid input. Please enter true or false for HIV Positive: ");
                isHivPositive = parseBoolean(scanner.nextLine().trim());
            }
        
            Date dateOfInfection = null;
            if (isHivPositive) {
                System.out.print("Date of Infection (yyyy-MM-dd): ");
                dateOfInfection = parseDate(scanner.nextLine().trim());
                while (dateOfInfection == null || dateOfInfection.before(dateOfBirth)) {
                    if(dateOfInfection == null){
                        System.out.print("Invalid date format. Please enter Date of Infection (yyyy-MM-dd): ");
                        dateOfInfection = parseDate(scanner.nextLine().trim());
                    }else{
                        System.out.print("Invalid date. Date of infection should be after the date of birth: ");
                        dateOfInfection = parseDate(scanner.nextLine().trim());
                    }
                }
            }
        
            System.out.print("On ART Drugs (true/false): ");
            Boolean onARTDrugs = parseBoolean(scanner.nextLine().trim());
            while (onARTDrugs == null) {
                System.out.print("Invalid input. Please enter true or false for On ART Drugs: ");
                onARTDrugs = parseBoolean(scanner.nextLine().trim());
            }
        
            Date startARTDate = null;
            if (onARTDrugs) {
                System.out.print("Start ART Date (yyyy-MM-dd): ");
                startARTDate = parseDate(scanner.nextLine().trim());
                while (startARTDate == null || startARTDate.before(dateOfInfection)) {
                    if(startARTDate == null){
                        System.out.print("Invalid date format. Please enter Start ART Date (yyyy-MM-dd): ");
                        startARTDate = parseDate(scanner.nextLine().trim());
                    }else{
                        System.out.print("Invalid date. Start ART Date should be after the Date of infection: ");
                        startARTDate = parseDate(scanner.nextLine().trim());
                    }
                }
            }
        
            System.out.print("Country: ");
            String country = scanner.nextLine().trim();
            while (country.isEmpty() || getCountryLifeExpectancy(country).equals("Invalid country code")) {
                if(country.isEmpty()){
                    System.out.print("Country cannot be empty. Please enter again: ");
                }else{
                    System.out.print("Invalid country code. Please enter again using Alpha-3 code: ");
                }
                country = scanner.nextLine().trim();
            }
        
            currentPatient = new Patient(uuid_code, firstName, lastName, null, password, dateOfBirth, isHivPositive,
                    dateOfInfection, onARTDrugs, startARTDate, country, null);
        
            response = currentPatient.completeRegistration();
            System.out.println(response);
            pressEnterToContinue();
        } catch (IOException e) {
            System.out.println("System error, please contact the administrator");
            pressEnterToContinue();
        }
    }
    
    private static String getCountryLifeExpectancy(String country){
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath != null) {
                String response = executeScript(scriptPath, "get-life-expectancy", country);
                return response;
            } else {
                return "Script not found.";
            }
        } catch (IOException e) {
            return "System error";
        }
    }

    private static Date parseDate(String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
    
    private static Boolean parseBoolean(String boolStr) {
        if (boolStr.equalsIgnoreCase("true") || boolStr.equalsIgnoreCase("yes") || boolStr.equalsIgnoreCase("y")) {
            return true;
        } else if (boolStr.equalsIgnoreCase("false") || boolStr.equalsIgnoreCase("no") || boolStr.equalsIgnoreCase("n")) {
            return false;
        } else {
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
                    pressEnterToContinue();
                    break;
                case 2:
                    currentAdmin.exportUserData();
                    pressEnterToContinue();
                    break;
                case 3:
                    currentAdmin.aggregateUserData();
                    pressEnterToContinue();
                    break;
                case 4:
                    clearScreen();
                    currentAdmin.initiateRegistration("ADMIN");
                    pressEnterToContinue();
                    break;
                case 5:
                    currentAdmin.getAllUsers();
                    pressEnterToContinue();
                    break;
                case 6:
                    currentAdmin.logout();
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
                    currentPatient.logout();
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
