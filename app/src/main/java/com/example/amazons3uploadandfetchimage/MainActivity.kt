package com.example.amazons3uploadandfetchimage
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.amazons3uploadandfetchimage.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var uploadedFileName: String? = null // Store the uploaded file name

    private val CAMERA_PERMISSION_CODE = 1001
    private val IMAGE_PICK_CODE = 1000

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadImageFromUri(it) }
    }

    private val getCameraImage = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { uploadImageFromBitmap(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Handle button clicks
        binding.btnUpload.setOnClickListener { checkCameraPermissionAndShowDialog() }
        binding.btnFetch.setOnClickListener {
            uploadedFileName?.let {
//                viewModel.fetchImage(it, "qty-cdn.healfate.in/Chat")
                viewModel.fetchImage(it, "qty-cdn.healfate.in/Chat")
            } ?: run {
                Toast.makeText(this, "No image uploaded yet!", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe image URI to update the UI
        viewModel.imageUri.observe(this) { uri ->
            uri?.let {
                Glide.with(this).load(it).into(binding.imageView)
            }
        }

        // Observe upload status to update the UI
        viewModel.uploadStatus.observe(this) { status ->
            binding.tvStatus.text = status
        }
    }

    // Check for camera permission and show dialog
    private fun checkCameraPermissionAndShowDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            showImageSourceDialog() // Permission granted, proceed
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE
            )
        }
    }

    // Show a dialog to select between Camera and Gallery
    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> getCameraImage.launch(null) // Camera selected
                    1 -> getImage.launch("image/*") // Gallery selected
                }
            }.show()
    }

    // Handle image upload from Bitmap (camera)
    private fun uploadImageFromBitmap(bitmap: Bitmap) {
        val file = saveBitmapToFile(bitmap)
        if (file != null) {
            uploadedFileName = file.name // Store the file name
//            viewModel.uploadImage(this, file, "qty-cdn.healfate.in/Chat")
            viewModel.uploadImage(this, file, "${MainViewModel.AWS_BUCKET_NAME}/Chat")
        }
    }

    // Handle image upload from URI (gallery)
    private fun uploadImageFromUri(uri: Uri) {
        val file = FileUtils.getFileFromUri(this, uri) ?: return
        uploadedFileName = file.name // Store the file name
//        viewModel.uploadImage(this, file, "qty-cdn.healfate.in/Chat")
        viewModel.uploadImage(this, file, "${MainViewModel.AWS_BUCKET_NAME}/Chat")

    }

    // Save the Bitmap (camera image) to a file
    private fun saveBitmapToFile(bitmap: Bitmap): File? {
        return try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "camera_image.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog() // Permission granted, proceed with camera
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show()

                // Open app settings if the permission is denied
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    // Handle gallery image selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let { uri ->
                val filePath = getRealPathFromURI(this, uri)
                if (filePath != null) {
                    val fileName = File(filePath).name
                    uploadedFileName = fileName // Store the file name
//                    viewModel.fetchImage(fileName, "qty-cdn.healfate.in/Chat")
                    // Use the AWS_BUCKET_NAME constant with the "Chat" path
                    viewModel.fetchImage(fileName, "${MainViewModel.AWS_BUCKET_NAME}/Chat")
                } else {
                    Log.e("FilePathError", "Failed to get file path")
                }
            }
        }
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var result: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri, projection, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                result = it.getString(columnIndex)
            }
        }
        return result
    }

}





