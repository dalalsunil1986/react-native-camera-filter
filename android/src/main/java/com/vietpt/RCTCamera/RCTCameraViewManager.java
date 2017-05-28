package com.vietpt.RCTCamera;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

public class RCTCameraViewManager extends SimpleViewManager<RCTCameraView> {
    public static final String REACT_CLASS = "RCTCamera";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected RCTCameraView createViewInstance(ThemedReactContext reactContext) {
        return new RCTCameraView(reactContext);
    }
}
