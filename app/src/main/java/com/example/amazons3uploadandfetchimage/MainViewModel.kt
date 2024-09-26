package com.example.amazons3uploadandfetchimage

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import java.io.File

class MainViewModel : ViewModel() {

    // LiveData for upload status and image URI
    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> = _uploadStatus

    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> = _imageUri

    // Initialize S3Client using AWS credentials
    private val s3Client: AmazonS3 by lazy {
        val credentials = BasicAWSCredentials(
            AWS_ACCESS_KEY_ID,
            AWS_SECRET_ACCESS_KEY
        )
        AmazonS3Client(credentials).apply {
            setRegion(Region.getRegion(Regions.AP_SOUTH_1))
        }
    }

    // Function to upload image to S3
    fun uploadImage(context: Context, file: File, bucketName: String) {
        try {
            // Build the TransferUtility instance using the passed context and S3 client
            val transferUtility = TransferUtility.builder()
                .context(context.applicationContext)  // Use application context for safety
                .s3Client(s3Client)
                .build()

            // Start the upload process
            val uploadObserver = transferUtility.upload(bucketName, file.name, file)

            uploadObserver.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    when (state) {
                        TransferState.COMPLETED -> {
                            Log.d("MainViewModel", "Upload completed: ${file.name}")
                            _uploadStatus.postValue("Upload Successful")
                        }

                        TransferState.FAILED -> {
                            _uploadStatus.postValue("Upload Failed")
                        }

                        else -> {}
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val progress = (bytesCurrent.toFloat() / bytesTotal.toFloat()) * 100
                    Log.d("MainViewModel", "Upload progress: $progress%")
                    _uploadStatus.postValue("Upload progress: $progress%")
                }

                override fun onError(id: Int, ex: Exception?) {
                    Log.e("MainViewModel", "Upload error: ${ex?.message}")
                    _uploadStatus.postValue("Upload Failed: ${ex?.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("MainViewModel", "Exception during upload: ${e.message}")
            _uploadStatus.postValue("Upload failed: ${e.message}")
        }
    }

    // Function to fetch image URL from S3
    fun fetchImage(fileName: String, bucketName: String) {
        try {
            // Log the filename and bucket name before fetching the URL
            Log.d("MainViewModel", "Fetching image with fileName: $fileName from bucket: $bucketName")

            // Get the image URL from S3 bucket
            val url = s3Client.getUrl(bucketName, fileName)
            Log.d("MainViewModel", "S3 Image download URL: ${url.toString()}")

            // Transform the URL to the desired format
            val transformedUrl = transformUrl(fileName)
            Log.d("MainViewModel", "Transformed URL: $transformedUrl")

            // Post the transformed URL to LiveData
            _imageUri.postValue(Uri.parse(transformedUrl))

        } catch (e: Exception) {
            Log.e("MainViewModel", "Error during download: ${e.message}")
            _uploadStatus.postValue("Fetch Failed: ${e.message}")
        }
    }

    // Function to transform the fileName to your CDN URL
    private fun transformUrl(fileName: String): String {
//        return "https://cdn.quality.healfate.in/$fileName"
        return "https://cdn.quality.healfate.in/Chat/$fileName"
    }

    // AWS credentials hardcoded (or use BuildConfig for security in production)
    companion object {
        const val AWS_ACCESS_KEY_ID: String = "use here your aws access key"
        const val AWS_SECRET_ACCESS_KEY: String = "use here your aws secret key"
        const val AWS_BUCKET_NAME: String = "use here your aws bucket name" // Add this line
    }
}


