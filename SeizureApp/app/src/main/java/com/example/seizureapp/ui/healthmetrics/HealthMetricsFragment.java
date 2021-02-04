package com.example.seizureapp.ui.healthmetrics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.seizureapp.R;

public class HealthMetricsFragment extends Fragment {

    private HealthMetricsViewModel healthMetricsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        healthMetricsViewModel =
                new ViewModelProvider(this).get(HealthMetricsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_healthmetrics, container, false);
        final TextView textView = root.findViewById(R.id.text_healthMetrics);
        healthMetricsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}