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
    String rootBat;
    String filenameCSI;
    String rootCSI;
    String filenameModCSI;
    String rootModCSI;
    String filenameECG;
    String rootECG;
    String filenameRR;
    String rootRR;
    String filenameSeizure;
    String filenameSeizureMark;
    String rootSeizure;
    String filenameThresh;
    String rootThresh;

    String m_strRoot;

    public Recorder() {
        Reset();

        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("dd.MM.yy_HH.mm");
        String currentTime = date.format(currentLocalTime);

        filenameBat = "Battery_log_" + currentTime + ".txt";
        filenameCSI = "CSI_log_" + currentTime + ".txt";
        filenameModCSI = "ModCSI_log_" + currentTime + ".txt";
        filenameECG = "ECG_log_" + currentTime + ".txt";
        filenameRR = "RR_log_" + currentTime + ".txt";
        filenameSeizure = "Seizures_log_" + currentTime + ".txt";
        filenameSeizureMark = "Seizure_marks_" + currentTime + ".txt";
        filenameThresh = "Thresholdchanges_log_" + currentTime + ".txt";
    }

    private void Reset() {
        MakeSaveDirectories();
    }

    private void MakeSaveDirectories(){
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        m_strRoot = root + "/ASSURE";

        rootBat = m_strRoot + "/Battery logs";
        rootCSI = m_strRoot + "/ModCSI and CSI logs";
        rootModCSI = rootCSI;
        rootECG = m_strRoot + "/ECG data logs";
        rootRR = m_strRoot + "/RR intervals logs";
        rootSeizure = m_strRoot + "/Seizure logs";
        rootThresh = m_strRoot + "/Threshold changes logs";

        File rootDir = new File(m_strRoot);
        File batDir = new File(rootBat);
        File csiDir = new File(rootCSI);
        File ecgDir = new File(rootECG);
        File rrDir = new File(rootRR);
        File seizureDir = new File(rootSeizure);
        File threshDir = new File(rootThresh);

        if (!rootDir.exists())
            rootDir.mkdirs();

        if (!batDir.exists())
            batDir.mkdirs();

        if (!csiDir.exists())
            csiDir.mkdirs();

        if (!ecgDir.exists())
            ecgDir.mkdirs();

        if (!rrDir.exists())
            rrDir.mkdirs();

        if (!seizureDir.exists())
            seizureDir.mkdirs();

        if (!threshDir.exists())
            threshDir.mkdirs();
    }

    private String GetSavePathBatteryLog() {
        if (rootBat == null)
            return ("/sdcard/" + filenameBat);

        return rootBat + "/" + filenameBat;
    }

    private String GetSavePathCSILog() {
        if (rootCSI == null)
            return ("/sdcard/" + filenameCSI);

        return rootCSI + "/" + filenameCSI;
    }

    private String GetSavePathModCSILog() {
        if (rootModCSI == null)
            return ("/sdcard/" + filenameModCSI);

        return rootModCSI + "/" + filenameModCSI;
    }

    private String GetSavePathECGLog() {
        if (rootECG == null)
            return ("/sdcard/" + filenameECG);

        return rootECG + "/" + filenameECG;
    }

    private String GetSavePathRRLog() {
        if (rootRR == null)
            return ("/sdcard/" + filenameRR);

        return rootRR + "/" + filenameRR;
    }

    private String GetSavePathSeizureLog() {
        if (rootSeizure == null)
            return ("/sdcard/" + filenameSeizure);

        return rootSeizure + "/" + filenameSeizure;
    }

    private String GetSavePathSeizureMarkLog() {
        if (rootSeizure == null)
            return ("/sdcard/" + filenameSeizureMark);

        return rootSeizure + "/" + filenameSeizureMark;
    }

    private String GetSavePathThreshLog() {
        if (rootThresh == null)
            return ("/sdcard/" + filenameThresh);

        return rootThresh + "/" + filenameThresh;
    }

    public void saveBatteryInfo(float c3BatPct, Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = context.registerReceiver(null, ifilter);

        // Determine phone battery percentage
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float phoneBatPct = level * 100 / (float)scale;

        String line = "\n\nPhone battery percent: " + phoneBatPct + "\nC3 battery percent: " + c3BatPct
                + "\n" + Calendar.getInstance().getTime().toString();

        FileOutputStream fos;
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

    public void saveModCSIandCSI(double modCSI, double CSI) {
        String str_modCSI = modCSI + ",";
        String str_CSI = CSI + ",";

        FileOutputStream fosModCSI;
        File fileModCSI = new File(GetSavePathModCSILog());
        try {
            fileModCSI.createNewFile();
            fosModCSI = new FileOutputStream(fileModCSI,true);

            if (fosModCSI != null) {
                fosModCSI.write(str_modCSI.getBytes());
                fileModCSI.getTotalSpace();
                fosModCSI.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream fosCSI;
        File fileCSI = new File(GetSavePathCSILog());
        try {
            fileCSI.createNewFile();
            fosCSI = new FileOutputStream(fileCSI,true);

            if (fosCSI != null) {
                fosCSI.write(str_CSI.getBytes());
                fileCSI.getTotalSpace();
                fosCSI.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveRawECG(int[] batch) {
        // Make the values comma separated
        String strLog = "";
        int Length = 12;
        for (int i = 0; i < Length; i++) {
            strLog += batch[i];
            strLog += ",";
        }

        FileOutputStream fos;
        File file = new File(GetSavePathECGLog());
        try {
            file.createNewFile();
            fos = new FileOutputStream(file,true);

            if (fos != null) {
                fos.write(strLog.getBytes());
                file.getTotalSpace();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveRRintervals(double rrInterval) {
        String str_rr = rrInterval + ",";

        FileOutputStream fos;
        File file = new File(GetSavePathRRLog());
        try {
            file.createNewFile();
            fos = new FileOutputStream(file,true);

            if (file != null) {
                fos.write(str_rr.getBytes());
                file.getTotalSpace();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSeizures() {
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm - dd.MM.yy");
        String currentTime = date.format(currentLocalTime);

        String str = "Seizure alarm triggered at: " + currentTime;
        str = str + "\n";

        FileOutputStream fos;
        File file = new File(GetSavePathSeizureLog());
        try {
            file.createNewFile();
            fos = new FileOutputStream(file,true);

            if (file != null) {
                fos.write(str.getBytes());
                file.getTotalSpace();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSeizureFeedback(boolean positive) {
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm - dd.MM.yy");
        String currentTime = date.format(currentLocalTime);

        String str = "";

        if (positive) {
            str = str + "User marked latest seizure as a true positive detection at " + currentTime;
        }
        if (!positive) {
            str = str + "User marked latest seizure as a false positive detection at " + currentTime;
        }

        str = str + "\n";

        FileOutputStream fos;
        File file = new File(GetSavePathSeizureMarkLog());
        try {
            file.createNewFile();
            fos = new FileOutputStream(file,true);

            if (file != null) {
                fos.write(str.getBytes());
                file.getTotalSpace();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveThresholdChange(double oldCSI, double oldModCSI, double newCSI, double newModCSI) {
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm - dd.MM.yy");
        String currentTime = date.format(currentLocalTime);

        String str = "User changed threshold values at " + currentTime;
        str = str + "\nOld CSI: " + oldCSI;
        str = str + "\nOld ModCSI: " + oldModCSI;
        str = str + "\nNew CSI: " + newCSI;
        str = str + "\nNew ModCSI: " + newModCSI;
        str = str + "\n\n";

        FileOutputStream fos;
        File file = new File(GetSavePathThreshLog());
        try {
            file.createNewFile();
            fos = new FileOutputStream(file,true);

            if (file != null) {
                fos.write(str.getBytes());
                file.getTotalSpace();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
