package com.example.seizureapp.ui.connections;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConnectionsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ConnectionsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Nearby Bluetooth devices");
    }

    public LiveData<String> getText() {
        return mText;
    }
}