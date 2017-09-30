package com.cs65.gnf.lab1

import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.edmodo.cropper.CropImageView

class CropActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        val imageToCrop = intent.extras.get("image") as Bitmap

        val cropView: CropImageView = findViewById(R.id.CropImageView)
        cropView.setImageBitmap(imageToCrop)
    }

    fun SubmitButton(v: View?) {
        val croppedIntent = Intent(applicationContext,MainActivity::class.java)
        val cropView: CropImageView = findViewById(R.id.CropImageView)
        croppedIntent.putExtra("cropped_image",cropView.croppedImage)

        setResult(RESULT_OK,croppedIntent)
        finish()
    }
}
