package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Patient extends User {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private Date dateOfBirth;
    private boolean isHivPositive;
    private Date dateOfInfection;
    private boolean onARTDrugs;
    private Date startARTDate;
    private String country;
    private Date demiseDate;
    private String uuid;

    // Default constructor
    public Patient() {
        super();
        setAccessType();
    }

    // Parameterized constructor
    public Patient(String uuid, String firstName, String lastName, String email, String password, Date dateOfBirth, boolean isHivPositive, Date dateOfInfection, boolean onARTDrugs, Date startARTDate, String country, Date demiseDate) {
        super(firstName, lastName, email, password);
        setAccessType();
        this.uuid = uuid;
        this.dateOfBirth = dateOfBirth;
        this.isHivPositive = isHivPositive;
        this.dateOfInfection = dateOfInfection;
        this.onARTDrugs = onARTDrugs;
        this.startARTDate = startARTDate;
        this.country = country;
        this.demiseDate = demiseDate;
    }

    // Getters and Setters
    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public boolean getIsHivPositive() { return isHivPositive; }
    public void setHivPositive(boolean hivPositive) { isHivPositive = hivPositive; }
    public Date getDateOfInfection() { return dateOfInfection; }
    public void setDateOfInfection(Date dateOfInfection) { this.dateOfInfection = dateOfInfection; }
    public boolean getIsOnARTDrugs() { return onARTDrugs; }
    public void setOnARTDrugs(boolean onARTDrugs) { this.onARTDrugs = onARTDrugs; }
    public Date getStartARTDate() { return startARTDate; }
    public void setStartARTDate(Date startARTDate) { this.startARTDate = startARTDate; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Date getDemiseDate() { return demiseDate; }
    public void setDemiseDate(Date demiseDate) { this.demiseDate = demiseDate; }

    public void modifyProfile() {
        // Display current profile information
        System.out.println("Current Profile Information:");
        viewProfile();  // Assuming viewProfile prints the current profile information
    
        // Prompt for new details
        Scanner scanner = new Scanner(System.in);
    
        System.out.print("Enter new first name (or press Enter to keep current): ");
        String firstName = scanner.nextLine();
        if (firstName.isEmpty()) {
            firstName = getFirstName();
        }
    
        System.out.print("Enter new last name (or press Enter to keep current): ");
        String lastName = scanner.nextLine();
        if (lastName.isEmpty()) {
            lastName = getLastName();
        }
    
        System.out.print("Enter new date of birth (yyyy-MM-dd) or press Enter to keep current: ");
        String dobString = scanner.nextLine();
        Date dateOfBirth = dobString.isEmpty() ? getDateOfBirth() : parseDate(dobString);
    
        System.out.print("Is HIV Positive (true/false) or press Enter to keep current: ");
        String hivPositiveString = scanner.nextLine();
        boolean isHivPositive = hivPositiveString.isEmpty() ? getIsHivPositive() : Boolean.parseBoolean(hivPositiveString);
    
        System.out.print("Enter new date of infection (yyyy-MM-dd) or press Enter to keep current: ");
        String doiString = scanner.nextLine();
        Date dateOfInfection = doiString.isEmpty() ? getDateOfInfection() : parseDate(doiString);
    
        System.out.print("On ART Drugs (true/false) or press Enter to keep current: ");
        String onARTDrugsString = scanner.nextLine();
        boolean onARTDrugs = onARTDrugsString.isEmpty() ? getIsOnARTDrugs() : Boolean.parseBoolean(onARTDrugsString);
    
        System.out.print("Enter new start ART date (yyyy-MM-dd) or press Enter to keep current: ");
        String artStartDateString = scanner.nextLine();
        Date startARTDate = artStartDateString.isEmpty() ? getStartARTDate() : parseDate(artStartDateString);
    
        System.out.print("Enter new country or press Enter to keep current: ");
        String country = scanner.nextLine();
        if (country.isEmpty()) {
            country = getCountry();
        }
    
        // Print the new user information for debugging
        System.out.println("New User Information:");
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Date of Birth: " + (dateOfBirth != null ? DATE_FORMAT.format(dateOfBirth) : "Not changed"));
        System.out.println("Is HIV Positive: " + isHivPositive);
        System.out.println("Date of Infection: " + (dateOfInfection != null ? DATE_FORMAT.format(dateOfInfection) : "Not changed"));
        System.out.println("On ART Drugs: " + onARTDrugs);
        System.out.println("Start ART Date: " + (startARTDate != null ? DATE_FORMAT.format(startARTDate) : "Not changed"));
        System.out.println("Country: " + country);
    
        // Call the script to update the profile
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath != null) {
                executeScript(scriptPath, "modify-patient-profile", uuid, firstName, lastName, 
                              dateOfBirth != null ? DATE_FORMAT.format(dateOfBirth) : "", 
                              String.valueOf(isHivPositive), 
                              dateOfInfection != null ? DATE_FORMAT.format(dateOfInfection) : "", 
                              String.valueOf(onARTDrugs), 
                              startARTDate != null ? DATE_FORMAT.format(startARTDate) : "", 
                              country);
                System.out.println("Profile updated successfully.");
            } else {
                System.out.println("Script not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private Date parseDate(String dateString) {
        try {
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please enter date in yyyy-MM-dd format.");
            return null;
        }
    }

    public void viewProfile() {
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath != null) {
                String response = executeScript(scriptPath, "view-profile", uuid);
                System.out.println(response);
            } else {
                System.out.println("Script not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void computeLifeExpectancy() {
        // Implementation
    }

    @Override
    public void setAccessType(){
        this.accessType = UserRole.PATIENT;
    }

    public String completeRegistration() {
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath != null) {
                String response = executeScript(scriptPath, "complete-registration", uuid, getFirstName(), getLastName(), new SimpleDateFormat("yyyy-MM-dd").format(dateOfBirth), String.valueOf(isHivPositive), dateOfInfection != null ? new SimpleDateFormat("yyyy-MM-dd").format(dateOfInfection) : "", String.valueOf(onARTDrugs), startARTDate != null ? new SimpleDateFormat("yyyy-MM-dd").format(startARTDate) : "", country, getPassword());
                return response;
            } else {
                return "Script not found.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error completing registration.";
        }
    }

    @Override
    public String toString() {
        return "Patient{" +
                "firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", accessType=" + getAccessType() +
                ", dateOfBirth=" + dateOfBirth +
                ", isHivPositive=" + isHivPositive +
                ", dateOfInfection=" + dateOfInfection +
                ", onARTDrugs=" + onARTDrugs +
                ", startARTDate=" + startARTDate +
                ", country='" + country + '\'' +
                ", demiseDate=" + demiseDate +
                '}';
    }

    // Method to find the script path
    public String findScript(String scriptName) throws IOException {
        File currentDir = new File(".");
        return searchForScript(currentDir, scriptName);
    }

    private String searchForScript(File directory, String scriptName) throws IOException {
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

            // Capture errors from the script
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                response.append(line).append(System.lineSeparator());
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                response.append("Script exited with error code: ").append(exitCode).append(System.lineSeparator());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error executing script.";
        }
        
        return response.toString();
    }
}
