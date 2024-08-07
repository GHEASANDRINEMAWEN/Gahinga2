package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class User {
    private String firstName;
    private String lastName;
    private String email;
    private String password; // Hashed password
    protected UserRole accessType; // Protected to allow subclasses to set it directly

    public User() {}

    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserRole getAccessType() { return accessType; }
    public abstract void setAccessType();

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", accessType=" + accessType +
                '}';
    }

    public String login() {
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath != null) {
                return executeScript(scriptPath, "login", email, password);
            } else {
                return "Script not found.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error during login.";
        }
    }

    // Method to find the script path dynamically
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
            
            // Capture standard output from the script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            
            // Capture error output from the script
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                response.append("ERROR: ").append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                response.append("Script exited with error code: ").append(exitCode).append("\n");
            }
    
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            response.append("An error occurred: ").append(e.getMessage()).append("\n");
        }
        
        return response.toString().trim(); // Trim to remove any trailing new lines
    }


}
