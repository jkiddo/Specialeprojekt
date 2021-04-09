package com.example.seizureapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.example.seizureapp.ui.home.HomeViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

//    private SensorManager m_SensorManager;
//    private Sensor m_SensorAccelerometer;
//
//    public ArrayList<CortriumC3> m_al_C3Devices;
//    public ArrayList<String> m_al_C3Names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_healthMetrics,
                R.id.navigation_connections, R.id.navigation_testing)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

//        navView.getMenu().findItem(R.id.navigation_testing).setVisible(false);

//        m_SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        m_SensorAccelerometer = m_SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        m_SensorManager.registerListener((SensorEventListener) MainActivity.this,m_SensorAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
//
//        getSupportActionBar().hide();
//
//        m_al_C3Devices = new ArrayList<>();
//        m_al_C3Names = new ArrayList<>();
    }
}