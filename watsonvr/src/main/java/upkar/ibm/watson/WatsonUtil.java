package upkar.ibm.watson;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Classifiers;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ListClassifiersOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class WatsonUtil {

    private WatsonUtil(){

    }

    static ClassifiedImages classify(String filePath, double threshold, Boolean checkCustom) throws FileNotFoundException {

        // create a file object to get the filename. Overkill ??
        File file = new File(filePath);
        InputStream imagesStream = new FileInputStream(filePath);

        VisualRecognition vrService = WatsonVRSingleton.getWatsonVR();

        List<String> owners = new ArrayList<>();
        if(checkCustom){
            owners.add("me");
            owners.add("IBM");
        } else {
            owners.add("IBM");
        }

        ClassifyOptions classifyOptions = new ClassifyOptions.Builder()
                .imagesFile(imagesStream)
                .imagesFilename(file.getName())
                .threshold((float)threshold)
                .owners(owners)
                .build();
        ClassifiedImages result = vrService.classify(classifyOptions).execute();
        return result;
    }

    static Classifiers getClassifiers() {
        VisualRecognition vrService = WatsonVRSingleton.getWatsonVR();
        ListClassifiersOptions listClassifiersOptions = new ListClassifiersOptions.Builder()
                .verbose(true)
                .build();
        Classifiers classifiers = vrService.listClassifiers(listClassifiersOptions).execute();
        return classifiers;
    }
}
