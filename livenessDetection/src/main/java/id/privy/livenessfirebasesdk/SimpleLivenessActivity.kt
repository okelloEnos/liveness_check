package id.privy.livenessfirebasesdk

import android.Manifest
import android.arch.lifecycle.Observer
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import id.privy.livenessfirebasesdk.common.*
import id.privy.livenessfirebasesdk.entity.LivenessItem
import id.privy.livenessfirebasesdk.event.LivenessEventProvider
import id.privy.livenessfirebasesdk.listener.PrivyCameraLivenessCallBackListener
import id.privy.livenessfirebasesdk.vision.VisionDetectionProcessor
import id.privy.livenessfirebasesdk.vision.VisionDetectionProcessor.Motion
import kotlinx.android.synthetic.main.activity_simple_liveness.*
import java.io.IOException
import java.util.*


class SimpleLivenessActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName

    internal var preview: CameraSourcePreview? = null

    internal var graphicOverlay: GraphicOverlay? = null

    private var cameraSource: CameraSource? = null

    private var passedCameraSide: Boolean = false

    private var visionDetectionProcessor: VisionDetectionProcessor? = null

    private var success = false

    private lateinit var successText: String

    private var isDebug = false

    private lateinit var motionInstructions: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_liveness)

        preview = findViewById(R.id.cameraPreview)
        graphicOverlay = findViewById(R.id.faceOverlay)

        if (intent.extras != null) {
            val b = intent.extras!!
            successText = b.getString(Constant.Keys.SUCCESS_TEXT, getString(R.string.success_text))
            isDebug = b.getBoolean(Constant.Keys.IS_DEBUG, false)
            passedCameraSide = b.getBoolean("camera", false)
            instructions.text = b.getString(Constant.Keys.INSTRUCTION_TEXT, getString(R.string.instructions))
            motionInstructions = b.getStringArray(Constant.Keys.MOTION_INSTRUCTIONS)
        }

        if (PermissionUtil.with(this).isCameraPermissionGranted) {
            createCameraSource(passedCameraSide)
            startHeadShakeChallenge()
        }
        else {
            PermissionUtil.requestPermission(this, 1, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        }

//        switch_camera_button1.setOnClickListener {
//            passedCameraSide = !passedCameraSide
//            preview?.stop()
//            LivenessEventProvider.getEventLiveData().postValue(null)
//            cameraSource!!.release()
////            cameraSource = null
//            createCameraSource(passedCameraSide)
////            startHeadShakeChallenge()
////            startCameraSource()
//            Toast.makeText(this, "Camera Switch up : $passedCameraSide", Toast.LENGTH_LONG).show()
////            val livenessApp = LivenessApp.Builder(this)
////                .setDebugMode(false)
////                .setCameraPart(passedCameraSide)
////                .setMotionInstruction("Look left", "Look left")
////                .setSuccessText("Successfull! Please look at the camera again to take a photo")
////                .setInstructions("Look at the camera and place the face on the green circle")
////                .build()
//
////            livenessApp.start(object : PrivyCameraLivenessCallBackListener {
////
////                override fun success(livenessItem: LivenessItem?) {
////                    if (livenessItem != null) {
//////                        test_image.setImageBitmap(livenessItem.imageBitmap)
////                    }
////                }
////
////                override fun failed(t: Throwable?) {
////
////                }
////
////            })
//        }

        LivenessEventProvider.getEventLiveData().observe(this, Observer {
            it?.let {
                when {
                    it.getType() == LivenessEventProvider.LivenessEvent.Type.HeadShake -> {
                        onHeadShakeEvent()
                    }

                    it.getType() == LivenessEventProvider.LivenessEvent.Type.Default -> {
                        onDefaultEvent()
                    }
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        createCameraSource(passedCameraSide)
    }



    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        preview?.stop()
        LivenessEventProvider.getEventLiveData().postValue(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource!!.release()
    }
// 0 for back and 1 for front
    private fun createCameraSource(boolean: Boolean) {
        // If there's no existing cameraSource, create one.
    var cameraSide: Int = 0

    if (boolean) {
        cameraSide = 0
    } else {
        cameraSide = 1
    }

    // set
    (this.application as MyApplication).cameraFacingSide = cameraSide
    if (cameraSource == null) {
            cameraSource = CameraSource(this, graphicOverlay)
            cameraSource!!.setFacing(cameraSide)
//        startCameraSource()
        }

        val motion = Motion.values()[Random().nextInt(Motion.values().size)]

        when (motion) {
            Motion.Left -> {
                motionInstruction.text = this.motionInstructions[0]
            }

            Motion.Right -> {
                motionInstruction.text = this.motionInstructions[1]
            }
//
//            Motion.Up -> {
//                Toast.makeText(this, "Look Up", Toast.LENGTH_SHORT).show()
//            }
//
//            Motion.Down -> {
//                Toast.makeText(this, "Look Down", Toast.LENGTH_SHORT).show()
//            }
        }

        visionDetectionProcessor = VisionDetectionProcessor()
        visionDetectionProcessor!!.isSimpleLiveness(true, this, motion)
        visionDetectionProcessor!!.isDebugMode(isDebug)

        cameraSource!!.setMachineLearningFrameProcessor(visionDetectionProcessor)
    }

    private fun startCameraSource() {

        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null")
                }
                preview!!.start(cameraSource, graphicOverlay)
                Handler().postDelayed({
                                      cameraSource!!.release()
                    LivenessApp.setCameraResultData(null)
                    finish()
                }, 10000)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }

    fun navigateBack(success: Boolean, bitmap: Bitmap?) {
        if (bitmap != null) {
            if (success) {
                // get
                val s: Int = (this.application as MyApplication).cameraFacingSide
                LivenessApp.setCameraResultData(BitmapUtils.processBitmap(bitmap, s))
                finish()
            }
            else {
                LivenessApp.setCameraResultData(null)
                finish()
            }
        }
    }

    private fun startHeadShakeChallenge() {
        visionDetectionProcessor!!.setVerificationStep(1)
    }

    private fun onHeadShakeEvent() {
        if (!success) {
            success = true
            motionInstruction.text = successText

            visionDetectionProcessor!!.setChallengeDone(true)
        }
    }

    private fun onDefaultEvent() {
        if (success) {
            Handler().postDelayed({
                cameraSource!!.takePicture(null, com.google.android.gms.vision.CameraSource.PictureCallback {
                    navigateBack(true, BitmapFactory.decodeByteArray(it, 0, it.size))
                })
            }, 200)
        }
    }

}
