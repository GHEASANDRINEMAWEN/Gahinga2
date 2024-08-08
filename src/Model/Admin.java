package Model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Admin extends User {
    private static final Scanner scanner = new Scanner(System.in);

    public Admin() {
        super();
        setAccessType();
    }

    public Admin(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
        setAccessType();
    }

    // Admin-specific methods
    public void deleteUsers() {
        // Implementation
    }

    public void exportUserData() {
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath != null) {
                String response = executeScript(scriptPath, "get-all-users");
                String csvFile = getFilePath("user_data.csv");
                BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
                writer.write("UUID,Email,FirstName,LastName,DateOfBirth,IsHivPositive,DateOfInfection,OnARTDrugs,StartARTDate,Country,LifeExpectancy");
                writer.newLine();

                String[] users = response.split("\n");
                for (String user : users) {
                    writer.write(user);
                    writer.newLine();
                }
                writer.close();
                System.out.println("User data exported successfully to " + csvFile);
            } else {
                System.out.println("Script not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String userToCsv(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("").append(",");
        sb.append(user.getEmail()).append(",");
        sb.append(user.getFirstName()).append(",");
        sb.append(user.getLastName()).append(",");
        sb.append("").append(",");
        sb.append("").append(",");
        sb.append("").append(",");
        sb.append("").append(",");
        sb.append("").append(",");
        sb.append("").append(",");
        sb.append("");
        return sb.toString();
    }

    public void aggregateUserData() {
        String csvFile = getFilePath("user_data_aggregated.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("UUID,Email,FirstName,LastName,DateOfBirth,IsHivPositive,DateOfInfection,OnARTDrugs,StartARTDate,Country,LifeExpectancy");
            writer.newLine();

            // for (User user : users) {
            //     writer.write(userToCsv(user));
            //     writer.newLine();
            // }

            System.out.println("User data aggregated successfully to " + csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAccessType() {
        this.accessType = UserRole.ADMIN;
    }

    public void initiateRegistration(String accessType) {
        // Implementation
        System.out.println("Initiate Registration");
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath != null) {
                String response = executeScript(scriptPath, "initiate-registration", accessType, email);
                System.out.print(response);
            } else {
                System.out.println("Script not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    public void getAllUsers() {
        // Implementation
    }

    @Override
    public String toString() {
        return "Admin{" +
                "firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", accessType=" + getAccessType() +
                '}';
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

    private String getFilePath(String fileName) {
        return new File(fileName).getAbsolutePath();
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
