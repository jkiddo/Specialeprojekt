package com.example.seizureapp.datapackages;

import android.os.Parcel;
import android.os.Parcelable;

import com.cortrium.cortrium.CortriumC3;
import com.cortrium.cortrium.PeakDetector;
import com.cortrium.cortrium.Utils;
import com.cortrium.cortrium.data.singleton_DebugSettings;
import com.cortrium.cortrium.datatypes.ECGSamples;
import com.cortrium.cortrium.datatypes.HeartRate;
import com.cortrium.cortrium.datatypes.MiscInfo;
import com.cortrium.cortrium.datatypes.MiscInfo_C3_PLUS;
import com.cortrium.cortrium.filters.EcgHighPassFilter;
import com.cortrium.cortrium.filters.EcgLowPassFilter;
import com.cortrium.cortrium.filters.RespHighPassFilter;
import com.cortrium.cortrium.filters.RespLowPassFilter;

import java.nio.ByteBuffer;


/**
 * Copyright (C) 2002 - 2016 Docobo Ltd.
 * <br><br>
 * Class EcgData
 * <br><br>
 * TODO - Class summary
 */
public class EcgData implements Parcelable
{
    private MiscInfo miscInfo = null;
    private MiscInfo_C3_PLUS miscInfo_C3_PLUS = null;
    private final boolean  fillerSamples;

    private float rawRespirationSample;
    private int[] rawEcg1Samples;
    private int[] rawEcg2Samples;
    private int[] rawEcg3Samples;

    private float filteredRespirationSample;
    private int[] filteredEcg1Samples;
    private int[] filteredEcg2Samples;
    private int[] filteredEcg3Samples;

    private int respiratoryRate;
    private int heartRate;
    private int rrInterval;

    private ByteBuffer rawBlePayload;

    // JKN 12/9-2019 : for new C3Plus device
    private int m_iSampleCount = ECGSamples.NUMBER_OF_SAMPLES;// Value for C3 original
    private int m_iSampleIndex = 0;


    public EcgData()
    {
        this(false);
    }

    public EcgData(boolean fillerSamples)
    {
        this.fillerSamples = fillerSamples;
    }

    protected EcgData(Parcel in)
    {
        miscInfo = in.readParcelable(MiscInfo.class.getClassLoader());
        fillerSamples = in.readByte() != 0;
        rawRespirationSample = in.readFloat();
        rawEcg1Samples = in.createIntArray();
        rawEcg2Samples = in.createIntArray();
        rawEcg3Samples = in.createIntArray();
        filteredRespirationSample = in.readFloat();
        filteredEcg1Samples = in.createIntArray();
        filteredEcg2Samples = in.createIntArray();
        filteredEcg3Samples = in.createIntArray();
        respiratoryRate = in.readInt();
        heartRate = in.readInt();
        rrInterval = in.readInt();
    }

    private singleton_DebugSettings m_sDebugSettings = null;
    public void SetDebugSettings(singleton_DebugSettings sDebugSettings){m_sDebugSettings = sDebugSettings;};

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(miscInfo, flags);
        dest.writeByte((byte) (fillerSamples ? 1 : 0));
        dest.writeFloat(rawRespirationSample);
        dest.writeIntArray(rawEcg1Samples);
        dest.writeIntArray(rawEcg2Samples);
        dest.writeIntArray(rawEcg3Samples);
        dest.writeFloat(filteredRespirationSample);
        dest.writeIntArray(filteredEcg1Samples);
        dest.writeIntArray(filteredEcg2Samples);
        dest.writeIntArray(filteredEcg3Samples);
        dest.writeInt(respiratoryRate);
        dest.writeInt(heartRate);
        dest.writeInt(rrInterval);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<EcgData> CREATOR = new Creator<EcgData>()
    {
        @Override
        public com.cortrium.cortrium.datapackages.EcgData createFromParcel(Parcel in)
        {
            return new com.cortrium.cortrium.datapackages.EcgData(in);
        }

        @Override
        public com.cortrium.cortrium.datapackages.EcgData[] newArray(int size)
        {
            return new com.cortrium.cortrium.datapackages.EcgData[size];
        }
    };

