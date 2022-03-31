package com.revature.cameraxexample

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.camera2.internal.annotation.CameraExecutor
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.revature.cameraxexample.ui.theme.CameraXExampleTheme
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.*
import java.util.concurrent.Executors.newSingleThreadExecutor

class MainActivity : ComponentActivity() {

    private var imageCapture: ImageCapture?=null

    private lateinit var outputDirectory: File

    private lateinit var cameraExecutor: CameraExecutor

    companion object
    {
        private val REQUIRED_PERMISSIONS=arrayOf(Manifest.permission.CAMERA)

        private val REQUEST_CODE_PERMISSION=10

        private val TAG="CAMERA"

        private val FILENAME_FORMAT="yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(allPermissionsGranted())
        {
            startCamera()
        }else
        {

            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION)

        }

        outputDirectory=getOutputDirectory()
        cameraExecutor= Executors.newSingleThreadExecutor()

        setContent {
            CameraXExampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                        UI()
                }
            }
        }
    }

    private fun startCamera() {

    }

    private fun getOutputDirectory(): File {

        val mediaDir=externalMediaDirs.firstOrNull()?.let {

            File(it,"Camerax App").apply {

                mkdir()
            }
        }

        return if(mediaDir!=null && mediaDir.exists())
            mediaDir else filesDir


    }

    private fun allPermissionsGranted()= Companion.REQUIRED_PERMISSIONS.all{

         ContextCompat.checkSelfPermission(baseContext,it)==PackageManager.PERMISSION_GRANTED


    }
}

@Composable
fun UI()
{

    Column(modifier=Modifier.fillMaxWidth()) {

        Button(onClick = { }) {


            Text(text = "Capture")
        }

    }

}



