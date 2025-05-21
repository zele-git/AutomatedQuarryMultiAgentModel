package quarrysim;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class ScenarioFileLoader {
    public static ScenarioFile load(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), ScenarioFile.class);
    }
}
