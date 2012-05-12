package utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SensitiveData {

    public static List<String> sensitiveData = new ArrayList<String>();

    static {
        try {
            BufferedReader file = new BufferedReader(new FileReader(
                    "config/sensitiveData"));

            String line;
            while ((line = file.readLine()) != null) {
                sensitiveData.add(line);
            }
        } catch (FileNotFoundException e) {
            System.err
                    .println("File was not found: Not checking sensative data");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static List<String> getSensitiveData() {
        return sensitiveData;
    }
}
