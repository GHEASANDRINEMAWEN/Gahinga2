package Model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Admin extends User {
    private static final Scanner scanner = new Scanner(System.in);
    private List<String[]> userDataList;
    private List<String[]> survivalMetricsList;
    private int recordsPerPage = 20;

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

    public void calculateSurvivalMetrics() {
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath != null) {
                String response = executeScript(scriptPath, "calculate-survival-metrics");
                String[] metrics = response.split("\n");
                survivalMetricsList = new ArrayList<>();
    
                // Parse the survival metrics and store them in a list
                for (String metric : metrics) {
                    String[] metricData = metric.split(",");
                    survivalMetricsList.add(metricData);
                }
    
                clearScreen();
                showMetricsPage(1); // Start by showing the first page
            } else {
                System.out.println("Script not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showMetricsPage(int currentPage) {
        int totalRecords = survivalMetricsList.size();
        int start = (currentPage - 1) * recordsPerPage;
        int end = Math.min(start + recordsPerPage, totalRecords);
        int pageCount = (int) Math.ceil((double) totalRecords / recordsPerPage);
    
        // Display survival metrics in a formatted table
        System.out.println("Survival Metrics (Page " + currentPage + "/" + pageCount + ") " + totalRecords + " records\n");
        System.out.println("------------------------------------------------------------------------------------------------------");
    
        for (int i = start; i < end; i++) {
            String[] metricData = survivalMetricsList.get(i);
            
            // Ensure we don't try to access an index that doesn't exist
            String metricName = metricData[0];
            String value = metricData.length > 1 ? metricData[1] : "";
    
            System.out.printf("%-35s %s%n", metricName, value);
        }
    
        System.out.println("------------------------------------------------------------------------------------------------------");
        showMetricsOptions(currentPage);
    }    
    
    private void showMetricsOptions(int currentPage) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Options: (n) Next page | (p) Previous page | (d) Download | (q) Quit");
        String option = scanner.nextLine().trim().toLowerCase();
    
        switch (option) {   
            case "n":
                if ((currentPage * recordsPerPage) < survivalMetricsList.size()) {
                    clearScreen();
                    showMetricsPage(currentPage + 1);
                } else {
                    System.out.println("You are on the last page.");
                    showOptions(currentPage);
                }
                break;

            case "p":
                if (currentPage > 1) {
                    clearScreen();
                    showMetricsPage(currentPage - 1);
                } else {
                    System.out.println("You are on the first page.");
                    showOptions(currentPage);
                }
                break;

            case "d":
                clearScreen();
                downloadSurvivalMetrics();
                break;
    
            case "q":
                clearScreen();
                System.out.println("Exiting...");
                break;
    
            default:
                System.out.println("Invalid option. Please try again.");
                showMetricsOptions(currentPage);
                break;
        }
    }
    
    private void downloadSurvivalMetrics() {
        try {
            String csvFile = getFilePath("survival_metrics.csv");
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            writer.write("Metric Name,Value,Average,Median,Percentile");
            writer.newLine();
    
            for (String[] metricData : survivalMetricsList) {
                writer.write(String.join(",", metricData));
                writer.newLine();
            }
            writer.close();
            System.out.println("Survival metrics exported successfully to " + csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    

    public void exportUserData() {
        try {
            String scriptPath = findScript("user-manager.sh");
            if (scriptPath != null) {
                String response = executeScript(scriptPath, "get-all-users");
                String[] users = response.split("\n");
                userDataList = new ArrayList<>();

                // Parse the user data and store it in a list
                for (String user : users) {
                    String[] userData = user.split(",");
                    userDataList.add(userData);
                }

                clearScreen();
                showPage(1); // Start by showing the first page
            } else {
                System.out.println("Script not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performMetricsSearch(int currentPage) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter search term: ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();
        List<String[]> searchResults = new ArrayList<>();
        for (String[] metricData : survivalMetricsList) {
            for (String field : metricData) {
                if (field.toLowerCase().contains(searchTerm)) {
                    searchResults.add(metricData);
                    break;
                }
            }
        }
        if (searchResults.isEmpty()) {
            System.out.println("No results found for '" + searchTerm + "'");
        } else {
            System.out.println("Search Results:");
            System.out.println("------------------------------------------------------------------------------------------------------");
            System.out.printf("%-25s %-15s %-15s %-15s %-15s%n",
                    "Metric Name", "Value", "Average", "Median", "Percentile");
            System.out.println("------------------------------------------------------------------------------------------------------");
            for (String[] metricData : searchResults) {
                System.out.printf("%-25s %-15s %-15s %-15s %-15s%n",
                    formatCell(25, metricData[0]), formatCell(15, metricData[1]), formatCell(15, metricData[2]),
                    formatCell(15, metricData[3]), formatCell(15, metricData[4]));
            }
            System.out.println("------------------------------------------------------------------------------------------------------");
        }
        showMetricsOptions(currentPage);
    }

    private void showPage(int currentPage) {
        int totalRecords = userDataList.size();
        int start = (currentPage - 1) * recordsPerPage;
        int end = Math.min(start + recordsPerPage, totalRecords);
        int pageCount = (int) Math.ceil((double) totalRecords / recordsPerPage);
    
        // Display users in a formatted table
        System.out.println("User Data (Page " + currentPage + "/" + pageCount + ") " + totalRecords + " records\n");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-25s %-15s %-15s %-15s %-15s %-18s %-12s %-15s %-15s %-15s %-15s %-15s%n",
                "Email", "FirstName", "LastName", "DateOfBirth", "IsHivPositive",
                "DateOfInfection", "OnARTDrugs", "StartARTDate", "Country", "LifeExpectancy", "DemiseDate", "LastUpdatedDate");
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
    
        for (int i = start; i < end; i++) {
            String[] userData = userDataList.get(i);
            System.out.printf("%-25s %-15s %-15s %-15s %-15s %-18s %-12s %-15s %-15s %-15s %-15s %-15s%n",
                    formatCell(25, userData[0]), formatCell(15, userData[1]), formatCell(15, userData[2]), formatCell(15, userData[3]), formatCell(15, userData[4]),
                    formatCell(18, userData[5]), formatCell(12, userData[6]), formatCell(15, userData[7]), formatCell(15, userData[8]), formatCell(15, userData[9]),
                    formatCell(15, userData[10]), formatCell(15, userData[11]));
        }
    
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        showOptions(currentPage);
    }
 

    private void showOptions(int currentPage) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Options: (n) Next page | (p) Previous page | (s) Search | (d) Download | (o) Sort | (q) Quit");
        String option = scanner.nextLine().trim().toLowerCase();

        switch (option) {
            case "n":
                if ((currentPage * recordsPerPage) < userDataList.size()) {
                    clearScreen();
                    showPage(currentPage + 1);
                } else {
                    System.out.println("You are on the last page.");
                    showOptions(currentPage);
                }
                break;

            case "o":
                clearScreen();
                performSort();
                break;

            case "p":
                if (currentPage > 1) {
                    clearScreen();
                    showPage(currentPage - 1);
                } else {
                    System.out.println("You are on the first page.");
                    showOptions(currentPage);
                }
                break;

            case "s":
                clearScreen();
                performSearch(currentPage);
                break;

            case "d":
                clearScreen();
                downloadUserData(currentPage);
                break;

            case "q":
                clearScreen();
                System.out.println("Exiting...");
                break;

            default:
                System.out.println("Invalid option. Please try again.");
                showOptions(currentPage);
                break;
        }
    }

    private String formatCell(int width, String value) {
        // Truncate or pad the value to fit the column
        String formattedValue = value.length() > width ? value.substring(0, width - 3) + "..." : value;
        
        // Create a formatted string with fixed-width columns
        return formattedValue;
    }

    private void performSearch(int currentPage) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter search term: ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();
        List<String[]> searchResults = new ArrayList<>();
        for (String[] userData : userDataList) {
            for (String field : userData) {
                if (field.toLowerCase().contains(searchTerm)) {
                    searchResults.add(userData);
                    break;
                }
            }
        }
        if (searchResults.isEmpty()) {
            System.out.println("No results found for '" + searchTerm + "'");
        } else {
            System.out.println("Search Results:");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-25s %-15s %-15s %-15s %-15s %-18s %-12s %-15s %-15s %-15s %-15s %-15s%n",
                    "Email", "FirstName", "LastName", "DateOfBirth", "IsHivPositive", 
                    "DateOfInfection", "OnARTDrugs", "StartARTDate", "Country", "LifeExpectancy", "DemiseDate", "LastUpdatedDate");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            for (String[] userData : searchResults) {
                System.out.printf("%-25s %-15s %-15s %-15s %-15s %-18s %-12s %-15s %-15s %-15s %-15s %-15s%n",
                    formatCell(25, userData[0]), formatCell(15, userData[1]), formatCell(15, userData[2]), formatCell(15, userData[3]), formatCell(15, userData[4]), 
                    formatCell(18, userData[5]), formatCell(12, userData[6]), formatCell(15, userData[7]), formatCell(15, userData[8]), formatCell(15, userData[9]), formatCell(15, userData[10]), formatCell(15, userData[11]));
            }
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        }
        showOptions(currentPage);
    }

    private void performSort() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select a column to sort by:");
        System.out.println("1. Email");
        System.out.println("2. FirstName");
        System.out.println("3. LastName");
        System.out.println("4. DateOfBirth");
        System.out.println("5. IsHivPositive");
        System.out.println("6. DateOfInfection");
        System.out.println("7. OnARTDrugs");
        System.out.println("8. StartARTDate");
        System.out.println("9. Country");
        System.out.println("10. LifeExpectancy");
        System.out.println("11. DemiseDate");
        System.out.println("12. LastUpdatedDate");
        int column = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.println("Select sort order: (a) Ascending | (d) Descending");
        String order = scanner.nextLine().trim().toLowerCase();

        // Perform sorting
        Comparator<String[]> comparator = getComparator(column);
        if (order.equals("d")) {
            comparator = comparator.reversed();
        }
        userDataList.sort(comparator);

        // Show sorted data
        clearScreen();
        showPage(1); // Show the first page of sorted data
    }

    private Comparator<String[]> getComparator(int column) {
        return (a, b) -> {
            String valueA = a[column - 1];
            String valueB = b[column - 1];
            return valueA.compareToIgnoreCase(valueB);
        };
    }

    private void downloadUserData(int currentPage) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Download options: (a) All data | (p) Current page");
        String option = scanner.nextLine().trim().toLowerCase();
        
        List<String[]> dataToExport;
        if (option.equals("a")) {
            dataToExport = userDataList;
        } else if (option.equals("p")) {
            int start = (currentPage - 1) * recordsPerPage;
            int end = Math.min(start + recordsPerPage, userDataList.size());
            dataToExport = userDataList.subList(start, end);
        } else {
            System.out.println("Invalid option.");
            return;
        }
    
        try {
            String csvFile = getFilePath("user_data.csv");
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            writer.write("Email,FirstName,LastName,DateOfBirth,IsHivPositive,DateOfInfection,OnARTDrugs,StartARTDate,Country,LifeExpectancy,DemiseDate,LastUpdatedDate");
            writer.newLine();
    
            for (String[] userData : dataToExport) {
                writer.write(String.join(",", userData));
                writer.newLine();
            }
            writer.close();
            System.out.println("User data exported successfully to " + csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private double calculateMean(List<Double> data) {
        return data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double calculateMedian(List<Double> data) {
        int size = data.size();
        if (size % 2 == 0) {
            return (data.get(size / 2 - 1) + data.get(size / 2)) / 2.0;
        } else {
            return data.get(size / 2);
        }
    }

    private double calculatePercentile(List<Double> data, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * data.size()) - 1;
        return data.get(index);
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

    private void clearScreen() {
        // This works in some environments; for others, you might need an alternative
        System.out.print("\033[H\033[2J");
        System.out.flush();
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
