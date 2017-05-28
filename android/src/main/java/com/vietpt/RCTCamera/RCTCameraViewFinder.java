package com.vietpt.RCTCamera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class RCTCameraViewFinder extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Size mPreviewSize;

    public RCTCameraViewFinder(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        startCamera();
    }

    public void startCamera() {
        mCamera = RCTCamera.getInstance().acquireCameraInstance(2);
        startCameraPreview();
    }

    public void stopCamera() {
        stopCameraPreview();
        mCamera.release();
    }

    public void startCameraPreview() {
        if(mCamera != null) {
            try {
                getHolder().addCallback(this);
                mCamera.setPreviewDisplay(getHolder());
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
            } catch (Exception e) {}
        }
    }

    public void stopCameraPreview() {
        if(mCamera != null) {
            try {
                getHolder().removeCallback(this);
                mCamera.cancelAutoFocus();
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
            } catch(Exception e) {}
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // create the surface and start camera preview
            if (mCamera == null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        mCamera = camera;
        try {
            Camera.Parameters parameters = mCamera.getParameters();

            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);

            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {}
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        refreshCamera(mCamera);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.release();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        mPreviewSize = mCamera.getParameters().getPreferredPreviewSizeForVideo();
    }
}
