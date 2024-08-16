package View;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserPopulator {
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Alex", "Emily", "Michael", "Sarah", "David", "Emma", "Daniel", "Olivia",
        "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla",
        "kanyombya", "Ruburi"
    };

    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla",
        "Jennifer", "Lopez"
    };

    private static final String[] COUNTRY_ISOS = {
        "USA", "CAN", "RWA", "FRA", "DEU", "BRA", "JPN", "CHN", "IND", "AUS", "UGA", "KEN",
        "AFG", "ALB", "DZA", "ASM", "AND", "AGO", "AIA", "ATG", "ARG", "ARM", "ABW", "AUS", "AUT", "AZE", "BHS", "BHR", "BGD", "BRB", "BLR", "BEL", "BLZ", "BEN", "BMU", "BTN", "BOL", "BES", "BIH", "BWA", "BRA", "VGB", "BRN", "BGR", "BFA", "BDI", "KHM", "CMR", "CAN", "CPV", "CYM", "CAF", "TCD", "CHL", "CHN", "COL", "COM", "COG", "COK", "CRI", "CIV", "HRV", "CUB", "CUW", "CYP", "CZE", "COD", "DNK", "DJI", "DMA", "DOM", "TLS", "ECU", "EGY", "SLV", "GNQ", "ERI", "EST", "SWZ", "ETH", "FLK", "FRO", "FJI", "FIN", "FRA", "GUF", "PYF", "GAB", "GMB", "GEO", "DEU", "GHA", "GIB", "GRC", "GRL", "GRD", "GLP", "GUM", "GTM", "GGY", "GIN", "GNB", "GUY", "HTI", "HND", "HKG", "HUN", "ISL", "IND", "IDN", "IRN", "IRQ", "IRL", "IMN", "ISR", "ITA", "JAM", "JPN", "JEY", "JOR", "KAZ", "KEN", "KIR", "KWT", "KGZ", "LAO", "LVA", "LBN", "LSO", "LBR", "LBY", "LIE", "LTU", "LUX", "MAC", "MDG", "MWI", "MYS", "MDV", "MLI", "MLT", "MHL", "MTQ", "MRT", "MUS", "MYT", "MEX", "FSM", "MDA", "MCO", "MNG", "MNE", "MSR", "MAR", "MOZ", "MMR", "NAM", "NRU", "NPL", "NLD", "NCL", "NZL", "NIC", "NER", "NGA", "NIU", "PRK", "MKD", "MNP", "NOR", "OMN", "PAK", "PLW", "PSE", "PAN", "PNG", "PRY", "PER", "PHL", "POL", "PRT", "PRI", "QAT", "REU", "ROU", "RUS", "RWA", "BLM", "SHN", "KNA", "LCA", "MAF", "SPM", "VCT", "WSM", "SMR", "STP", "SAU", "SEN", "SRB", "SYC", "SLE", "SGP", "SXM", "SVK", "SVN", "SLB", "SOM", "ZAF", "KOR", "SSD", "ESP", "LKA", "SDN", "SUR", "SWE", "CHE", "SYR", "TWN", "TJK", "TZA", "THA", "TGO", "TKL", "TON", "TTO", "TUN", "TUR", "TKM", "TCA", "TUV", "UGA", "UKR", "ARE", "GBR", "USA", "VIR", "URY", "UZB", "VUT", "VEN", "VNM", "WLF", "ESH", "YEM", "ZMB", "ZWE"
    };
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DEFAULT_PASSWORD = "123";

    public static void main(String[] args) {
        for (int i = 0; i < 500; i++) {
            try {
                String email = generateEmail(i);
                String uuid = initiateRegistration(email);
                
                // Define a set of error messages
                Set<String> errorMessages = new HashSet<>();
                errorMessages.add("Email already registered.");
                errorMessages.add("Invalid email format.");
                errorMessages.add("Access denied.");
                errorMessages.add("UUID not found.");
                errorMessages.add("Script not found.");

                // Check if registrationSuccess is not in the set of error messages
                if (!errorMessages.contains(uuid)) {
                    completeRegistration(uuid);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static String generateEmail(int index) {
        String firstName = getRandom(FIRST_NAMES);
        String lastName = getRandom(LAST_NAMES);
        return firstName.toLowerCase() + "." + lastName.toLowerCase() + index + "@example.com";
    }

    private static String initiateRegistration(String email) throws IOException, InterruptedException {
        String scriptPath = findScript("user-manager.sh");
        if (scriptPath == null) {
            System.out.println("  Script not found.");
            return "Script not found.";
        }

        // Construct the command string using the absolute path
        String command = scriptPath + " initiate-registration 6735862e-8be9-4ac8-ae3a-d85b60e95925 " + email;
        System.out.println("Executing command: " + command); // Log the command being executed

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        Process process = pb.start();
        int exitCode = process.waitFor();

        // Prepare to read the output and error streams
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder output = new StringBuilder();
        StringBuilder errors = new StringBuilder();

        // Read standard output
        String line;
        while ((line = outputReader.readLine()) != null) {
            output.append(line).append(System.lineSeparator());
            System.out.println(line); // Log the standard output
        }

        // Read error output
        while ((line = errorReader.readLine()) != null) {
            errors.append(line).append(System.lineSeparator());
            System.err.println(line); // Log the errors
        }

        // Close readers
        outputReader.close();
        errorReader.close();

        // Return output or handle errors as needed
        if (exitCode == 0) {
            return output.toString().trim(); // Return the standard output if no errors occurred
        } else {
            return errors.toString().trim(); // Return error output if there were errors
        }
    }
    
    private static void completeRegistration(String uuid) throws IOException, InterruptedException {
        String firstName = getRandom(FIRST_NAMES);
        String lastName = getRandom(LAST_NAMES);
        String dateOfBirth = getRandomDateOfBirth();
        boolean hasHIV = RANDOM.nextBoolean();
        String diagnosisDate = hasHIV ? getRandomDateAfter(dateOfBirth) : "null";
        boolean onART = hasHIV && RANDOM.nextBoolean();
        String artStartDate = onART ? getRandomDateAfter(diagnosisDate) : "null";
        String countryISO = getRandom(COUNTRY_ISOS);

        String scriptPath = findScript("user-manager.sh");
        if (scriptPath == null) {
            System.out.println("  Script not found.");
            return;
        }

        String command = String.format(
                "%s complete-registration %s %s %s %s %s %s %s %s %s %s",
                scriptPath, uuid, firstName, lastName, dateOfBirth, hasHIV, diagnosisDate,
                onART, artStartDate, countryISO, DEFAULT_PASSWORD
        );
        System.out.println("Executing command: " + command); // Log the command being executed

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        Process process = pb.start();
        int exitCode = process.waitFor();

        // Prepare to read the output and error streams
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder output = new StringBuilder();
        StringBuilder errors = new StringBuilder();

        // Read standard output
        String line;
        while ((line = outputReader.readLine()) != null) {
            output.append(line).append(System.lineSeparator());
            System.out.println(line); // Log the standard output
        }

        // Read error output
        while ((line = errorReader.readLine()) != null) {
            errors.append(line).append(System.lineSeparator());
            System.err.println(line); // Log the errors
        }

        // Close readers
        outputReader.close();
        errorReader.close();
    }

    private static String getRandom(String[] array) {
        int index = RANDOM.nextInt(array.length);
        return array[index];
    }

    private static String getRandomDateOfBirth() {
        // Generate a random date of birth in yyyy-mm-dd format
        int year = RANDOM.nextInt(50) + 1950;
        int month = RANDOM.nextInt(12) + 1;
        int day = RANDOM.nextInt(28) + 1; // Keep it simple with max 28 days
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private static String getRandomDateAfter(String date) {
        // Generate a date after the given date in yyyy-mm-dd format
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));
        // Just a simple approach to generate a date after the given date
        day += RANDOM.nextInt(30) + 1;
        return String.format("%04d-%02d-%02d", year, month, day);
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
}
