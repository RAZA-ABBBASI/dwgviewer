package com.it22.myapplication

import android.R.attr
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.groupdocs.cloud.viewer.api.FileApi
import com.groupdocs.cloud.viewer.api.InfoApi
import com.groupdocs.cloud.viewer.api.ViewApi
import com.groupdocs.cloud.viewer.client.ApiException
import com.groupdocs.cloud.viewer.client.Configuration
import com.groupdocs.cloud.viewer.model.*
import com.groupdocs.cloud.viewer.model.requests.CreateViewRequest
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLEncoder
import java.util.jar.Manifest
import com.groupdocs.cloud.viewer.model.requests.DeleteFolderRequest

import com.groupdocs.cloud.viewer.model.requests.ObjectExistsRequest

import com.groupdocs.cloud.viewer.model.requests.MoveFileRequest

import com.groupdocs.cloud.viewer.model.ObjectExist

import com.groupdocs.cloud.viewer.model.requests.CopyFileRequest

import com.groupdocs.cloud.viewer.model.requests.CreateFolderRequest
import android.R.attr.path

import com.groupdocs.cloud.viewer.model.requests.UploadFileRequest







class MainActivity2 : AppCompatActivity() {

    private val TAG = MainActivity::class.java.name
//    var appSid = "e24b6714-9768-480a-88d1-28b6929dca59"
//    var appKey = "0495f2872fa0fc29e239fe6df3c30101"
    var apiInstance: ViewApi? = null
    var fileApi:FileApi?=null
    val REQ_CODE_PERMISSION = 10101
    var someActivityResultLauncher : ActivityResultLauncher<Intent>?=null
    val arrayOfPermissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE)
    var appSid = "e24b6714-9768-480a-88d1-28b6929dca69"
    var appKey = "0495f2872fa0fc29e239fe6df3c30121"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)

        val configuration = Configuration(appSid, appKey)
        val infoApi = InfoApi(configuration)
        apiInstance = ViewApi(configuration)
        fileApi=FileApi(configuration)

        // Request code for selecting a PDF document.
        val PICK_PDF_FILE = 2


        someActivityResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                val data = result.data
                val uri =  data!!.data
                Thread {
                    try {
                        val fileInfo = FileInfo()
                        val filePath = getRealPathFromURI(this, uri!!)
                        val file = File(uri.path)
                        Log.e(TAG, filePath)
                        Log.e(TAG, this.filesDir.absolutePath)

//                        fileInfo.filePath = filePath
                        fileInfo.filePath = "https://www.dropbox.com/s/w1ad4yvbmutg76b/sample.dwg?dl=0"
                        val viewOptions = ViewOptions()
                        viewOptions.fileInfo = fileInfo
                        viewOptions.viewFormat = ViewOptions.ViewFormatEnum.HTML
                        val renderOptions = HtmlOptions()
                        val cadOptions = CadOptions()
                        cadOptions.addLayersItem("TRIANGLE")
                        cadOptions.addLayersItem("QUADRANT")
                        renderOptions.cadOptions = cadOptions
                        viewOptions.renderOptions = renderOptions

//                        val response: ViewResult = apiInstance.createView(CreateViewRequest(viewOptions));
//                        val response = apiInstance!!.createView(CreateViewRequest(viewOptions))
//                        Log.e(TAG, "RenderLayers completed: " + response.pages.size);

                        fileApi!!.uploadFile(UploadFileRequest(filePath, file, null))
                        Log.e(TAG, "File Uploaded");

//                        val response = infoApi.supportedFileFormats
//                        for (format in response.formats) {
//                            Log.e(TAG, format.fileFormat)
//                        }
                    } catch (e: ApiException) {
                        Log.e(TAG, "Exception: " + e.message)
                        e.printStackTrace()
                    }
                }.start()
            }
        }

        if(!checkPermission(arrayOfPermissions)){
            getPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            selectDwgFile()
        }


    }

    fun selectDwgFile(){



//        var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
        chooseFile.type = "*/*"
        chooseFile = Intent.createChooser(chooseFile, "Choose a DWG file")
        someActivityResultLauncher!!.launch(chooseFile)
    }

    @SuppressLint("NewApi")
    fun getRealPathFromURI(context: Context, uri: Uri): String {
        val isKitKat: Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) { //main if start

            // DocumentProvider
            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(context, uri)
            } else if (isExternalStorageDocument(uri)) {// ExternalStorageProvider

                val docId: String = DocumentsContract.getDocumentId(uri)
                val split: List<String> = docId.split(":")
                val type: String = split[0]
                // This is for checking Main Memory
                if ("primary".equals(type, ignoreCase = true)) {
                    if (split.size > 1) {
                        return context.getExternalFilesDir(null).toString() + "/" + split[1]
                    } else {
                        return context.getExternalFilesDir(null).toString() + "/"
                    }
                    // This is for checking SD Card
                } else {
                    return "storage" + "/" + docId.replace(":", "/")
                }
            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider
                val parcelFileDescriptor =
                    context.contentResolver.openFileDescriptor(uri, "r", null)
                parcelFileDescriptor?.let {
                    val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                    val file = File(
                        context.cacheDir,
                        context.contentResolver.getFileName(uri)
                    )
                    val outputStream = FileOutputStream(file)
                    IOUtils.copy(inputStream, outputStream)
                    return file.path
                }
            } else if (isMediaDocument(uri)) {
                val docId: String = DocumentsContract.getDocumentId(uri)
                val split: List<String> = docId.split(":")
                val type: String = split[0]
                return copyFileToInternalStorage(context, uri, "dwgviewer")

            }
        }//main if end
        else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // MediaStore (and general)
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment!!
            else copyFileToInternalStorage(
                context,
                uri,
                "dwgviewer"
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            // File
            return uri.path!!
        }
        return null!!
    }

    fun ContentResolver.getFileName(fileUri: Uri): String {
        var name = ""
        val returnCursor = this.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(
                OpenableColumns.DISPLAY_NAME
            )
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }
        return URLEncoder.encode(name, "utf-8")
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents".equals(uri.authority)
    }

    fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage".equals(uri.authority)
                || "com.google.android.apps.docs.storage.legacy".equals(uri.authority)
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents"
            .equals(uri.authority)
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents"
            .equals(uri.authority)
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content"
            .equals(uri.authority)
    }


    fun getDriveFilePath(context: Context, uri: Uri): String {
        val returnUri = uri
        val returnCursor: Cursor? = context.contentResolver.query(returnUri, null, null, null, null)

        val nameIndex: Int = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)

        returnCursor.moveToFirst()
        val name: String = (returnCursor.getString(nameIndex))
