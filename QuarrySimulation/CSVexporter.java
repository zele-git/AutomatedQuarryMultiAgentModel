package quarrysim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVexporter {
    public static void writeActionTimeSeqToCsv(List<String> actionTime, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.append("Truck Name,Load RqstTime, Load Station, Load StartTime, Load CompletedTime, Unload RqstTime, Unload Station, Unload StartTime, Unload CompletedTime\n");
            for (String line : actionTime) {
                writer.write(line);
                writer.newLine();
            }
              // Ensures each string is on a new line
            System.out.println("CSV written to " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }

    }
}