    public MiscInfo getMiscInfo()
    {
        return miscInfo;
    }

    public MiscInfo_C3_PLUS getMiscInfo_C3_PLUS()
    {
        return miscInfo_C3_PLUS;
    }

    public void setMiscInfo(MiscInfo miscInfo, RespHighPassFilter highPassFilter, RespLowPassFilter lowPassFilter, PeakDetector peakDetector)
    {
        this.miscInfo = miscInfo;
        // Get the Raw Sample value from bytes
        this.rawRespirationSample = (float) (Utils.convertSampleValue(miscInfo.getRespirationBytes()) / 256);

        if (highPassFilter != null && lowPassFilter != null)
        {
            this.filteredRespirationSample = filterRespSample(this.rawRespirationSample, highPassFilter, lowPassFilter);

            int numberOfRespPeaks = peakDetector.detectPeak(this.filteredRespirationSample);
            if (numberOfRespPeaks > 0)
            {
                this.respiratoryRate = numberOfRespPeaks;
            }
        }
        else
        {
            this.filteredRespirationSample = this.rawRespirationSample;
        }
    }

    public void setRawBlePayload(ByteBuffer rawBlePayload)
    {
        this.rawBlePayload = rawBlePayload;
    }

    public void setEcg1Data(ECGSamples ecgData)
    {
        // Get the Raw Sample value from bytes
        this.rawEcg1Samples = getEcgRawSamples(ecgData);
    }

    public void setEcg2Data(ECGSamples ecgData)
    {
        // Get the Raw Sample value from bytes
        this.rawEcg2Samples = getEcgRawSamples(ecgData);
    }

    public void setEcg3Data(ECGSamples ecgData)
    {
        // Get the Raw Sample value from bytes
        this.rawEcg3Samples = getEcgRawSamples(ecgData);
    }

    // JKN 12/9-2019 start
    public int GetSampleCount(){return m_iSampleCount;}
    public int GetSampleIndex(){return m_iSampleIndex;}
    public void SetSampleIndex( int iSampleIndex ){m_iSampleIndex = iSampleIndex;}

    public void setEcgData(int[]ecg1,int[]ecg2,int[]ecg3,int iSampleCount) {
        if (iSampleCount > 0) {
            m_iSampleCount = iSampleCount;
            this.rawEcg1Samples = copyIntBuffer(ecg1, m_iSampleCount);
            this.rawEcg2Samples = copyIntBuffer(ecg2, m_iSampleCount);
            this.rawEcg3Samples = copyIntBuffer(ecg3, m_iSampleCount);

        }
    }

    int [] copyIntBuffer( int[]ecg,int iSampleCount)
    {
        int[] newIntBuffer = new int[iSampleCount];
        for(int i=0;i<iSampleCount;i++)
            newIntBuffer[i] = ecg[i];

        return newIntBuffer;
    }
    // JKN 12/9-2019 end

    public boolean getLeadOff() { return getMiscInfo().getLeadOffBits() != 0; }

    public boolean isFillerSamples()
    {
        return fillerSamples;
    }

    public float getRawRespirationSample()
    {
        return rawRespirationSample;
    }

    public ByteBuffer getRawBlePayload() { return rawBlePayload; }

    public int[] getRawEcg1Samples()
    {
        return rawEcg1Samples;
    }

    public int[] getRawEcg2Samples()
    {
        return rawEcg2Samples;
    }

    public int[] getRawEcg3Samples()
    {
        return rawEcg3Samples;
    }

    public float getFilteredRespirationSample()
    {
        return filteredRespirationSample;
    }

    public int[] getFilteredEcg1Samples()
    {
        return filteredEcg1Samples;
    }

    public int[] getFilteredEcg2Samples()
    {
        return filteredEcg2Samples;
    }

    public int[] getFilteredEcg3Samples()
    {
        return filteredEcg3Samples;
    }

