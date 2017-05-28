package com.vietpt.RCTCamera;

import android.content.Context;
import android.widget.FrameLayout;

public class RCTCameraView extends FrameLayout {
    private RCTCameraViewFinder mPreview;

    public RCTCameraView(Context context) {
        super(context);
        RCTCamera.createInstance();
        mPreview = new RCTCameraViewFinder(context);
        this.addView(mPreview);
    }

    public void onResume() {
        mPreview.startCamera(); // workaround for reload js
    }

    public void onPause() {
        mPreview.stopCamera();  // workaround for reload js
    }
}
