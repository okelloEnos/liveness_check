package org.novanto.firebaselivenessdetection

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import id.privy.livenessfirebasesdk.LivenessApp
import id.privy.livenessfirebasesdk.entity.LivenessItem
import id.privy.livenessfirebasesdk.listener.PrivyCameraLivenessCallBackListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
var cameraSide: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//switch_camera_button.setOnClickListener {
//            cameraSide = !cameraSide
//
//}

        buttonStart.setOnClickListener {

            val livenessApp = LivenessApp.Builder(this)
                .setDebugMode(false)
                .setCameraPart(cameraSide)
                .setMotionInstruction("Look left", "Look left")
                .setSuccessText("Successfull! Please look at the camera again to take a photo")
                .setInstructions("Look at the camera and place the face on the green circle")
                .build()

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_item, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.switch_camera_button) {
            cameraSide = !cameraSide
        }
        return super.onOptionsItemSelected(item)
    }
}
