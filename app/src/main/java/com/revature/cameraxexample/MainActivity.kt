package com.revature.cameraxexample

import android.Manifest
import android.content.pm.PackageManager


import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.Button
import android.widget.Toast
import androidx.camera.camera2.internal.annotation.CameraExecutor
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var imageCapture:ImageCapture?=null

    private lateinit var outputDirectory:File

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var btnCapture:Button

    private  lateinit var viewFinder:PreviewView

    companion object{

        private val REQUIRED_PERMISSIONS=arrayOf(Manifest.permission.CAMERA)

        private val REQUEST_CODE_PERMISSIONS=10

        private val TAG="CameraX"

        private val FILENAME_FORMAT="yyyy-MM-dd-HH-mm-ss-SSS"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCapture=findViewById(R.id.camera_capture_button)
        viewFinder=findViewById(R.id.viewFinder)


        //request permissions

        if(allPermissionGranted())
        {
            startCamera()

        }
        else
        {
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS)
        }

        //set the listener for the button

        btnCapture.setOnClickListener{ takePhoto()}

        outputDirectory=getOutputDirectory()

        cameraExecutor= Executors.newSingleThreadExecutor()


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode== REQUEST_CODE_PERMISSIONS)
        {
            if(allPermissionGranted())
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

    private fun allPermissionGranted()= REQUIRED_PERMISSIONS.all{

        ContextCompat.checkSelfPermission(baseContext,it)==PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {


        val cameraProviderFuture=ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {

            val cameraProvider:ProcessCameraProvider=cameraProviderFuture.get()

            //preview
            val preview=Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(viewFinder.surfaceProvider) }

            imageCapture=ImageCapture.Builder().build()


            val cameraSelector=CameraSelector.DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture)

            }catch (exc: Exception)
            {
                Log.e(TAG,"use case binding failed",exc)
            }

        },ContextCompat.getMainExecutor(this)   )


    }

    private fun getOutputDirectory(): File {

        val mediaDir=externalMediaDirs.firstOrNull()?.let {

            File(it,resources.getString(R.string.app_name)).apply {

                mkdirs()
            }
        }

        return if(mediaDir!=null && mediaDir.exists())
            mediaDir else filesDir

    }

    private fun takePhoto() {

        val imageCapture=imageCapture ?: return

        //create a file to hold the image

        val photoFile=File(outputDirectory,SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())+".jpg")

        //create output options object which contains File+metadata

        val outputOptions=ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions,ContextCompat.getMainExecutor(this),object:ImageCapture.OnImageSavedCallback
        {
            override fun onError(exception: ImageCaptureException) {

                Log.e(TAG,"Photo Capure Failed:${exception.message}")
            }

            override fun onImageSaved(output:ImageCapture.OutputFileResults)
            {
                val savedFile= Uri.fromFile(photoFile)
                val msg="Photo captured successfully: $savedFile"

                Toast.makeText(baseContext,msg,Toast.LENGTH_LONG).show()
                Log.d(TAG,msg)
            }
        })

    }

    override fun onDestroy()
    {
        super.onDestroy()
        cameraExecutor.shutdown()
    }






















}