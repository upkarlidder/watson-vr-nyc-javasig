package upkar.ibm.watson;

import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class WatsonVRSingleton {

    private static VisualRecognition single_instance;

    private WatsonVRSingleton() {

    }

    static VisualRecognition getWatsonVR() {

        if (single_instance == null) {
            Properties prop = new Properties();
            InputStream input = null;

            try {

                input = new FileInputStream("src/main/java/upkar/ibm/watson/env.properties");

                // load a properties file
                prop.load(input);

                IamOptions options = new IamOptions.Builder()
                        .apiKey(prop.getProperty("API_KEY"))
                        .build();

                single_instance = new VisualRecognition(prop.getProperty("VERSION"), options);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return single_instance;
    }
}
