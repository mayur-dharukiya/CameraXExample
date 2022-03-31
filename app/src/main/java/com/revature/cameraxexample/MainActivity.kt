package com.revature.cameraxexample

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.camera2.internal.annotation.CameraExecutor
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.revature.cameraxexample.MainActivity.Companion.outputDirectory
import com.revature.cameraxexample.ui.theme.CameraXExampleTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors.newSingleThreadExecutor

class MainActivity : ComponentActivity() {

    private var imageCapture: ImageCapture?=null



    private lateinit var cameraExecutor: ExecutorService

    companion object
    {
        private val REQUIRED_PERMISSIONS=arrayOf(Manifest.permission.CAMERA)

        private val REQUEST_CODE_PERMISSION=10

        val TAG="CAMERA"

        val FILENAME_FORMAT="yyyy-MM-dd-HH-mm-ss-SSS"

        lateinit var outputDirectory: File

        //val context= LocalContext
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode== REQUEST_CODE_PERMISSION)
        {
            if(allPermissionsGranted())
            {
                startCamera()
            }
            else
            {
                Toast.makeText(this,"Permission not granted by user",Toast.LENGTH_LONG).show()
                finish()
            }
        }
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

        outputDirectory=getOutputDirectory1()
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

     val cameraProviderFuture=ProcessCameraProvider.getInstance((this))

        cameraProviderFuture.addListener(Runnable {

            val cameraProvider:ProcessCameraProvider=cameraProviderFuture.get()

            //preview

            val previewView = PreviewView(this).apply {
                this.scaleType = scaleType
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }


            val preview= Preview.Builder().build().also{

                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture=ImageCapture.Builder().build()

            val cameraSelector=CameraSelector.DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture)
            }catch(ex:Exception)
            {
                Log.d(TAG,"USE CASE BINDING FAILED",ex)
            }
        },ContextCompat.getMainExecutor(this)   )
    }

    private fun getOutputDirectory1(): File {

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

    override fun onDestroy()
    {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

//     fun takePhoto(){
//
//         val imageCapture: ImageCapture?=null
//
//        //create a file to hold the image
//
//        val photoFile=File(outputDirectory, SimpleDateFormat(FILENAME_FORMAT,Locale.US).format(System.currentTimeMillis() )+".jpg")
//
//        val outputOptions=ImageCapture.OutputFileOptions.Builder(photoFile).build()
//
//        imageCapture?.takePicture(outputOptions,ContextCompat.getMainExecutor(this),object:ImageCapture.OnImageSavedCallback
//        {
//
//            override fun onError(exception:ImageCaptureException)
//            {
//                Log.d(TAG,"photo capture failed:${exception.message}")
//            }
//
//            override fun onImageSaved(output:ImageCapture.OutputFileResults)
//            {
//
//                val savedFile= Uri.fromFile(photoFile)
//                val msg="photo captured successfully:$savedFile"
//
//                Toast.makeText(baseContext,msg,Toast.LENGTH_LONG).show()
//                Log.d(TAG,msg)
//            }
//        })
//
//
//    }

}

fun takePhoto(){

    val imageCapture: ImageCapture?=null
    val context:Context?=null

    //create a file to hold the image

    val photoFile=File(outputDirectory, SimpleDateFormat(MainActivity.FILENAME_FORMAT,Locale.US).format(System.currentTimeMillis() )+".jpg")

    val outputOptions=ImageCapture.OutputFileOptions.Builder(photoFile).build()

    context?.let { ContextCompat.getMainExecutor(it) }?.let {
        imageCapture?.takePicture(outputOptions,
            it,object:ImageCapture.OnImageSavedCallback {

                override fun onError(exception:ImageCaptureException) {
                    Log.d(MainActivity.TAG,"photo capture failed:${exception.message}")
                }

                override fun onImageSaved(output:ImageCapture.OutputFileResults) {

                    val savedFile= Uri.fromFile(photoFile)
                    val msg="photo captured successfully:$savedFile"

                    Toast.makeText(context,msg,Toast.LENGTH_LONG).show()
                    Log.d(MainActivity.TAG,msg)
                }
            })
    }


}

@Composable
fun UI()
{

    Column(modifier=Modifier.fillMaxWidth()) {

        Button(onClick = { takePhoto() }) {


            Text(text = "Capture")
        }

    }

}



