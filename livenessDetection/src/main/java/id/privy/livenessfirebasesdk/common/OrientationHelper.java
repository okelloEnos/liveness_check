package id.privy.livenessfirebasesdk.common;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationHelper implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private Camera.CameraInfo cameraInfo;

    private int deviceOrientationDegrees  = 0;

    public OrientationHelper(SensorManager sensorManager, Camera.CameraInfo cameraInfo) {
        this.sensorManager = sensorManager;
        this.cameraInfo = cameraInfo;
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void start() {
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public int getCurrentOrientation() {
        return deviceOrientationDegrees ;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] rotationMatrix = new float[9];
        float[] orientationValues = new float[3];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientationValues);
        deviceOrientationDegrees = (int) Math.toDegrees(orientationValues[0]);
        // do something with the device orientation
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    private int getCameraOrientationDegrees() {
        int cameraOrientation;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraOrientation = cameraInfo.orientation;
        } else {
            cameraOrientation = (cameraInfo.orientation + 180) % 360;
        }
        return cameraOrientation;
    }
}
