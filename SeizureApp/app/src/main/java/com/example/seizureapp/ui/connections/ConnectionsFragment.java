package com.example.seizureapp.ui.connections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.seizureapp.R;

import java.util.ArrayList;

public class ConnectionsFragment extends Fragment {

    private ConnectionsViewModel connectionsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        connectionsViewModel =
                new ViewModelProvider(this).get(ConnectionsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_connections, container, false);

//        final ListView listView = root.findViewById(R.id.devicesList);
//        String[] names = {"Cortrium C3+", "Oliver"};
//        int[] images = {R.drawable.connected, R.drawable.disconnected};
//        ViewAdapter adapter = new ViewAdapter(this.getContext(), names, images);
//        listView.setAdapter(adapter);

        ArrayList<String> devices = new ArrayList<>();
        devices.add("Cortrium C3+");
        devices.add("Oliver");
        devices.add("Cortrium C3+");
        devices.add("Oliver");
        devices.add("Cortrium C3+");
        devices.add("Oliver");
        devices.add("Cortrium C3+");
        devices.add("Oliver");
        devices.add("Cortrium C3+");
        devices.add("Oliver");
        devices.add("Cortrium C3+");
        devices.add("Oliver");
        devices.add("Cortrium C3+");
        devices.add("Oliver");
        devices.add("Cortrium C3+");
        devices.add("Oliver");
        devices.add("Cortrium C3+");
        devices.add("Oliver");
        devices.add("Cortrium C3+");
        devices.add("Oliver");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(),
                android.R.layout.simple_list_item_1,
                devices
        );

        ListView listView = root.findViewById(R.id.devicesList);
        listView.setAdapter(adapter);

        final TextView textView = root.findViewById(R.id.text_connections);
        connectionsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}