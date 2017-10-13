package com.cs65.gnf.lab2

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class AuthDialog : DialogFragment() {

    // global members to use in both onStart and onCreate
    private lateinit var dialog: AlertDialog
    private lateinit var submitButton: Button
    private lateinit var mListener: DialogListener

    // public members to pass to host
    var ifMatch: Boolean = false

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    interface DialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        // inflate dialog view
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_auth, null)
        // build dialog
        val alertDialogBuilder = AlertDialog.Builder(activity)

        dialog = alertDialogBuilder.show()
        // set basic view for alertDialog
        alertDialogBuilder.setView(dialogView)
                .setTitle(R.string.confirm_dialog_pass_dialog)

                .setPositiveButton(android.R.string.ok, { dialog, _ ->
                    dialog.dismiss()
                })
                .setNegativeButton("Cancel", { dialog, _ ->
                    dialog.dismiss()
                })

        /* checks if password or user is valid
         * and adjust "matched/unmatched" accordingly
         * enable positive button only when matched*/
        val passRe: EditText = dialogView.findViewById(R.id.Confirm_pass)

        passRe.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            // update hint and enable submit button after password confirm text changed
            override fun afterTextChanged(editable: Editable) {
                // original password
                val passwordOrigin: EditText = activity.findViewById(R.id.passwrd)
                val passwordOriginString = passwordOrigin.text.toString()
                // password retyped
                val passReString = passRe.text.toString()
                // checkView to show if it is available
                val passCheck: TextView = dialogView.findViewById(R.id.Match)

                // check if password retyped match the original password
                // and update checkView and save button
                if (passwordOriginString == passReString) {
                    passCheck.setText(R.string.confirm_dialog_pass_match)
                    ifMatch = true
                    submitButton.isEnabled = true
                } else {
                    passCheck.setText(R.string.confirm_dialog_pass_unmatch)
                    ifMatch = false
                    submitButton.isEnabled = false
                }
            }
        })

        dialog = alertDialogBuilder.create()
        return dialog
    }

    // disable save button at the beginning
    override fun onStart() {
        super.onStart()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        submitButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val activity: Activity

        if (context is Activity) {
            activity = context
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = activity as DialogListener
            } catch (e: ClassCastException) {
                // The activity doesn't implement the interface, throw exception
                throw ClassCastException(activity.toString() + " must implement AuthDialog")
            }
        }

    }
}