//        val size = Long.toString(returnCursor.getLong(sizeIndex))
        val file = File(
            context.cacheDir,
            URLEncoder.encode(name, "utf-8")
        )
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            val read: Int = 0
            val maxBufferSize: Int = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream!!.available()
            //int bufferSize = 1024;
            val bufferSize: Int =
                Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            inputStream.use { inputStream: InputStream ->
                outputStream.use { fileOut ->
                    while (true) {
                        val length = inputStream.read(buffers)
                        if (length <= 0)
                            break
                        fileOut.write(buffers, 0, length)
                    }
                    fileOut.flush()
                    fileOut.close()
                }
            }

            Log.e("File Size", "Size " + file.length())
            inputStream.close()
            Log.e("File Path", "Path " + file.path)
            Log.e("File Size", "Size " + file.length())
        } catch (e: Exception) {
            Log.e("Exception", e.message.toString())
        }
        return file.path
    }

    private fun copyFileToInternalStorage(mContext: Context, uri: Uri, newDirName: String): String {
        val returnCursor: Cursor? = mContext.contentResolver.query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
            ),
            null,
            null,
            null
        )

        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
        val output: File
        if (newDirName != "") {
            val dir = File(
                mContext.filesDir.toString()
                        + "/" + newDirName
            )
            if (!dir.exists()) {
                dir.mkdir()
            }
            output = File(
                mContext.filesDir.toString()
                        + "/" + newDirName + "/"
                        + URLEncoder.encode(name, "utf-8")
            )
        } else {
            output = File(
                mContext.filesDir.toString()
                        + "/" + URLEncoder.encode(name, "utf-8")
            )
        }
        try {
            val inputStream: InputStream? =
                mContext.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(output)
            var read = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while (inputStream!!.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: java.lang.Exception) {
            Log.e("Exception", e.message!!)
        }
        return output.path
    }

    fun checkPermission(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    fun getPermission(permission: String) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), REQ_CODE_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQ_CODE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectDwgFile()
        } else {
            Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}