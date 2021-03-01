package com.example.seizureapp.ui.testing;

import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.seizureapp.R;
import com.example.seizureapp.pantompkins.OSEAFactory;
import com.example.seizureapp.pantompkins.detection.QRSDetector2;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class TestingFragment extends Fragment {

    private TestingViewModel testingViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        testingViewModel =
                new ViewModelProvider(this).get(TestingViewModel.class);
        View root = inflater.inflate(R.layout.fragment_testing, container, false);
        final TextView textView = root.findViewById(R.id.text_testing);
        final Button button = root.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> rDetections = new ArrayList<>(); // Indices of the detected R-peaks
                List<Float> rIntervals = new ArrayList<>(); // Seconds between each R-peak

//                Read CSV file
                InputStream is = getResources().openRawResource(R.raw.testcsv);
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
                    QRSDetector2 qrsDetector = OSEAFactory.createQRSDetector2(512);
                    for (int i = 0; i < ecgSamples.length; i++) {
                        int result = qrsDetector.QRSDet(ecgSamples[i]);
                        if (result != 0) {
                            rDetections.add(i - result);
                        }
                    }

//                    R-peak indices to R-R intervlas in seconds
                    for (int i = 0; i < rDetections.size() - 1; i++) {
                        rIntervals.add((float) ((rDetections.get(i + 1) - rDetections.get(i))) / 512);
                        System.out.println("R-R interval: " + rIntervals.get(i));
                    }
                    System.out.println("Total number of R-R intervals: " + rIntervals.size());


//                    Save R-R intervals to file
                    String filename = "output_Rpeaks_" + getResources().getResourceEntryName(R.raw.testcsv) + ".csv";
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

        testingViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}