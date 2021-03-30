package com.au.assure.datatypes;

//package com.cortrium.opkit.datatypes;

import com.au.assure.pantompkins.OSEAFactory;
import com.au.assure.pantompkins.classification.BeatDetectionAndClassification;

public final class HeartRate {
    public class HeartRateResult
    {
        private int heartRate;
        private int rrInterval;

        public HeartRateResult(int heartRate, int rrInterval)
        {
            this.heartRate = heartRate;
            this.rrInterval = rrInterval;
        }

        public int getHeartRate() {
            return heartRate;
        }

        public int getRrInterval() {
            return rrInterval;
        }
    }

    final private int hrRunningAverageWindow = 10;
    final private int hrBlankoutSeconds = 5;

    private int mNumberOfSamplesPerBatch;
    private int mSampleRate;
    private int[] samples;

    private int sumAverage;
    private int index;
    private int count;
    private int previousHeartRate;

    private int clearHr; // If certain threshold is met then send 'HR unavailable'

    private BeatDetectionAndClassification detector;

    public HeartRate(int sampleRate, int numberOfSamplesPerBatch)
    {
        samples = new int[hrRunningAverageWindow];
        mNumberOfSamplesPerBatch = numberOfSamplesPerBatch;
        mSampleRate = sampleRate;

        detector = OSEAFactory.createBDAC(mSampleRate, mSampleRate / 2);

        reset();
    }

    public HeartRateResult determineHeartRateFromSamples(int[] samples)
    {
        HeartRateResult heartRateResult = null;
        int iRRInterval = 0;// JKN 28/2-2020. RR-interval is only calculated at peek top occurrences. Store result in other instances.
        for (int i = 0; i < mNumberOfSamplesPerBatch; i++)
        {
            heartRateResult= determineHeartRateFromSample(samples[i]);
            if( heartRateResult.rrInterval > 0 )// JKN 28/2-2020
                iRRInterval = heartRateResult.rrInterval;
        }

        if( iRRInterval > 0 )// JKN 28/2-2020
            heartRateResult.rrInterval = iRRInterval;

        return heartRateResult;
    }

    public HeartRateResult determineHeartRateFromSample(int sample)
    {
        int iInterbeatInterval = 0;

        clearHr++;

        BeatDetectionAndClassification.BeatDetectAndClassifyResult result = detector.BeatDetectAndClassify(sample);
        if (result.samplesSinceRWaveIfSuccess != 0 && result.rrInterval > 0)
        {
            clearHr = 0;
            //interbeatInterval = (result.rrInterval / mSampleRate) * 1000;
            iInterbeatInterval = ( (result.rrInterval * 1000) + (mSampleRate/2)) / mSampleRate;
            previousHeartRate = runningAverage((int)(60f / ((float)result.rrInterval / mSampleRate)));
        }

        if (clearHr >= mSampleRate * hrBlankoutSeconds)
        {
            return new HeartRateResult(0, 0);
        }

        return new HeartRateResult(previousHeartRate, iInterbeatInterval);
    }

    private int runningAverage(int sample)
    {
        if (sample > 250) // Ignore if heartrate is above 250
        {
            if (count > 0)
                return sumAverage / count;
            else
                return 0;
        }

        if (count >= hrRunningAverageWindow)
            sumAverage -= samples[index]; // Remove old value from sum
        else
            count++;

        sumAverage += sample;
        samples[index] = sample;

        index++;
        index %= hrRunningAverageWindow;

        return (int)(sumAverage / count + 0.5);
    }


    private void reset()
    {
        detector = OSEAFactory.createBDAC(mSampleRate, mSampleRate / 2);
        sumAverage = 0;
        index = 0;
        count = 0;
        previousHeartRate = 0;
        clearHr = mSampleRate * hrBlankoutSeconds + 1; // blank out HR when new connection is made
    }
}

