package com.au.assure;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.Arrays;
import java.util.List;

public class SeizureDetector {

    int mws = 100;


    /**
     * Calculates the ModCSI and CSI parameters based on the input array of RR intervals.
     *
     * @param rrIntervals The sampleRate of the ECG samples.
     * @return An array of two double values: ModCSI and CSI
     */
    public double[] CalcModCSI_and_CSI(List<Double> rrIntervals) {

        // Filter - 7 R-R interval median filter
        double[] rrFiltered = new double[mws];
        int filterCounter = 0;
        for (int j = 0; j < mws-7; j++) {

            double median;
            double[] filterBuffer = new double[7];
            for (int k = 0; k < 7; k++) {
                filterBuffer[k] = rrIntervals.get(k + filterCounter);
            }
            Arrays.sort(filterBuffer); // Sorting the 7 values

            if (filterBuffer.length % 2 == 0) // If devisable by 2 (always is, when size is 7)
                median = (filterBuffer[filterBuffer.length/2] + filterBuffer[filterBuffer.length/2 - 1])/2;
            else
                median = filterBuffer[filterBuffer.length/2];

            rrFiltered[j] = median; // rrFiltered is the 100 RR interval moving window

            filterCounter++;
        }

        // Calculate slope of RR intervals
        double[] x_axis = new double[mws]; // The x-axis is the accumulated RR intervals
        for (int j = 0; j < mws; ++j) {
            if (j>0){ // The point before + the current RR interval
                x_axis[j] = x_axis[j-1] + rrFiltered[j];
            }
            else { // First point is just the first RR-interval
                x_axis[j] = rrFiltered[j];
            }
        }

        double[] y_axis = new double[mws]; // The y-axis data is the heart rate in minutes
        for (int j = 0; j < mws; ++j) {
            y_axis[j] = 60/rrFiltered[j];
        }

        SimpleRegression regression = new SimpleRegression(); // doing the regression ('org.apache.commons:commons-math3:3.6.1')
        for (int j = 0; j < x_axis.length; j++) {
            regression.addData(x_axis[j], y_axis[j]);
        }
        double slope = Math.abs(regression.getSlope());

        // Calculate SD1 - filtered
        double[] rrDiff_filt = new double[mws-1];
        for (int j = 0; j < mws-1; j++) {
            rrDiff_filt[j] = rrFiltered[j+1] - rrFiltered[j];
        }

        for (int j = 0; j < mws-1; j++) {
            rrDiff_filt[j] = rrDiff_filt[j] * (Math.sqrt(2)/2);
        }

        StandardDeviation std = new StandardDeviation();
        double SD1_filt = 1000*std.evaluate(rrDiff_filt);

        // Calculate SD2 - filtered
        double[] rrSum_filt = new double[mws-1];
        for (int j = 0; j < mws-1; j++) {
            rrSum_filt[j] = rrFiltered[j] + rrFiltered[j+1];
        }

        for (int j = 0; j < mws-1; j++) {
            rrSum_filt[j] = rrSum_filt[j] * (Math.sqrt(2)/2);
        }

        double SD2_filt = 1000*std.evaluate(rrSum_filt);

        // Calculate SD1 - unfiltered
        double[] rrDiff_unFilt = new double[mws-1];
        for (int j = 0; j < mws-1; j++) {
            rrDiff_unFilt[j] = rrIntervals.get(j + 7) - rrIntervals.get(j + 8);
        }

        for (int j = 0; j < mws-1; j++) {
            rrDiff_unFilt[j] = rrDiff_unFilt[j] * (Math.sqrt(2)/2);
        }

        double SD1_unFilt = 1000*std.evaluate(rrDiff_unFilt);

        // Calculate SD2 - unfiltered
        double[] rrSum_unFilt = new double[mws-1];
        for (int j = 0; j < mws-1; j++) {
            rrSum_unFilt[j] = rrIntervals.get(j + 7) + rrIntervals.get(j + 8);
        }

        for (int j = 0; j < mws-1; j++) {
            rrSum_unFilt[j] = rrSum_unFilt[j] * (Math.sqrt(2)/2);
        }

        double SD2_unFilt = 1000*std.evaluate(rrSum_unFilt);


        // Calculate T based on filtered
        double T_filt = 4 * SD1_filt;

        // Calculate L based on filtered data
        double L_filt = 4 * SD2_filt;

        // Calculate T based on unfiltered
        double T_unFilt = 4 * SD1_unFilt;

        // Calculate L based on unfiltered data
        double L_unFilt = 4 * SD2_unFilt;

        // Calculate ModCSI100 filtered and multiplied by slope
        double modCSI = (L_filt * L_filt) / T_filt;
        double modCSI_slope = modCSI * slope;

        // Calculate CSI100, no filtering
        double CSI = L_unFilt/T_unFilt;
        double CSI_slope = CSI * slope;

        double[] modCSI_and_CSI = new double[2];
        modCSI_and_CSI[0] = modCSI_slope;
        modCSI_and_CSI[1] = CSI_slope;

        return modCSI_and_CSI;
    }



}
