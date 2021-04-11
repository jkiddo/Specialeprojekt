package com.au.assure;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Recorder {

    Intent batteryStatus;
    String filenameBat;
    String m_strRoot;

    public Recorder(Context context) {
        Reset();

        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("dd.MM.yy_HH.mm");
        date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
        String currentTime = date.format(currentLocalTime);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = context.registerReceiver(null, ifilter);
        filenameBat = "Batterylog_" + currentTime + ".txt";
    }

    private void Reset() {
        MakeSaveDirectory();
    }

    private void MakeSaveDirectory(){
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        m_strRoot = root + "/ASSURE";
        File myDir = new File(m_strRoot);

        if (!myDir.exists())
            myDir.mkdirs();

    }

    private String GetSavePathBatteryLog() {
        if (m_strRoot == null)
            return ("/sdcard/" + filenameBat);

        return m_strRoot + "/" + filenameBat;
    }

    public void saveBatteryInfo(float c3BatPct) {
        // Determine phone battery percentage
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float phoneBatPct = level * 100 / (float)scale;

        String line = "\n\nPhone battery percent: " + phoneBatPct + "\nC3 battery percent: " + c3BatPct
                + "\n" + Calendar.getInstance().getTime().toString();

        FileOutputStream fos = null;
        File file = new File(GetSavePathBatteryLog());

        try {
            file.createNewFile();


            fos = new FileOutputStream(file, true);

            if (fos != null) {
                fos.write(line.getBytes());

                file.getTotalSpace();

                fos.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void saveModCSIandCSI() {

    }

    public void saveRawECG() {

    }

    public void saveRRintervals() {

    }
}
