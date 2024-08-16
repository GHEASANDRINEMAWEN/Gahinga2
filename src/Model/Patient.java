package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Patient extends User {
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private Date dateOfBirth;
    private String isHivPositive;
    private Date dateOfInfection;
    private String onARTDrugs;
    private Date startARTDate;
    private String country;
    private Date demiseDate;
    private Double lifeExpectancy;
    private String uuid;
    private Date lastUpdatedDate;

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    // Default constructor
    public Patient() {
        super();
        setAccessType();
    }

    // Parameterized constructor
    public Patient(String uuid, String firstName, String lastName, String email, String password, Date dateOfBirth, String isHivPositive, Date dateOfInfection, String onARTDrugs, Date startARTDate, String country, Date demiseDate, Double lifeExpectancy) {
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
        this.lifeExpectancy = lifeExpectancy;
    }

    public Patient(String uuid2, String firstName, String lastName, String string, String string2, Object object,
            Object object2, Object object3, Object object4, Object object5, String country2, Object object6,
            Object object7) {
        //TODO Auto-generated constructor stub
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
    public Double getLifeExpectancy() { return lifeExpectancy; }
    public void setLifeExpectancy(Double lifeExpectancy) { this.lifeExpectancy = lifeExpectancy; }

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
                System.out.println("  Profile updated successfully.");
            } else {
                System.out.println("  Script not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public String getFilePath(String fileName) {
        // Define the base directory where files will be stored
        String baseDir = "/path/to/storage/directory"; // Replace with your actual directory path

        // Combine the base directory with the filename
        String fullPath = Paths.get(baseDir, fileName).toString();

        return fullPath;
    }
  

    public void generateICalendar() {
        try {
            String scriptPath = findScript("user-manager.sh"); // Name your script accordingly
            if (scriptPath != null) {
                // Execute the script. Adjust parameters if necessary.
                String output = executeScript(scriptPath, "generate-icalendar",  getUuid());
                System.out.println(output);
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
            if (scriptPath == null) {
                System.out.println("  Script not found.");
                return;
            }
    
            String response = executeScript(scriptPath, "view-profile", uuid);
            System.out.println();
    
            if (response.contains("UUID not found") || response.contains("Access denied") || response.contains("Profile not found")) {
                System.out.println("  " + response);
                return;
            }
    
            String[] profile = response.split(",");
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║               User Information               ║");
            System.out.println("╠══════════════════════╗╔══════════════════════╣");
            System.out.println("║                      ║║                      ║");
    
            setFirstName(profile[0]);
            System.out.println(formatRow("First Name", getDisplayValue(profile[0])));
            System.out.println("║                      ║║                      ║");

            setLastName(profile[1]);
            System.out.println(formatRow("Last Name", getDisplayValue(profile[1])));
            System.out.println("║                      ║║                      ║");
    
            if (!profile[2].equals("null") && !profile[2].equals("")) setDateOfBirth(DATE_FORMAT.parse(profile[2]));
            System.out.println(formatRow("Date of Birth", getDisplayValue(profile[2])));
            System.out.println("║                      ║║                      ║");
    
            setHivPositive(profile[3]);
            System.out.println(formatRow("Is HIV Positive", getDisplayValue(profile[3])));
            System.out.println("║                      ║║                      ║");
    
            if (!profile[4].equals("null") && !profile[4].equals("")) setDateOfInfection(DATE_FORMAT.parse(profile[4]));
            System.out.println(formatRow("Date of Infection", getDisplayValue(profile[4])));
            System.out.println("║                      ║║                      ║");
    
            setOnARTDrugs(profile[5]);
            System.out.println(formatRow("On ART Drugs", getDisplayValue(profile[5])));
            System.out.println("║                      ║║                      ║");
    
            if (!profile[6].equals("null") && !profile[6].equals("")) setStartARTDate(DATE_FORMAT.parse(profile[6]));
            System.out.println(formatRow("Date of Start ART", getDisplayValue(profile[6])));
            System.out.println("║                      ║║                      ║");
    
            setCountry(profile[7]);
            System.out.println(formatRow("Country", getDisplayValue(profile[7])));
            System.out.println("║                      ║║                      ║");
    
            System.out.println(formatRow("Life Expectancy", getDisplayValue(profile[8])));
            System.out.println("║                      ║║                      ║");
            System.out.println(formatRow("Demise Date", getDisplayValue(profile[9])));
            System.out.println("║                      ║║                      ║");
            System.out.println("╚══════════════════════╝╚══════════════════════╝");
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to format a table row with fixed width columns.
     * Adjusts the second column to fit the content within the table width.
     */
    private String formatRow(String label, String value) {
        final int labelWidth = 20;
        final int valueWidth = 20;
        
        // Truncate the label if it's too long
        String formattedLabel = label.length() > labelWidth ? label.substring(0, labelWidth - 3) + "..." : label;
        
        // Truncate or pad the value to fit the column
        String formattedValue = value.length() > valueWidth ? value.substring(0, valueWidth - 3) + "..." : value;
        
        // Create a formatted string with fixed-width columns
        return String.format("║ %-20s ║║ %-20s ║", formattedLabel, formattedValue);
    }
    
    /**
     * Helper method to get displayable value or "N/A" if the value is null.
     */
    private String getDisplayValue(String value) {
        return (value != null && !value.equals("null") && !value.equals("")) ? value : "N/A";
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

    public void generateICalendar() {
        try {
            String scriptPath = findScript("user-manager.sh"); // Name your script accordingly
            if (scriptPath != null) {
                // Execute the script. Adjust parameters if necessary.
                String output = executeScript(scriptPath, "generate-icalendar",  getUuid());
                System.out.println(output);
            } else {
                System.out.println("        Script not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
