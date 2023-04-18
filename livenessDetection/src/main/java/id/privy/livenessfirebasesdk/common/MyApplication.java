package id.privy.livenessfirebasesdk.common;

import android.app.Application;

public class MyApplication extends Application {

    private int facing;

    public int getCameraFacingSide() {
        return facing;
    }

    public void setCameraFacingSide(int facing) {
        this.facing = facing;
    }
}