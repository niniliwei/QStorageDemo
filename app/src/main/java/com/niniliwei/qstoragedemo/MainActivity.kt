package com.niniliwei.qstoragedemo

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.os.BuildCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "QStorageDemo"
        private const val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        contributingFilesButton.setOnClickListener {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "IMG1024.JPG")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val item = contentResolver.insert(collection, values) ?: return@setOnClickListener

            try {
                contentResolver.openOutputStream(item)?.use { outputStream ->
                    resources.assets.open("tim-meyer.jpg").use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                values.clear()
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                contentResolver.update(item, values, null, null)

                Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show()
            }

        }
        customRelativePathAndVolumeButton.setOnClickListener {
            if (BuildCompat.isAtLeastQ()) {
                val volumeNames = MediaStore.getAllVolumeNames(this)
                        .filter { it != "internal" }
                        .toTypedArray()
                AlertDialog.Builder(this)
                        .setTitle("请选择卷名")
                        .setItems(volumeNames) { _, which ->
                            insertMp3File(volumeNames[which])
                        }
                        .show()
            }
        }
        readImagesWithoutPermissionButton.setOnClickListener {
            readAllImages()
        }
        readImagesWithPermissionButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_PERMISSION_READ_EXTERNAL_STORAGE
                )
            } else {
                readAllImages()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
                readAllImages()
            }
        } else {
            Log.i(TAG, "PERMISSION REQUEST DENIED!!!")
        }
    }

    private fun readAllImages() {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(collection, null, null, null, null) ?: return

        cursor.use {
            Log.i(TAG, "images cursor size: ${cursor.count}")
            while (cursor.moveToNext()) {
                val imageUri = ContentUris.withAppendedId(
                        collection,
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                )
                val displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                val mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
                Log.i(TAG, "imageUri: $imageUri(displayName: $displayName, mimeType:$mimeType)")
            }
        }
    }

    private fun insertMp3File(volumeName: String) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.RELATIVE_PATH,
                    "Music/My Artist/My Album")
            put(MediaStore.Audio.Media.DISPLAY_NAME,
                    "My Song.mp3")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Audio.Media.getContentUri(volumeName)
        val item = contentResolver.insert(collection, values) ?: return

        try {
            contentResolver.openOutputStream(item)?.use { outputStream ->
                resources.assets.open("libai.mp3").use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            values.clear()
            values.put(MediaStore.Audio.Media.IS_PENDING, 0)
            contentResolver.update(item, values, null, null)

            Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show()
        }
    }
}