    public int getRespiratoryRate() {
        if (getLeadOff()) {
            return 0;
        }

        return respiratoryRate; }

    public int getHeartRate() {
        if (getLeadOff()) {
            return 0;
        }

        return heartRate;
    }

    public int getHeartRateRaw() // JKN 13/9-2019
    {
        return heartRate;
    }
    public int getRrIntervalRaw()// JKN 13/9-2019
    {
        return rrInterval;
    }

    public int getRrInterval() {

        if( getMiscInfo()==null )
            return 0;

        if (getLeadOff()) {
            return 0;
        }
        return rrInterval;
    }

    private int[] getEcgRawSamples(ECGSamples ecgData)
    {
        int[] samples;
        if (this.fillerSamples)
        {
            samples = new int[ECGSamples.NUMBER_OF_SAMPLES];
            for (int index = 0; index < ECGSamples.NUMBER_OF_SAMPLES; index++)
                samples[index] = CortriumC3.SAMPLE_COMM_ERR;
        }
        else
        {
            samples = ecgData.getRawEcgValues();
        }

        return samples;
    }

    public void applyFilters(EcgHighPassFilter[] highPassFilters, EcgLowPassFilter[] lowPassFilters, HeartRate heartRateDetector)
    {
        if (rawEcg1Samples != null)
        {
            this.filteredEcg1Samples = filterEcgSamples(rawEcg1Samples, highPassFilters[0], lowPassFilters[0]);
        }

        if (rawEcg2Samples != null)
        {
            this.filteredEcg2Samples = filterEcgSamples(rawEcg2Samples, highPassFilters[1], lowPassFilters[1]);
        }

        if (rawEcg3Samples != null)
        {
            this.filteredEcg3Samples = filterEcgSamples(rawEcg3Samples, highPassFilters[2], lowPassFilters[2]);
            // For now just use ECG3 for heartrate
            HeartRate.HeartRateResult result = heartRateDetector.determineHeartRateFromSamples(filteredEcg3Samples);
            heartRate =  result.getHeartRate();
            rrInterval = result.getRrInterval();
        }
    }


    public void applyFilters_C3_PLUS(EcgHighPassFilter[] highPassFilters, EcgLowPassFilter[] lowPassFilters, HeartRate heartRateDetector)
    {
        if (rawEcg1Samples != null)
        {
            this.filteredEcg1Samples = filterEcgSamples_C3_PLUS(rawEcg1Samples , m_iSampleCount, highPassFilters[0], lowPassFilters[0]);
        }

        if (rawEcg2Samples != null)
        {
            this.filteredEcg2Samples = filterEcgSamples_C3_PLUS(rawEcg2Samples, m_iSampleCount, highPassFilters[1], lowPassFilters[1]);
        }

        if (rawEcg3Samples != null)
        {
            this.filteredEcg3Samples = filterEcgSamples_C3_PLUS(rawEcg3Samples, m_iSampleCount, highPassFilters[2], lowPassFilters[2]);
            // For now just use ECG3 for heartrate
            HeartRate.HeartRateResult result = heartRateDetector.determineHeartRateFromSamples(filteredEcg3Samples);
            heartRate = result.getHeartRate();
            //rrInterval = result.getRrInterval()
            if( result.getRrInterval()>0)// JKN 28/2-2020
            {
                rrInterval = result.getRrInterval();
            }
        }
    }

    private final boolean FILTERING_ENABLED = true;
    private float filterRespSample(float respSample, RespHighPassFilter highPassFilter, RespLowPassFilter lowPassFilter)
    {
        float highpassFilteredSample = highPassFilter.filterInput(respSample);
        return lowPassFilter.filterInput(highpassFilteredSample);
    }

