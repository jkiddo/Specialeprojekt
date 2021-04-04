package com.au.assure;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentEditValues extends DialogFragment {
    String initValModCSI;
    String initValCSI;

    public DialogFragmentEditValues(double initModCSI, double initCSI) {
        initValModCSI = Double.toString(initModCSI);
        initValCSI = Double.toString(initCSI);
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(double modCSI, double CSI);
        public void onDialogNegativeClick();
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_values_dialog, null);
        EditText modCSI = view.findViewById(R.id.ModCSIDialog);
        EditText CSI = view.findViewById(R.id.CSIDialog);
        modCSI.setText(initValModCSI);
        CSI.setText(initValCSI);
        builder.setTitle(R.string.changeThresh)
                .setView(view)
                .setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        boolean isError = true;
                        double modCSI_double = 0;
                        double CSI_double = 0;

                        try {
                            modCSI_double = Double.parseDouble(String.valueOf(modCSI.getText()));
                            isError = false;
                        }catch (Exception e) {
                            modCSI.setError(getString(R.string.inputError));
                            isError = true;
                        }

                        try {
                            CSI_double = Double.parseDouble(String.valueOf(CSI.getText()));
                            isError = false;
                        }catch (Exception e) {
                            CSI.setError(getString(R.string.inputError));
                            isError = true;
                        }

                        if (!isError)
                            listener.onDialogPositiveClick(modCSI_double,CSI_double);

                    }
                })
                .setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        listener.onDialogNegativeClick();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}