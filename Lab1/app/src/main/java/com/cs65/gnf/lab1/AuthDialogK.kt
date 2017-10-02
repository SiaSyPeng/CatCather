package com.cs65.gnf.lab1

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class AuthDialogK : DialogFragment() {

    private var dialog: AlertDialog? = null
    private var submitButton: Button? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        // inflate dialog view
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_auth, null)
        // build dialog
        val alertDialogBuilder = AlertDialog.Builder(activity)

        dialog = alertDialogBuilder.show()
        // set basic view for alertDialog
        alertDialogBuilder.setView(dialogView)
                .setTitle(R.string.pass_dialog)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, _ ->
                    Log.d("DIALOG", "positive clicked")
                    dialog.dismiss()
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                    Log.d("DIALOG", "negative clicked")
                    dialog.dismiss()
                })

        /* checks if password or user is valid
         * and adjust "matched/unmatched" accordingly
         * enable positive button only when matched*/
        //        submitButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        val passRe: EditText = dialogView.findViewById(R.id.Confirm_pass)

        passRe.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                val password = activity.findViewById<EditText>(R.id.passwrd)
                val passCheck: TextView = dialogView.findViewById(R.id.Match)
                val passwords = password.text.toString()
                val passRes = passRe.text.toString()

                // update hint and enable submit button
                if (passwords == passRes) {
                    Log.d("DIALOG", "Password Matches")
                    passCheck.setText(R.string.pass_match)
                    submitButton!!.isEnabled = true
                } else {
                    passCheck.setText(R.string.pass_unmatch)
                    submitButton!!.isEnabled = false
                }
            }
        })

        dialog = alertDialogBuilder.create()
        return dialog
    }

    // disable save button at the beginning
    override fun onStart() {
        super.onStart()
        dialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        submitButton = dialog!!.getButton(AlertDialog.BUTTON_POSITIVE) as Button

    }
}
