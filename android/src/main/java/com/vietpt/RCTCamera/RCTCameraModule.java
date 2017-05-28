package com.vietpt.RCTCamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaActionSound;
import android.media.MediaRecorder;
import android.os.AsyncTask;;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RCTCameraModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static ReactApplicationContext _reactContext;

    public static final int RCT_CAMERA_TAKE_PICTURE = 0;
    public static final int RCT_CAMERA_RECORD_VIDEO = 1;
    public static final String RCT_CAMERA_FILTER_AUTO = Camera.Parameters.WHITE_BALANCE_AUTO;
    public static final String RCT_CAMERA_FILTER_DAYLIGHT = Camera.Parameters.WHITE_BALANCE_DAYLIGHT;
    public static final String RCT_CAMERA_FILTER_FLUORESCENT = Camera.Parameters.WHITE_BALANCE_FLUORESCENT;
    public static final String RCT_CAMERA_FILTER_INCANDESCENT = Camera.Parameters.WHITE_BALANCE_INCANDESCENT;

    private MediaRecorder mMediaRecorder;
    private MediaActionSound sound;
    private Camera mCamera = null;
    private boolean mRecording = false;

    public RCTCameraModule(ReactApplicationContext reactContext) {
        super(reactContext);
        sound = new MediaActionSound();
        _reactContext = reactContext;
        _reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "RCTCameraModule";
    }

    @Override
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("CameraMode", getCameraMode());
                put("CameraFilter", getCameraFilter());
            }

            private Map<String, Object> getCameraMode() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put(Camera.Parameters.WHITE_BALANCE_AUTO, RCT_CAMERA_FILTER_AUTO);
                        put(Camera.Parameters.WHITE_BALANCE_DAYLIGHT, RCT_CAMERA_FILTER_DAYLIGHT);
                        put(Camera.Parameters.WHITE_BALANCE_FLUORESCENT, RCT_CAMERA_FILTER_FLUORESCENT);
                        put(Camera.Parameters.WHITE_BALANCE_INCANDESCENT, RCT_CAMERA_FILTER_INCANDESCENT);
                    }
                });
            }

            private Map<String, Object> getCameraFilter() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("picture", RCT_CAMERA_TAKE_PICTURE);
                        put("video", RCT_CAMERA_RECORD_VIDEO);
                    }
                });
            }
        });
    }

    @ReactMethod
    private void record() {
        mCamera = RCTCamera.getInstance().acquireCameraInstance(2);
        if (mCamera == null) {
            return;
        }

        if (!prepareMediaRecorder()) {
            return;
        }

        try {
            mRecording = true;
            mMediaRecorder.start();
        } catch (Exception ex) {}
    }

    @ReactMethod
    private void stopRecord() {
        mRecording = false;
        mMediaRecorder.stop(); // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
    }

    @ReactMethod
    private void changeCameraFilter(ReadableMap options) {
        mCamera = RCTCamera.getInstance().acquireCameraInstance(2);
        String tag = options.getString("cameraFilter");
        Camera.Parameters parameters = mCamera.getParameters();
        if(tag.equals("daylight")) {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
        } else if(tag.equals("fluorescent")) {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_FLUORESCENT);
        } else if(tag.equals("incandescent")) {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
        } else if(tag.equals("auto")) {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        }
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    @ReactMethod
    private void capture() {
        mCamera = RCTCamera.getInstance().acquireCameraInstance(2);
        if(mCamera != null) {
            sound.play(MediaActionSound.SHUTTER_CLICK);
            mCamera.takePicture(null, null, mPictureCallback);
        }
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            mCamera.startPreview();
            String picture = RCTCameraUtils.getStoragePath(RCTCameraUtils.getTimeStampFile("jpg"));
            if (picture == null) {
                // The path to save picture has error
                return;
            }

            int rotate = 90;
            if (findBackFacingCamera() < 0) {
                rotate = 270;
            }

            File pictureFile = new File(picture);
            new SavePhotoAsynctask(data, pictureFile, rotate).execute();
        }
    };

    private class SavePhotoAsynctask extends AsyncTask<Object, Void, String> {
        byte[] data;
        File pictureFile;
        int rotate;

        public SavePhotoAsynctask(byte[] data, File pictureFile, int rotate) {
            this.data = data;
            this.pictureFile = pictureFile;
            this.rotate = rotate;
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                Bitmap temp = RCTCameraUtils.rotatePhoto(bitmap, rotate);

                temp.compress(Bitmap.CompressFormat.JPEG, 90, new FileOutputStream(pictureFile));
            } catch(Exception ex) {}

            return null;
        }
    }

    private boolean prepareMediaRecorder() {
        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOrientationHint(90);
        if(findBackFacingCamera() < 0) {
            mMediaRecorder.setOrientationHint(270);
        }

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        mMediaRecorder.setOutputFile(RCTCameraUtils.getStoragePath(RCTCameraUtils.getTimeStampFile("mp4")));

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset(); // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    @Override
    public void onHostResume() {}

    @Override
    public void onHostPause() {
        if (mRecording && mCamera != null) {
            releaseMediaRecorder();
        }
    }

    @Override
    public void onHostDestroy() {}
}
