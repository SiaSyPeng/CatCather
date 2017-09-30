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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class AuthDialog extends DialogFragment {

    private AlertDialog dialog;
    private Button submitButton;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // inflate dialog view
        final View dialog_view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_auth, null);
        // build dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        dialog = alertDialogBuilder.show();
        // set basic view for alertDialog
        alertDialogBuilder.setView(dialog_view)
                .setTitle(R.string.pass_dialog)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("DIALOG", "positive clicked");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("DIALOG", "negative clicked");
                        //dialog.cancel();
                        dialog.dismiss();
                    }
                });

        /* checks if password or user is valid
         * and adjust "matched/unmatched" accordingly
         * enable positive button only when matched*/
//        submitButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        final EditText pass_re = dialog_view.findViewById(R.id.Confirm_pass);

        pass_re.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                final EditText password = getActivity().findViewById(R.id.passwrd);
                final TextView pass_check = dialog_view.findViewById(R.id.Match);
                String passwords = password.getText().toString();
                String pass_res = pass_re.getText().toString();

                // update hint and enable submit button
                if (passwords.equals(pass_res)) {
                    Log.d("DIALOG", "Password Matches");
                    pass_check.setText(R.string.pass_match);
                    submitButton.setEnabled(true);
                } else {
                    pass_check.setText(R.string.pass_unmatch);
                    submitButton.setEnabled(false);
                }
            }
        });

        dialog = alertDialogBuilder.create();
        return dialog;
    }

    @Override
    public void onStart(){
        super.onStart();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        submitButton = (Button) dialog.getButton(AlertDialog.BUTTON_POSITIVE);

    }
}