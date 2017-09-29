package com.cs65.gnf.lab1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class AuthDialog extends DialogFragment {


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DialogListener {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }

    DialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // inflate dialog view
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_auth, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // checks if password or user is valid
        // and adjust "matched/unmatched" accordingly
        final EditText pass_re = (EditText) v.findViewById(R.id.Confirm_pass);

        pass_re.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                final EditText password = getActivity().findViewById(R.id.passwrd);
                final TextView pass_check = getActivity().findViewById(R.id.Match);
                String passwords = password.getText().toString();
                String pass_res = pass_re.getText().toString();

                // match/unmatch change
                String match = getString(R.string.pass_match);

                if (passwords.equals(pass_res)) {

                    pass_check.setText(match);
                } else {
                    pass_check.setText(R.string.pass_unmatch);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        alertDialogBuilder.setView(v);
        alertDialogBuilder.setTitle(R.string.pass_dialog)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("DIALOG", "positive clicked");

                        // collect password
                        final Text pass_check = getActivity().findViewById(R.id.Match);
                        String pass_res = pass_re.getText().toString();

                        if (pass_check.equals(R.string.pass_match)){
                            mListener.onDialogPositiveClick(AuthDialog.this);
                        }
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Log.d("DIALOG", "negative clicked");
                        mListener.onDialogPositiveClick(AuthDialog.this);
                        dialog.cancel();
                    }
                })
                .create();

        return alertDialogBuilder.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;

        if (context instanceof Activity){
            activity= (Activity) context;
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (DialogListener) activity;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(activity.toString()
                        + " must implement NoticeDialogListener");
            }
            Log.d("DIALOG", "attached");
        }

    }
}