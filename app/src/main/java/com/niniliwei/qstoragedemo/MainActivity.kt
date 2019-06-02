package com.niniliwei.qstoragedemo

import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BuildCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

            Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "导入失败", Toast.LENGTH_SHORT).show()
        }
    }
}
