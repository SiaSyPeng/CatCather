package com.cs65.gnf.lab1

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import com.edmodo.cropper.CropImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CropActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        val filePath = intent.extras.get("image") as String
        val imageToCrop = BitmapFactory.decodeFile(filePath)

        val cropView: CropImageView = findViewById(R.id.CropImageView)
        cropView.setImageBitmap(imageToCrop)
    }

    fun SubmitButton(v: View?) {
        val croppedIntent = Intent(applicationContext,MainActivity::class.java)
        val cropView: CropImageView = findViewById(R.id.CropImageView)

        val uriToSaveAt = intent.extras.get(MediaStore.EXTRA_OUTPUT) as Uri
        val file = File(uriToSaveAt.path)

        try {
            val fos = FileOutputStream(file)
            val image = cropView.croppedImage
            image.compress(Bitmap.CompressFormat.JPEG,100,fos)
            fos.flush()
            fos.close()
        }
        catch (e: IOException) {
            e.printStackTrace()
        }

        croppedIntent.putExtra("cropped_image",cropView.croppedImage)

        setResult(RESULT_OK,croppedIntent)
        finish()
    }
}
