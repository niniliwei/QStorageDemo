package com.niniliwei.qstoragedemo

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "QStorageDemo"
        private const val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_FOR_IMAGES = 1
        private const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_FOR_DELETING_FIRST_IMAGE = 2
        private const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_FOR_WRITING_TO_SDCARD = 3
        private const val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_FOR_DOWNLOADS = 4
        private const val REQUEST_CODE_OPEN_DOCUMENT = 5
        private const val REQUEST_CODE_CREATE_DOCUMENT = 6
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // 向 MediaStore.Images 中写入文件
        writeFileToMediaImagesButton.setOnClickListener {
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
        ////////////////////////////////////////////////////////////////////////////////////////////
        // 指定相对路径和设备名称
        customRelativePathAndVolumeButton.setOnClickListener {
            val volumeNames = MediaStore.getAllVolumeNames(this)
                    .filter { it != "internal" }
                    .toTypedArray()
            AlertDialog.Builder(this)
                    .setTitle("请选择卷名")
                    .setItems(volumeNames) { _, which ->
                        writeMp3FileToMusic(volumeNames[which])
                    }
                    .show()
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        // 不申请权限读取所有图片
        readImagesWithoutPermissionButton.setOnClickListener {
            readAllImages()
        }
        // 申请权限读取所有图片
        readImagesWithPermissionButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_FOR_IMAGES
                )
            } else {
                readAllImages()
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        // 向 MediaStore.Downloads 中写入文件
        writeFileToMediaDownloadsButton.setOnClickListener {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "IMG1024_Downloads.JPG")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
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
        // 不申请权限读取 MediaStore.Downloads 中所有文件
        readDownloadsWithoutPermissionButton.setOnClickListener {
            readDownloadsFiles()
        }
        // 申请权限读取 MediaStore.Downloads 中所有文件
        readDownloadsWithPermissionButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_FOR_DOWNLOADS
                )
            } else {
                readDownloadsFiles()
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        // 不申请权限删除所有图片中的第一张
        deleteFirstImageWithoutPermissionButton.setOnClickListener {
            deleteFirstImage()
        }
        // 申请权限删除所有图片中的第一张
        deleteFirstImageWithPermissionButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_FOR_DELETING_FIRST_IMAGE
                )
            } else {
                deleteFirstImage()
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        // 向 SD 卡写入文件
        writeFileToSDCardButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_FOR_WRITING_TO_SDCARD
                )
            } else {
                writeFileToSDCard()
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        // 通过 SAF 打开文档
        safOpenDocumentButton.setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                // 指定文件类型
                type = "text/plain"
                startActivityForResult(this, REQUEST_CODE_OPEN_DOCUMENT)
            }
        }
        // 通过 SAF 创建文档
        safCreateDocumentButton.setOnClickListener {
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                // 指定文件类型
                type = "text/plain"
                // 指定文件名称
                putExtra(Intent.EXTRA_TITLE, "hello.txt")
                startActivityForResult(this, REQUEST_CODE_CREATE_DOCUMENT)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT) {
            data?.data?.let {
                Log.i(TAG, "open document: $it")
                // contentResolver.openInputStream(it)
                // contentResolver.openFileDescriptor(it, "r")
            }
        } else if (requestCode == REQUEST_CODE_CREATE_DOCUMENT) {
            data?.data?.let {
                try {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write("Hello World!".toByteArray())
                    }

                    Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_FOR_IMAGES) {
                readAllImages()
            } else if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_FOR_DELETING_FIRST_IMAGE) {
                deleteFirstImage()
            } else if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_FOR_WRITING_TO_SDCARD) {
                writeFileToSDCard()
            }
        } else {
            Log.i(TAG, "PERMISSION REQUEST DENIED!!!")
        }
    }

    /**
     * 向 Music 中写入一个 mp3 文件，并制定设备和相对路径
     */
    private fun writeMp3FileToMusic(volumeName: String) {
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

    /**
     * 读取所有图片
     */
    private fun readAllImages() {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        // val collectionWithPending = MediaStore.setIncludePending(collection)
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

    /**
     * 删除第一张图片
     */
    private fun deleteFirstImage() {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(collection, null, null, null, null)
                ?: return

        cursor.use {
            if (cursor.moveToFirst()) {
                val imageUri = ContentUris.withAppendedId(
                        collection,
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                )

                try {
                    val result = contentResolver.delete(imageUri, null, null)
                    Log.i(TAG, "delete result: $result")
                } catch (e: RecoverableSecurityException) {
                    AlertDialog.Builder(this)
                            .setMessage(e.userMessage)
                            .setPositiveButton(e.userAction.title) { dialog, which ->
                                try {
                                    e.userAction.actionIntent.send()
                                } catch (ignored: PendingIntent.CanceledException) {
                                }
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                }
            }
        }
    }

    /**
     * 向 SD 卡中写入文件
     */
    private fun writeFileToSDCard() {
        val dir = Environment.getExternalStoragePublicDirectory("MyPictures")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "IMG1024.JPG")
        try {
            resources.assets.open("tim-meyer.jpg").use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 读取 MediaStorage.Downloads 下所有文件
     */
    private fun readDownloadsFiles() {
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(collection, null, null, null, null) ?: return

        cursor.use {
            Log.i(TAG, "downloads cursor size: ${cursor.count}")
            while (cursor.moveToNext()) {
                val item = ContentUris.withAppendedId(
                        collection,
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Downloads._ID))
                )
                val displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                val mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
                Log.i(TAG, "downloads item: $item(displayName: $displayName, mimeType:$mimeType)")
            }
        }
    }
}
