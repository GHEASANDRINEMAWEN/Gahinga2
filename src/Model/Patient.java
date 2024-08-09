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
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private Date dateOfBirth;
    private String isHivPositive;
    private Date dateOfInfection;
    private String onARTDrugs;
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
    public Patient(String uuid, String firstName, String lastName, String email, String password, Date dateOfBirth, String isHivPositive, Date dateOfInfection, String onARTDrugs, Date startARTDate, String country, Date demiseDate) {
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
    public String getIsHivPositive() { return isHivPositive; }
    public void setHivPositive(String hivPositive) { isHivPositive = hivPositive; }
    public Date getDateOfInfection() { return dateOfInfection; }
    public void setDateOfInfection(Date dateOfInfection) { this.dateOfInfection = dateOfInfection; }
    public String getIsOnARTDrugs() { return onARTDrugs; }
    public void setOnARTDrugs(String onARTDrugs) { this.onARTDrugs = onARTDrugs; }
    public Date getStartARTDate() { return startARTDate; }
    public void setStartARTDate(Date startARTDate) { this.startARTDate = startARTDate; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Date getDemiseDate() { return demiseDate; }
    public void setDemiseDate(Date demiseDate) { this.demiseDate = demiseDate; }

    public void modifyProfile() {
        // Call the script to update the profile
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath != null) {
                executeScript(scriptPath, "modify-patient-profile", getUuid(), getFirstName(), getLastName(), 
                              getDateOfBirth() != null ? new SimpleDateFormat("yyyy-MM-dd").format(getDateOfBirth()) : "", 
                              getIsHivPositive(), 
                              getDateOfInfection() != null ? new SimpleDateFormat("yyyy-MM-dd").format(getDateOfInfection()) : "",
                              getIsOnARTDrugs(), 
                              getStartARTDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(getStartARTDate()) : "",
                              getCountry());
                System.out.println();
                System.out.println("        Profile updated successfully.");
            } else {
                System.out.println("        Script not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void viewProfile() throws ParseException {
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath != null) {
                String response = executeScript(scriptPath, "view-profile", uuid);
                System.out.println();

            if (response.contains("UUID not found") || response.contains("Access denied") || response.contains("Profile not found")) {
                System.out.println("        " + response);
            } else {
                String[] profile = response.split(",");
                System.out.println("        User Information:");
                System.out.println("        =================");
                System.out.println();
                setFirstName(profile[0]);
                System.out.println("        First Name: " + (!profile[0].equals("null") ? profile[0] : "N/A"));
                setLastName(profile[1]);
                System.out.println("        Last Name: " + (!profile[1].equals("null") ? profile[1] : "N/A"));
                if(!profile[2].equals("null")) setDateOfBirth(DATE_FORMAT.parse(profile[2]));
                System.out.println("        Date of Birth: " + (!profile[2].equals("null") ? profile[2] : "N/A"));
                setHivPositive(profile[3]);
                System.out.println("        Is HIV Positive: " + (!profile[3].equals("null") ? profile[3] : "N/A"));
                if(!profile[4].equals("null")) setDateOfInfection(DATE_FORMAT.parse(profile[4]));
                System.out.println("        Date of Infection: " + (!profile[4].equals("null") ? profile[4] : "N/A"));
                setOnARTDrugs(profile[5]);
                System.out.println("        On ART Drugs: " + (!profile[5].equals("null") ? profile[5] : "N/A"));
                if(!profile[6].equals("null")) setStartARTDate(DATE_FORMAT.parse(profile[6]));
                System.out.println("        Date of Start ART: " + (!profile[6].equals("null") ? profile[6] : "N/A"));
                setCountry(profile[7]);
                System.out.println("        Country: " + (!profile[7].equals("null") ? profile[7] : "N/A"));
                System.out.println("        Life Expectancy: " + (!profile[8].equals("null") ? profile[8] : "N/A"));
                System.out.println("        Demise Date: " + (!profile[9].equals("null") ? profile[9] : "N/A"));
            }
            } else {
                System.out.println("        Script not found.");
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
                String response = executeScript(scriptPath, "complete-registration", getUuid(), getFirstName(), getLastName(), new SimpleDateFormat("yyyy-MM-dd").format(getDateOfBirth()), getIsHivPositive(), getDateOfInfection() != null ? new SimpleDateFormat("yyyy-MM-dd").format(getDateOfInfection()) : "", getIsOnARTDrugs(), getStartARTDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(getStartARTDate()) : "", getCountry(), getPassword());
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
