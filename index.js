import React, { Component, PropTypes } from 'react';
import {
  DeviceEventEmitter, // android
  NativeAppEventEmitter, // ios
  NativeModules,
  Platform,
  StyleSheet,
  requireNativeComponent,
  View,
} from 'react-native';

const CameraManager = NativeModules.CameraManager || NativeModules.CameraModule;
const CAMERA_REF = 'camera';

function convertNativeProps(props) {
  const newProps = { ...props };
  if (typeof props.cameraMode === 'string') {
    newProps.cameraMode = Camera.constants.CameraMode[props.cameraMode];
  }

  if (typeof props.cameraFilter === 'string') {
    newProps.cameraFilter = Camera.constants.CameraFilter[props.cameraFilter];
  }

  return newProps;
}

export default class Camera extends Component {

  static constants = {
    CameraMode: CameraManager.CameraMode,
    CameraFilter: CameraManager.CameraFilter
  };

  static propTypes = {
    ...View.propTypes,
    cameraMode: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ]),
    cameraFilter: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number
    ])
  };

  static defaultProps = {
    cameraMode: CameraManager.CameraMode.picture,
    cameraFilter: CameraManager.CameraFilter.auto
  };

  static checkDeviceAuthorizationStatus = CameraManager.checkDeviceAuthorizationStatus;
  static checkVideoAuthorizationStatus = CameraManager.checkVideoAuthorizationStatus;
  static checkAudioAuthorizationStatus = CameraManager.checkAudioAuthorizationStatus;

  setNativeProps(props) {
    this.refs[CAMERA_REF].setNativeProps(props);
  }

  constructor() {
    super();
    this.state = {
      isAuthorized: false,
      isRecording: false
    };
  }

  async componentWillMount() {
    let check = Camera.checkDeviceAuthorizationStatus && Camera.checkVideoAuthorizationStatus;

    if (check) {
      const isAuthorized = await check();
      this.setState({ isAuthorized });
    }
  }

  render() {
    const style = [styles.base, this.props.style];
    const nativeProps = convertNativeProps(this.props);

    return <RCTCamera ref={CAMERA_REF} {...nativeProps} />;
  }

  capture() {
    return CameraManager.capture();
  }

  setFilter(options) {
	return CameraManager.changeCameraFilter(options);
  }
  
  record() {
    return CameraManager.record();
  }
  
  stopRecord() {
    return CameraManager.stopRecord();
  }
}

const RCTCamera = requireNativeComponent('RCTCamera', Camera);

const styles = StyleSheet.create({
  base: {},
});
