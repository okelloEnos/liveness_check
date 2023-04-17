package org.novanto.firebaselivenessdetection

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import id.privy.livenessfirebasesdk.LivenessApp
import id.privy.livenessfirebasesdk.entity.LivenessItem
import id.privy.livenessfirebasesdk.listener.PrivyCameraLivenessCallBackListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val livenessApp = LivenessApp.Builder(this)
            .setDebugMode(false)
            .setMotionInstruction("Look left", "Look left")
            .setSuccessText("Succeed! Please look at the camera again to take a photo")
            .setInstructions("Look at the camera and place the face on the green circle")
            .build()

        buttonStart.setOnClickListener {
            livenessApp.start(object : PrivyCameraLivenessCallBackListener {

                override fun success(livenessItem: LivenessItem?) {
                    if (livenessItem != null) {
                        test_image.setImageBitmap(livenessItem.imageBitmap)
                    }
                }

                override fun failed(t: Throwable?) {

                }

            })
        }
    }


}
