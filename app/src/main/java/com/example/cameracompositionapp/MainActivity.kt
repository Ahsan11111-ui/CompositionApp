package com.example.cameracompositionapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var overlayView: OverlayView

    private var isFlashOn = false
    private var cameraControl: androidx.camera.core.CameraControl? = null
    private var currentZoom = 0.0f

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)

        val captureButton: Button = findViewById(R.id.captureButton)
        val zoomInButton: Button = findViewById(R.id.zoomInButton)
        val zoomOutButton: Button = findViewById(R.id.zoomOutButton)
        val flashButton: Button = findViewById(R.id.flashToggleButton)

        val centralButton: Button = findViewById(R.id.centralButton)
        val ruleOfThirdsButton: Button = findViewById(R.id.ruleOfThirdsButton)
        val diagonalButton: Button = findViewById(R.id.diagonalButton)
        val btnGoldenRatio: Button = findViewById(R.id.btnGoldenRatio)

        // Composition mode buttons
        centralButton.setOnClickListener {
            overlayView.setCompositionMode(OverlayView.CompositionMode.CENTRAL)
        }
        ruleOfThirdsButton.setOnClickListener {
            overlayView.setCompositionMode(OverlayView.CompositionMode.RULE_OF_THIRDS)
        }
        diagonalButton.setOnClickListener {
            overlayView.setCompositionMode(OverlayView.CompositionMode.DIAGONAL)
        }
        btnGoldenRatio.setOnClickListener {
            overlayView.setCompositionMode(OverlayView.CompositionMode.GOLDEN_RATIO)
        }


        // Zoom buttons
        zoomInButton.setOnClickListener {
            currentZoom += 0.1f
            if (currentZoom > 1.0f) currentZoom = 1.0f
            cameraControl?.setLinearZoom(currentZoom)
        }

        zoomOutButton.setOnClickListener {
            currentZoom -= 0.1f
            if (currentZoom < 0.0f) currentZoom = 0.0f
            cameraControl?.setLinearZoom(currentZoom)
        }

        // Flash toggle
        flashButton.setOnClickListener {
            isFlashOn = !isFlashOn
            flashButton.text = if (isFlashOn) "⚡ ON" else "⚡ OFF"
            imageCapture.flashMode = if (isFlashOn)
                ImageCapture.FLASH_MODE_ON
            else
                ImageCapture.FLASH_MODE_OFF
        }

        // Capture photo
        captureButton.setOnClickListener {
            takePhoto()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                cameraControl = camera.cameraControl
            } catch (exc: Exception) {
                exc.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val photoFile = File(
            externalMediaDirs.first(),
            "CameraApp-${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    exc.printStackTrace()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        baseContext,
                        "Photo saved: ${photoFile.absolutePath}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
