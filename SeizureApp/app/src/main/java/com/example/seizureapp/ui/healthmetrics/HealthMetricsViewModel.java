package com.example.seizureapp.ui.healthmetrics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HealthMetricsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HealthMetricsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}