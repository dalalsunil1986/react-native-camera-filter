package com.vietpt.RCTCamera;

import android.hardware.Camera;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RCTCamera {
    private static RCTCamera ourInstance;
    private final HashMap<Integer, CameraInfoWrapper> _cameraInfos;
    private final HashMap<Integer, Integer> _cameraTypeToIndex;
    private final Map<Number, Camera> _cameras;

    public static RCTCamera getInstance() {
        return ourInstance;
    }
    public static void createInstance() {
        ourInstance = new RCTCamera();
    }


    public synchronized Camera acquireCameraInstance(int type) {
        if (null == _cameras.get(type) && null != _cameraTypeToIndex.get(type)) {
            try {
                Camera camera = Camera.open(_cameraTypeToIndex.get(type));
                _cameras.put(type, camera);
                adjustPreviewLayout(type);
            } catch (Exception e) {
                Log.e("RCTCamera", "acquireCameraInstance failed", e);
            }
        }
        return _cameras.get(type);
    }

    public void releaseCameraInstance(int type) {
        // Release seems async and creates race conditions. Remove from map first before releasing.
        Camera releasingCamera = _cameras.get(type);
        if (null != releasingCamera) {
            _cameras.remove(type);
            releasingCamera.release();
        }
    }

    public Camera.Size getBestSize(List<Camera.Size> supportedSizes, int maxWidth, int maxHeight) {
        Camera.Size bestSize = null;
        for (Camera.Size size : supportedSizes) {
            if (size.width > maxWidth || size.height > maxHeight) {
                continue;
            }

            if (bestSize == null) {
                bestSize = size;
                continue;
            }

            int resultArea = bestSize.width * bestSize.height;
            int newArea = size.width * size.height;

            if (newArea > resultArea) {
                bestSize = size;
            }
        }

        return bestSize;
    }

    public void adjustPreviewLayout(int type) {
        Camera camera = _cameras.get(type);
        if (null == camera) {
            return;
        }

        CameraInfoWrapper cameraInfo = _cameraInfos.get(type);
        int displayRotation;
        int rotation;
        if (cameraInfo.info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = 270;
            displayRotation = 270;
        } else {
            rotation = 90;
            displayRotation = rotation;
        }
        cameraInfo.rotation = rotation;
        // TODO: take in account the _orientation prop

        camera.setDisplayOrientation(displayRotation);

        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(cameraInfo.rotation);

        // set preview size
        // defaults to highest resolution available
        Camera.Size optimalPreviewSize = getBestSize(parameters.getSupportedPreviewSizes(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        int width = optimalPreviewSize.width;
        int height = optimalPreviewSize.height;

        parameters.setPreviewSize(width, height);
        try {
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cameraInfo.rotation == 0 || cameraInfo.rotation == 180) {
            cameraInfo.previewWidth = width;
            cameraInfo.previewHeight = height;
        } else {
            cameraInfo.previewWidth = height;
            cameraInfo.previewHeight = width;
        }
    }

    private RCTCamera() {
        _cameras = new HashMap<>();
        _cameraInfos = new HashMap<>();
        _cameraTypeToIndex = new HashMap<>();

        // map camera types to camera indexes and collect cameras properties
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && _cameraInfos.get(1) == null) {
                _cameraInfos.put(1, new CameraInfoWrapper(info));
                _cameraTypeToIndex.put(1, i);
                acquireCameraInstance(1);
                releaseCameraInstance(1);
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK && _cameraInfos.get(2) == null) {
                _cameraInfos.put(2, new CameraInfoWrapper(info));
                _cameraTypeToIndex.put(2, i);
                acquireCameraInstance(2);
                releaseCameraInstance(2);
            }
        }
    }

    private class CameraInfoWrapper {
        public final Camera.CameraInfo info;
        public int rotation = 0;
        public int previewWidth = -1;
        public int previewHeight = -1;

        public CameraInfoWrapper(Camera.CameraInfo info) {
            this.info = info;
        }
    }
}
