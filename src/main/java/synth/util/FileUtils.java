package synth.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    /**
     * Read all lines from a file.
     *
     * @param filepath path to the file
     * @return a list of string where each string is a line in the file
     */
    public static List<String> readLinesFromFile(String filepath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("File reading error");
        }
    }
}