    private int[] filterEcgSamples(int[] rawEcgSamples, EcgHighPassFilter highPassFilter, EcgLowPassFilter lowPassFilter)
    {
        int[] filteredEcgSamples = new int[ECGSamples.NUMBER_OF_SAMPLES];
        short filteredSample = 0;
        for (int index = 0; index < ECGSamples.NUMBER_OF_SAMPLES; index++)
        {
            if (FILTERING_ENABLED)
            {
                if( m_sDebugSettings==null) {
                    filteredSample = highPassFilter.filterInput(rawEcgSamples[index]);
                    filteredEcgSamples[index] = lowPassFilter.filterInput(filteredSample);
                }
                else
                {
                    if( m_sDebugSettings.Get_UseHighPassFilter() && m_sDebugSettings.Get_UseLowPassFilter() )
                    {
                        filteredSample = highPassFilter.filterInput(rawEcgSamples[index]);
                        filteredEcgSamples[index] = lowPassFilter.filterInput(filteredSample);
                    }
                    else if ( m_sDebugSettings.Get_UseHighPassFilter() )
                    {
                        // Only high pass filter
                        filteredEcgSamples[index]  = highPassFilter.filterInput(rawEcgSamples[index]);
                    }
                    else if( m_sDebugSettings.Get_UseLowPassFilter() )
                    {
                        // only low pass filter
                        filteredEcgSamples[index] = lowPassFilter.filterInput((short)rawEcgSamples[index]);
                    }
                    else // New default
                        filteredEcgSamples[index] = rawEcgSamples[index];
                }
            }
            else
            {
                filteredEcgSamples[index] = rawEcgSamples[index];
            }
        }

        return filteredEcgSamples;
    }

    // JKN 10/9-2019
    private int[] filterEcgSamples_C3_PLUS(int[] rawEcgSamples, int iNumberOfSamples, EcgHighPassFilter highPassFilter, EcgLowPassFilter lowPassFilter)
    {
        if( iNumberOfSamples<=0)
            throw new IllegalArgumentException("ECG 'number of data' cannot be null");

        int[] filteredEcgSamples = new int[iNumberOfSamples];
        short filteredSample = 0;
        for (int index = 0; index < iNumberOfSamples; index++)
        {
            if (FILTERING_ENABLED)
            {
                if( (m_sDebugSettings==null) ) {
                    filteredSample = highPassFilter.filterInput(rawEcgSamples[index]);
                    filteredEcgSamples[index] = lowPassFilter.filterInput(filteredSample);
                }
                else if( m_sDebugSettings.GetShowDebugInfo()==false )
                {
                    filteredSample = highPassFilter.filterInput(rawEcgSamples[index]);
                    filteredEcgSamples[index] = lowPassFilter.filterInput(filteredSample);
                }
                else
                {
                    if( m_sDebugSettings.Get_UseHighPassFilter() && m_sDebugSettings.Get_UseLowPassFilter() )
                    {
                        filteredSample = highPassFilter.filterInput(rawEcgSamples[index]);
                        filteredEcgSamples[index] = lowPassFilter.filterInput(filteredSample)*4;
                    }
                    else if ( m_sDebugSettings.Get_UseHighPassFilter() )
                    {
                        // Only high pass filter
                        filteredEcgSamples[index]  = highPassFilter.filterInput(rawEcgSamples[index])*4;
                    }
                    else if( m_sDebugSettings.Get_UseLowPassFilter() )
                    {
                        // only low pass filter
                        filteredEcgSamples[index] = lowPassFilter.filterInput((short)rawEcgSamples[index]);
                    }
                    else // New default
                        filteredEcgSamples[index] = rawEcgSamples[index];
                }
            }
            else
            {
                filteredEcgSamples[index] = rawEcgSamples[index];
            }
        }

        return filteredEcgSamples;
    }

    // JKN 29/11-2019
    public void SetAccelerometerRaw(int acc_x, int acc_y, int acc_z) {
        if( miscInfo == null )
            miscInfo = new MiscInfo(null);
        if( miscInfo != null )
        {
            miscInfo.SetAccelerometerRawX(acc_x);
            miscInfo.SetAccelerometerRawY(acc_y);
            miscInfo.SetAccelerometerRawZ(acc_z);

            miscInfo.FindAccelerometerOrientation();
        }
    }
    // JKN 10/9-2019
}
