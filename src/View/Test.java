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

    public static void main(String[] args) throws ParseException {
        while (true) {
            clearScreen();
            displayWelcomeBanner();
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║                                              ║");
            System.out.println("║   Welcome to Life Prognosis Management Tool  ║");
            System.out.println("║                                              ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║                                              ║");
            System.out.println("║    [1] Log in                                ║");
            System.out.println("║                                              ║");
            System.out.println("║    [2] Complete Registration                 ║");
            System.out.println("║                                              ║");
            System.out.println("║    [3] Exit                                  ║");
            System.out.println("║                                              ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println();
            System.out.print("  Your choice: ");
            String choice = scanner.nextLine();


            switch (choice) {
                case "1":
                    if (login()) {
                        if (currentAdmin != null) {
                            adminMenu();
                        } else if (currentPatient != null) {
                            patientMenu();
                        }
                    }
                    break;
                case "2":
                    registerPatient();
                    break;
                case "3":
                    clearScreen();
                    System.out.println("  Exiting...");
                    System.exit(0);
                    break;
                default:
                    System.out.println();
                    System.out.println("  Invalid choice, please try again.");
                    pressEnterToContinue();
            }
        }
    }

    public static void displayWelcomeBanner() {
        String[] bannerLines = {
            "         __      ____    _  __    _________",
            "        / /     / __ \\  / \\/  \\  /___  ___/",
            "       / /___  / /_/_/ / /\\_/\\ \\   / /",
            "      /_____/ /_/     /_/     \\_\\ /_/\n",
            ""
        };
    
        for (String line : bannerLines) {
            for (char c : line.toCharArray()) {
                System.out.print(c);
                try {
                    Thread.sleep(15); // Delay to create the animation effect
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println(); // Move to the next line after printing the current one
        }
        
        System.out.println(); // Extra line break after the banner
    }
    
    private static boolean login() {
        clearScreen();
        System.out.println("         __      ____    _  __    _________");
        System.out.println("        / /     / __ \\  / \\/  \\  /___  ___/");
        System.out.println("       / /___  / /_/_/ / /\\_/\\ \\   / /");
        System.out.println("      /_____/ /_/     /_/     \\_\\ /_/\n");
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║                                              ║");
        System.out.println("║    Log in to Life Prognosis Management Tool  ║");
        System.out.println("║                                              ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
        System.out.print("  Email: ");
        String email = scanner.nextLine();
        String password = readPassword("  Password: ");

        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath == null) {
                System.out.println();
                System.out.println("  Script not found.");
                pressEnterToContinue();
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
                currentPatient = new Patient(storedUuid, "", "", email, "", null, "false", null, "false", null, "", null,null);
                return true;
            } else {
                System.out.println();
                System.out.println("  " + response);
                pressEnterToContinue();
                return false;
            }
        } catch (IOException e) {
            System.out.println();
            System.out.println("  System error, please contact the administrator");
            pressEnterToContinue();
            return false;
        }
    }
    
    private static boolean isValidName(String name) {
        return name.matches("^[A-Za-z]+([\\s'-][A-Za-z]+)*$");
    }

    private static void modifyProfile() throws ParseException {
        // Display current profile information
        currentPatient.viewProfile();
    
        // Prompt for new details
        Scanner scanner = new Scanner(System.in);
        String doiString = "";
        String artStartDateString = "";
        String onARTDrugsString = "";
    
        System.out.print("  Enter new first name (or press Enter to keep current): ");
        String firstName = scanner.nextLine();
        while (!firstName.isEmpty() && !isValidName(firstName)) {
            System.out.println("  Invalid input. Name should only contain letters. Please enter again: ");
            firstName = scanner.nextLine().trim();
        }
    
        System.out.print("  Enter new last name (or press Enter to keep current): ");
        String lastName = scanner.nextLine();
        while (!lastName.isEmpty() && !isValidName(lastName)) {
            System.out.println("  Invalid input. Name should only contain letters. Please enter again: ");
            lastName = scanner.nextLine().trim();
        }
    
        System.out.print("  Enter new date of birth (yyyy-MM-dd) or press Enter to keep current: ");
        String dobString = scanner.nextLine();
        while (!dobString.isEmpty() && parseDate(dobString) == null) {
            System.out.print("  Invalid date format. Please enter Date of Birth (yyyy-MM-dd): ");
            dobString = scanner.nextLine();
        }
    
        System.out.print("  Is HIV Positive (true/false) or press Enter to keep current: ");
        String hivPositiveString = scanner.nextLine();
        while (!hivPositiveString.isEmpty() && parseBoolean(hivPositiveString) == null) {
            System.out.print("  Invalid input. Please enter true or false for HIV Positive: ");
            hivPositiveString = scanner.nextLine();
        }
    
        Boolean enterDateOfInfection = ((!hivPositiveString.isEmpty() && hivPositiveString.equals("true")) || (hivPositiveString.isEmpty() && currentPatient.getIsHivPositive().equals("true")));
        if(enterDateOfInfection){
            System.out.print("  Enter new date of infection (yyyy-MM-dd) or press Enter to keep current: ");
            doiString = scanner.nextLine();
            while (!doiString.isEmpty() && (parseDate(doiString) == null || parseDate(doiString).before((!dobString.isEmpty()) ? parseDate(dobString) : currentPatient.getDateOfBirth()))) {
                System.out.print("  Invalid date format. Please enter Date of Birth (yyyy-MM-dd): ");
                doiString = scanner.nextLine();
            }
            System.out.print("  On ART Drugs (true/false) or press Enter to keep current: ");
            onARTDrugsString = scanner.nextLine();
            while (!onARTDrugsString.isEmpty() && parseBoolean(onARTDrugsString) == null) {
                System.out.print("  Invalid input. Please enter true or false for HIV Positive: ");
                onARTDrugsString = scanner.nextLine();
            }
        
            Boolean enterArtStartDate = ((!onARTDrugsString.isEmpty() && onARTDrugsString.equals("true")) || (onARTDrugsString.isEmpty() && currentPatient.getIsOnARTDrugs().equals("true")));
            if(enterArtStartDate){
                System.out.print("  Enter new start ART date (yyyy-MM-dd) or press Enter to keep current: ");
                artStartDateString = scanner.nextLine();
                while (!artStartDateString.isEmpty() && (parseDate(artStartDateString) == null || parseDate(artStartDateString).before((!doiString.isEmpty()) ? parseDate(doiString) : currentPatient.getDateOfInfection()))) {
                    System.out.print("  Invalid date format. Please enter Date of Birth (yyyy-MM-dd): ");
                    artStartDateString = scanner.nextLine();
                }
            }
        }
        
        System.out.print("  Enter new country or press Enter to keep current: ");
        String country = scanner.nextLine();
        while (!country.isEmpty() && getCountryLifeExpectancy(country).equals("Invalid country code")) {
            System.out.print("  Invalid country code. Please enter again using Alpha-3 code: ");
            country = scanner.nextLine().trim();
        }
    
        // Print the new user information for debugging
        System.out.println();
        System.out.println("  New User Information");
        System.out.println("  ════════════════════");
        System.out.println();
        System.out.println("  First Name: " + (!firstName.isEmpty() ? firstName : "Not changed"));
        System.out.println("  Last Name: " + (!lastName.isEmpty() ? lastName : "Not changed"));
        System.out.println("  Date of Birth: " + (!dobString.isEmpty() ? dobString : "Not changed"));
        System.out.println("  Is HIV Positive: " + (!hivPositiveString.isEmpty() ? parseBoolean(hivPositiveString) : "Not changed"));
        System.out.println("  Date of Infection: " + (!doiString.isEmpty() ? doiString : "Not changed"));
        System.out.println("  On ART Drugs: " + (!onARTDrugsString.isEmpty() ? parseBoolean(onARTDrugsString) : "Not changed"));
        System.out.println("  Start ART Date: " + (!artStartDateString.isEmpty() ? artStartDateString : "Not changed"));
        System.out.println("  Country: " + (!country.isEmpty() ? country : "Not changed"));
        Patient updatePatient = new Patient(currentPatient.getUuid(), firstName, lastName, "", "", (!dobString.isEmpty() ? parseDate(dobString) : null), (!hivPositiveString.isEmpty() ? parseBoolean(hivPositiveString) : ""),
        (!doiString.isEmpty() ? parseDate(doiString) : null), (!onARTDrugsString.isEmpty() ? parseBoolean(onARTDrugsString) : ""), (!artStartDateString.isEmpty() ? parseDate(artStartDateString) : null), country, null,null);
        updatePatient.modifyProfile();
    }

    private static void registerPatient() {
        clearScreen();

        System.out.print("  Enter UUID Code: ");
        String uuid_code = scanner.nextLine().trim();
        while (uuid_code.isEmpty()) {
            System.out.print("  UUID Code cannot be empty. Please enter again: ");
            uuid_code = scanner.nextLine().trim();
        }

        // Validate the UUID and get the associated email
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath == null) {
                System.out.println();
                System.out.println("  Script not found.");
                return;
            }

            String response = executeScript(scriptPath, "validate-uuid", uuid_code);
            if (response.startsWith("Invalid") || response.isEmpty()) {
                System.out.println();
                System.out.println("  " + response);
                pressEnterToContinue();
                return;
            }

            String email = response.trim();
            System.out.println("  Complete registration for user with email: " + email);

            System.out.print("  First Name: ");
            String firstName = scanner.nextLine().trim();
            while (firstName.isEmpty() || !isValidName(firstName)) {
                if(!isValidName(firstName)){
                    System.out.println("  Invalid input. Name should only contain letters. Please enter again: ");
                }else{
                    System.out.print("  First Name cannot be empty. Please enter again: ");
                }
                firstName = scanner.nextLine().trim();
            }

            System.out.print("  Last Name: ");
            String lastName = scanner.nextLine().trim();
            while (lastName.isEmpty() || !isValidName(lastName)) {
                if(!isValidName(lastName)){
                    System.out.println("  Invalid input. Name should only contain letters. Please enter again: ");
                }else{
                    System.out.print("  Last Name cannot be empty. Please enter again: ");
                }
                lastName = scanner.nextLine().trim();
            }
        
            String password = readPassword("  Password: ");
            while (password.isEmpty()) {
                System.out.print("  Password cannot be empty. Please enter again: ");
                password = readPassword("  Password: ");
            }
        
            System.out.print("  Date of Birth (yyyy-MM-dd): ");
            Date dateOfBirth = parseDate(scanner.nextLine().trim());
            while (dateOfBirth == null) {
                System.out.print("  Invalid date format. Please enter Date of Birth (yyyy-MM-dd): ");
                dateOfBirth = parseDate(scanner.nextLine().trim());
            }
        
            System.out.print("  Is HIV Positive (true/false): ");
            String isHivPositive = parseBoolean(scanner.nextLine().trim());
            while (isHivPositive == null) {
                System.out.print("  Invalid input. Please enter true or false for HIV Positive: ");
                isHivPositive = parseBoolean(scanner.nextLine().trim());
            }
        
            Date dateOfInfection = null;
            if (isHivPositive.equals("true")) {
                System.out.print("  Date of Infection (yyyy-MM-dd): ");
                dateOfInfection = parseDate(scanner.nextLine().trim());
                while (dateOfInfection == null || dateOfInfection.before(dateOfBirth)) {
                    if(dateOfInfection == null){
                        System.out.print("  Invalid date format. Please enter Date of Infection (yyyy-MM-dd): ");
                        dateOfInfection = parseDate(scanner.nextLine().trim());
                    }else{
                        System.out.print("  Invalid date. Date of infection should be after the date of birth: ");
                        dateOfInfection = parseDate(scanner.nextLine().trim());
                    }
                }
            }
        
            System.out.print("  On ART Drugs (true/false): ");
            String onARTDrugs = parseBoolean(scanner.nextLine().trim());
            while (onARTDrugs == null) {
                System.out.print("  Invalid input. Please enter true or false for On ART Drugs: ");
                onARTDrugs = parseBoolean(scanner.nextLine().trim());
            }
        
            Date startARTDate = null;
            if (onARTDrugs.equals("true")) {
                System.out.print("  Start ART Date (yyyy-MM-dd): ");
                startARTDate = parseDate(scanner.nextLine().trim());
                while (startARTDate == null || startARTDate.before(dateOfInfection)) {
                    if(startARTDate == null){
                        System.out.print("  Invalid date format. Please enter Start ART Date (yyyy-MM-dd): ");
                        startARTDate = parseDate(scanner.nextLine().trim());
                    }else{
                        System.out.print("  Invalid date. Start ART Date should be after the Date of infection: ");
                        startARTDate = parseDate(scanner.nextLine().trim());
                    }
                }
            }
        
            System.out.print("  Country: ");
            String country = scanner.nextLine().trim();
            while (country.isEmpty() || getCountryLifeExpectancy(country).equals("Invalid country code")) {
                if(country.isEmpty()){
                    System.out.print("  Country cannot be empty. Please enter again: ");
                }else{
                    System.out.print("  Invalid country code. Please enter again using Alpha-3 code: ");
                }
                country = scanner.nextLine().trim();
            }
        
            currentPatient = new Patient(uuid_code, firstName, lastName, null, password, dateOfBirth, isHivPositive,
                    dateOfInfection, onARTDrugs, startARTDate, country, null,null);
        
            response = currentPatient.completeRegistration();
            System.out.println();
            System.out.println("  " + response);
            pressEnterToContinue();
        } catch (IOException e) {
            System.out.println("  System error, please contact the administrator");
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
    
    private static String parseBoolean(String boolStr) {
        if (boolStr.equalsIgnoreCase("true") || boolStr.equalsIgnoreCase("yes") || boolStr.equalsIgnoreCase("y")) {
            return "true";
        } else if (boolStr.equalsIgnoreCase("false") || boolStr.equalsIgnoreCase("no") || boolStr.equalsIgnoreCase("n")) {
            return "false";
        } else {
            return null;
        }
    }

    private static void adminMenu() {
        while (true) {
            clearScreen();
            System.out.println("         __      ____    _  __    _________");
            System.out.println("        / /     / __ \\  / \\/  \\  /___  ___/");
            System.out.println("       / /___  / /_/_/ / /\\_/\\ \\   / /");
            System.out.println("      /_____/ /_/     /_/     \\_\\ /_/\n");
            System.out.println();
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║                                              ║");
            System.out.println("║                   Admin Menu                 ║");
            System.out.println("║                                              ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║                                              ║");
            System.out.println("║   [1] Get User Data                          ║");
            System.out.println("║                                              ║");
            System.out.println("║   [2] Aggregate User Data                    ║");
            System.out.println("║                                              ║");
            System.out.println("║   [3] Initiate Registration                  ║");
            System.out.println("║                                              ║");
            System.out.println("║   [4] Logout                                 ║");
            System.out.println("║                                              ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println();
            System.out.print("  Your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    clearScreen();
                    currentAdmin.exportUserData();
                    pressEnterToContinue();
                    break;
                case "2":
                    clearScreen();
                    currentAdmin.calculateSurvivalMetrics();
                    pressEnterToContinue();
                    break;
                case "3":
                    clearScreen();
                    currentAdmin.initiateRegistration("ADMIN");
                    pressEnterToContinue();
                    break;
                case "4":
                    currentAdmin.logout();
                    currentAdmin = null;
                    return;
                default:
                    System.out.println();
                    System.out.println("  Invalid choice, please try again.");
                    pressEnterToContinue();
            }
        }
    }

    private static void patientMenu() throws ParseException {
        while (true) {
            clearScreen();
            System.out.println("         __      ____    _  __    _________");
            System.out.println("        / /     / __ \\  / \\/  \\  /___  ___/");
            System.out.println("       / /___  / /_/_/ / /\\_/\\ \\   / /");
            System.out.println("      /_____/ /_/     /_/     \\_\\ /_/\n");
            System.out.println();
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║                                              ║");
            System.out.println("║                  Patient Menu                ║");
            System.out.println("║                                              ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║                                              ║");
            System.out.println("║    [1] Modify Profile                        ║");
            System.out.println("║                                              ║");
            System.out.println("║    [2] View Profile                          ║");
            System.out.println("║                                              ║");
            System.out.println("║    [3] Generate Icalendar                    ║");
            System.out.println("║                                              ║");
            System.out.println("║    [4] Logout                                ║");
            System.out.println("║                                              ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println();
            System.out.print("  Your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    clearScreen();
                    modifyProfile();
                    pressEnterToContinue();
                    break;
                case "2":
                    clearScreen();
                    currentPatient.viewProfile();
                    pressEnterToContinue();
                    break;
                case "3":
                    clearScreen();
                    currentPatient.generateICalendar();
                    pressEnterToContinue();
                    break;
                case "4":
                    currentPatient.logout();
                    currentPatient = null;
                    return;    
                default:
                    System.out.println();
                    System.out.println("  Invalid choice, please try again.");
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
        System.out.println();
        System.out.println("  Press Enter to continue...");
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
                System.err.println("  Error: " + errorLine);
            }

            // Wait for the script to finish
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("  Script exited with error code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
        return response.toString().trim(); // Trim to remove any trailing new lines
    }
}




