package com.au.assure;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentLogSettings extends DialogFragment {
    boolean logBattery;
    boolean logRawECG;
    boolean logRRintervals;
    boolean logSeizureVals;

    public DialogFragmentLogSettings(boolean logBattery, boolean logRawECG, boolean logRRintervals, boolean logSeizureVals) {
        this.logBattery = logBattery;
        this.logRawECG = logRawECG;
        this.logRRintervals = logRRintervals;
        this.logSeizureVals = logSeizureVals;
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(boolean logBattery, boolean logRawECG, boolean logRRintervals, boolean logSeizureVals);
        public void onDialogNegativeClick();
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (DialogFragmentLogSettings.NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.log_settings_dialog, null);
        Switch swLogBattery = view.findViewById(R.id.swBattery);
        Switch swLogRawECG = view.findViewById(R.id.swRawECG);
        Switch swLogRRintervals = view.findViewById(R.id.swRR);
        Switch swLogSeizureVals = view.findViewById(R.id.swSeizure);

        swLogBattery.setChecked(logBattery);
        swLogRawECG.setChecked(logRawECG);
        swLogRRintervals.setChecked(logRRintervals);
        swLogSeizureVals.setChecked(logSeizureVals);

        builder.setTitle(R.string.logSettingsTitle)
                .setView(view)
                .setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDialogPositiveClick(swLogBattery.isChecked(),swLogRawECG.isChecked(),swLogRRintervals.isChecked(),swLogSeizureVals.isChecked());
                    }
                })
                .setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User cancelled the dialog
                        listener.onDialogNegativeClick();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
