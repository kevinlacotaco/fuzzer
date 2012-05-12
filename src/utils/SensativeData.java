package utils;

import java.util.ArrayList;
import java.util.List;

public class SensativeData {

    public static List<String> sensativeData = new ArrayList<String>();

    static {
        sensativeData.add("test123");
    }

    public static List<String> getSensativeData() {
        return sensativeData;
    }
}
