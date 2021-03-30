package com.example.seizureapp.ui.testing;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.seizureapp.R;
import com.example.seizureapp.pantompkins.OSEAFactory;
import com.example.seizureapp.pantompkins.classification.BeatDetectionAndClassification;
import com.example.seizureapp.pantompkins.detection.QRSDetector2;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class TestingFragment extends Fragment {

    private TestingViewModel testingViewModel;
    private int sampleRate = 512;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        testingViewModel =
                new ViewModelProvider(this).get(TestingViewModel.class);
        View root = inflater.inflate(R.layout.fragment_testing, container, false);
        final TextView textView = root.findViewById(R.id.text_testing);
        final Button button = root.findViewById(R.id.button);
        final Button button2 = root.findViewById(R.id.button2);
        final Button button3 = root.findViewById(R.id.button3);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> rDetections = new ArrayList<>(); // Indices of the detected R-peaks
                List<Float> rIntervals = new ArrayList<>(); // Seconds between each R-peak
                int sampleRate = 256;
                int inputFile = R.raw.patient_28_3_256hz_resample;

//                Read TXT file
                InputStream is = getResources().openRawResource(inputFile);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8)
                );
                String line;
                try {
                    line = reader.readLine();
                    String[] tokens = line.split(",");

                    int[] ecgSamples = new int[tokens.length];

                    for (int i = 0; i < tokens.length; i++) {
                        ecgSamples[i] = Integer.parseInt(tokens[i]);
                    }

//                    R-peak detection
//                    https://github.com/MEDEVIT/OSEA-4-Java
                    QRSDetector2 qrsDetector = OSEAFactory.createQRSDetector2(sampleRate);
//                    QRSDetectorParameters qrsDetectorParameters = new QRSDetectorParameters(512);
//                    QRSFilterer qrsFilterer = new QRSFilterer(qrsDetectorParameters);
//                    qrsFilterer.
//                    qrsDetector.setObjects(qrsFilterer);
                    for (int i = 0; i < ecgSamples.length; i++) {
                        int result = qrsDetector.QRSDet(ecgSamples[i]);
                        if (result != 0) {
                            rDetections.add(i - result);
                        }
                    }

//                    Another R-peak method
//                    BeatDetectionAndClassification detector = OSEAFactory.createBDAC(sampleRate, sampleRate / 2);
//                    for (int i = 0; i < ecgSamples.length; i++) {
//                        BeatDetectionAndClassification.BeatDetectAndClassifyResult result = detector.BeatDetectAndClassify(ecgSamples[i]);
//                        if (result.rrInterval != 0) {
//                            rDetections.add(i - result.samplesSinceRWaveIfSuccess);
//                        }
//                    }

//                    Another R-peak method
//                    https://github.com/MEDEVIT/OSEA-4-Java
//                    QRSDetector qrsDetectorAlt = OSEAFactory.createQRSDetector(512);
//                    for (int i = 0; i < ecgSamples.length; i++) {
//                        int result = qrsDetectorAlt.QRSDet(ecgSamples[i]);
//                        if (result != 0) {
//                            rDetections.add(i - result);
//                        }
//                    }


//                    Convert from R-peak indices to R-R intervlas in seconds
                    for (int i = 0; i < rDetections.size() - 1; i++) {
                        rIntervals.add((float) ((rDetections.get(i + 1) - rDetections.get(i))) / sampleRate);
                        System.out.println("R-R interval: " + rIntervals.get(i));
                    }
                    System.out.println("Total number of R-R intervals: " + rIntervals.size());


//                    Save R-R intervals to file
                    String filename = "optimized_noAbs_mw4samples_preBlank250ms_" + getResources().getResourceEntryName(inputFile) + ".csv";
                    String rIntervalsString = "";
                    for (int i = 0; i < rIntervals.size(); i++) {
                        rIntervalsString += rIntervals.get(i).toString();
                        if (i < rIntervals.size() - 1) {
                            rIntervalsString += ";";
                        }
                    }
                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput(filename, getActivity().MODE_PRIVATE));
                        outputStreamWriter.write(rIntervalsString);
                        outputStreamWriter.close();

                        textView.setText("Success: R-peaks saved to file \n" +
                                "Total number of R-R intervals: " + rIntervals.size() + "\n" +
                                "Filename: " + filename);
                    } catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                        textView.setText("Error: File save failed");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    textView.setText("Error: File read failed");
                }

            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int inputFile = R.raw.patient_1_1;
                List<Float> rIntervals = new ArrayList<>();
                List<Integer> rrLocations = new ArrayList<>();

                // Read CSV file (.txt)
                InputStream is = getResources().openRawResource(inputFile);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8)
                );
                String line;
                try {
                    line = reader.readLine();
                    String[] tokens = line.split(",");

                    int[] ecgSamples = new int[tokens.length];

                    for (int i = 0; i < tokens.length; i++) {
                        ecgSamples[i] = Integer.parseInt(tokens[i]);
                    }

                    // R-peak detection
                    BeatDetectionAndClassification detector = OSEAFactory.createBDAC(sampleRate, sampleRate / 2);
                    for (int i = 0; i < ecgSamples.length; i++) {
                        BeatDetectionAndClassification.BeatDetectAndClassifyResult result = detector.BeatDetectAndClassify(ecgSamples[i]);
                        if (result.rrInterval != 0) {
//                            rIntervals.add((float) result.rrInterval / sampleRate);
                            rrLocations.add(i);
                        }
                    }

//                    Save RR locations
                    String filename = "RR_locations_" + getResources().getResourceEntryName(inputFile) + ".csv";
                    String rrLocationsString = "";
                    for (int i = 0; i < rrLocations.size(); i++) {
                        rrLocationsString += rrLocations.get(i).toString();
                        if (i < rrLocations.size() - 1) {
                            rrLocationsString += ";";
                        }
                    }
                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput(filename, getActivity().MODE_PRIVATE));
                        outputStreamWriter.write(rrLocationsString);
                        outputStreamWriter.close();

                        textView.setText("Success: RR locations saved to file \n" +
                                "Total number of R-R intervals: " + rrLocations.size() + "\n" +
                                "Filename: " + filename);
                    } catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                        textView.setText("Error: File save failed");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    textView.setText("Failed to read file");
                }
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int inputFile = R.raw.optimized_noabs_mw4samples_preblank250ms_patient_13_2_256hz_resample;

                List<Double> rrIntervals = new ArrayList<>(); // Seconds between each R-peak
                List<Double> modCSIs = new ArrayList<>(); // List with the ModCSI filtered values multiplied by slope
                List<Double> CSIs = new ArrayList<>(); // List with the CSI values

                // Read R R interval file
                InputStream is = getResources().openRawResource(inputFile);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8)
                );
                try {
                    String line = reader.readLine();
                    String[] tokens = line.split(";");

                    for (int i = 0; i < tokens.length; i++) {
                        rrIntervals.add(Double.parseDouble(tokens[i]));
                    }

                    int rrCounter = 0; // Counter for position of moving window (only relevant when reading file, otherwise this would be a memory leak)
                    int mws = 100; // Moving window size
                    // 107 R-R interval sliding window
                    for (int i = 0; i < rrIntervals.size() - (mws+7); i++) {

                        // Buffer with 107 R-R intervals
                        double[] rrBuffer = new double[mws+7];
                        for (int j = 0; j < (mws+7); j++) {
                            rrBuffer[j] = rrIntervals.get(j + rrCounter);
                        }

                        // Filter - 7 R-R interval median filter
                        double[] rrFiltered = new double[mws];
                        int filterCounter = 0;
                        for (int j = 0; j < rrBuffer.length-7; j++) {

                            double median;
                            double[] filterBuffer = new double[7];
                            for (int k = 0; k < 7; k++) {
                                filterBuffer[k] = rrBuffer[k+filterCounter];
                            }
                            Arrays.sort(filterBuffer); // Sorting the 7 values

                            if (filterBuffer.length % 2 == 0) // If not devisable by 2
                                median = ((double)filterBuffer[filterBuffer.length/2] + (double)filterBuffer[filterBuffer.length/2 - 1])/2;
                            else
                                median = (double) filterBuffer[filterBuffer.length/2];

                            rrFiltered[j] = median; // rrFiltered is the 100 RR interval moving window

                            filterCounter++;
                        }

                        // Calculate slope of R-R intervals - filtered
                        double[] x_axis = new double[mws]; // The x-axis is the accumulated RR intervals
                        for (int j = 0; j < mws; ++j) {
                            if (j>0){ // The point before + the current RR interval
                                x_axis[j] = x_axis[j-1] + rrFiltered[j];
                            }
                            else { // First point is just the first RR-interval
                                x_axis[j] = rrFiltered[j];
                            }
                        }

                        double[] y_axis = new double[mws]; // The y-axis data is the heart rate in minutes
                        for (int j = 0; j < mws; ++j) {
                            y_axis[j] = 60/rrFiltered[j];
                        }

                        SimpleRegression regression = new SimpleRegression(); // doing the regression
                        for (int j = 0; j < x_axis.length; j++) {
                            regression.addData(x_axis[j], y_axis[j]);
                        }
                        double slope = Math.abs(regression.getSlope());

                        // Calculate SD1 - filtered
                        double[] rrDiff_filt = new double[mws-1];
                        for (int j = 0; j < mws-1; j++) {
                            rrDiff_filt[j] = rrFiltered[j+1] - rrFiltered[j];
                        }

                        for (int j = 0; j < mws-1; j++) {
                            rrDiff_filt[j] = rrDiff_filt[j] * (Math.sqrt(2)/2);
                        }

                        StandardDeviation std = new StandardDeviation();
                        double SD1_filt = 1000*std.evaluate(rrDiff_filt);

                        // Calculate SD2 - filtered
                        double[] rrSum_filt = new double[mws-1];
                        for (int j = 0; j < mws-1; j++) {
                            rrSum_filt[j] = rrFiltered[j] + rrFiltered[j+1];
                        }

                        for (int j = 0; j < mws-1; j++) {
                            rrSum_filt[j] = rrSum_filt[j] * (Math.sqrt(2)/2);
                        }

                        double SD2_filt = 1000*std.evaluate(rrSum_filt);

                        // Calculate SD1 - unfiltered
                        double[] rrDiff_unFilt = new double[mws-1];
                        for (int j = 0; j < mws-1; j++) {
                            rrDiff_unFilt[j] = rrBuffer[j+7] - rrBuffer[j+8];
                        }

                        for (int j = 0; j < mws-1; j++) {
                            rrDiff_unFilt[j] = rrDiff_unFilt[j] * (Math.sqrt(2)/2);
                        }

                        double SD1_unFilt = 1000*std.evaluate(rrDiff_unFilt);

                        // Calculate SD2 - unfiltered
                        double[] rrSum_unFilt = new double[mws-1];
                        for (int j = 0; j < mws-1; j++) {
                            rrSum_unFilt[j] = rrBuffer[j+7] + rrBuffer[j+8];
                        }

                        for (int j = 0; j < mws-1; j++) {
                            rrSum_unFilt[j] = rrSum_unFilt[j] * (Math.sqrt(2)/2);
                        }

                        double SD2_unFilt = 1000*std.evaluate(rrSum_unFilt);

                        // Calculate T based on filtered
                        double T_filt = 4 * SD1_filt;

                        // Calculate L based on filtered data
                        double L_filt = 4 * SD2_filt;

                        // Calculate T based on unfiltered
                        double T_unFilt = 4 * SD1_unFilt;

                        // Calculate L based on unfiltered data
                        double L_unFilt = 4 * SD2_unFilt;

                        // Calculate ModCSI100 filtered and multiplied by slope
                        double modCSI = (L_filt * L_filt) / T_filt;
                        double modCSI_slope = modCSI * slope;
                        modCSIs.add(modCSI_slope);

                        // Calculate CSI100, no filtering
                        double CSI = L_unFilt/T_unFilt;
                        double CSI_slope = CSI * slope;
                        CSIs.add(CSI_slope);

                        rrCounter++;
                    }

                    // Save highest MODCSI100_filt_slope value for evaluation against LabVIEW version
                    double maxModCSI_filt_slope = Collections.max(modCSIs);
                    double maxCSI = Collections.max(CSIs);

                    textView.setText("Max ModCSI100_filt_slope: " + maxModCSI_filt_slope + "\nMax CSI100: " + maxCSI);

                } catch (IOException e) {
                    e.printStackTrace();
                    textView.setText("Error: File read failed");
                }
            }
        });

        testingViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    public static double calculateSD(double numArray[])
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;

        for(double num : numArray) {
            sum += num;
        }

        double mean = sum/length;

        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/length);
    }
}