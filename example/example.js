import React from 'react';
import {
  Image,
  StyleSheet,
  TouchableOpacity,
  View,
} from 'react-native';
import Camera from 'react-native-camera-filter';

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  preview: {
    flex: 1,
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  overlay: {
    position: 'absolute',
    padding: 16,
    right: 0,
    left: 0,
    alignItems: 'center',
  },
  topOverlay: {
    top: 0,
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  bottomOverlay: {
    bottom: 0,
	flex: 1,
    flexDirection: 'row',
	justifyContent: 'center',
    alignItems: 'center',
  },
  typeButton: {
    padding: 5,
	left: 0,
  },
  buttonsSpace: {
    width: 89,
  },
  filterButton: {
    padding: 5,
	right: 0,
  },
});

export default class example extends React.Component {
  constructor(props) {
    super(props);
	
	this.camera = null;

    this.state = {
	  camera: {
	    cameraMode: 0,
	    cameraFilter: "auto",
      },
      isRecording: false
    };
  }
  
  switchFilter = () => {
    var newFilter;

    if (this.state.camera.cameraFilter === "auto") {
      newType = "daylight";
    } else if (this.state.camera.cameraFilter === "daylight") {
      newType = "fluorescent";
    } else if (this.state.camera.cameraFilter === "fluorescent") {
      newType = "incandescent";
    } else if (this.state.camera.cameraFilter === "incandescent") {
      newType = "auto";
    }

    this.setState({
      camera: {
        ...this.state.camera,
        cameraFilter: newType,
      },
    });
	
	if (this.camera) {
      this.camera.setFilter({cameraFilter: newType});
    }
  }

  get filterIcon() {
    let icon;

    if (this.state.camera.cameraFilter === "auto") {
      icon = require('./assets/ic_filter_auto.png');
    } else if (this.state.camera.cameraFilter === "daylight") {
      icon = require('./assets/ic_filter_daylight.png');
    } else if (this.state.camera.cameraFilter === "fluorescent") {
      icon = require('./assets/ic_filter_fluorescent.png');
    } else if (this.state.camera.cameraFilter === "incandescent") {
      icon = require('./assets/ic_filter_incandescent.png');
    }

    return icon;
  }
  
  switchMode = () => {
    var newMode;

    if (this.state.camera.cameraMode === 0) {
      newMode = 1;
    } else if (this.state.camera.cameraMode === 1) {
      newMode = 0;
    }

    this.setState({
      camera: {
        ...this.state.camera,
        cameraMode: newMode,
      },
    });
  }

  get modeIcon() {
    let icon;

    if (this.state.camera.cameraMode === 0) {
      icon = require('./assets/ic_photo.png');
    } else if (this.state.camera.cameraMode === 1) {
      icon = require('./assets/ic_video.png');
    }

    return icon;
  }
  
  takePicture = () => {
    if (this.camera) {
      this.camera.capture();
    }
  }
  
  startRecording = () => {
    if (this.camera) {
      this.camera.record();
      this.setState({
        isRecording: true
      });
    }
  }

  stopRecording = () => {
    if (this.camera) {
      this.camera.stopRecord();
      this.setState({
        isRecording: false
      });
    }
  }
  
  _renderMode() {
	if (this.state.camera.cameraMode === 0) {
		return (
			<TouchableOpacity
				style={[styles.overlay, styles.bottomOverlay]}
				onPress={this.takePicture}>
				<Image
					source={require('./assets/ic_take_photo.png')}
				/>
			</TouchableOpacity>
		);
	} else {
		return (
				  !this.state.isRecording
				  &&
				  <TouchableOpacity
					  style={[styles.overlay, styles.bottomOverlay]}
					  onPress={this.startRecording}
				  >
					<Image
						source={require('./assets/ic_take_video.png')}
					/>
				  </TouchableOpacity>
				  ||
				  <TouchableOpacity
					  style={[styles.overlay, styles.bottomOverlay]}
					  onPress={this.stopRecording}
				  >
					<Image
						source={require('./assets/ic_take_video_enable.png')}
					/>
				  </TouchableOpacity>
		);
	}
  }

  render() {
    return (
      <View style={styles.container}>
        <Camera
          ref={(cam) => {
            this.camera = cam;
          }}
          style={styles.preview}
        />
		
		<View style={[styles.overlay, styles.topOverlay]}>
			<TouchableOpacity
				style={styles.typeButton}
				onPress={this.switchMode}
			  >
				<Image
				  source={this.modeIcon}
				/>
			</TouchableOpacity>
			<TouchableOpacity
				style={styles.filterButton}
				onPress={this.switchFilter}
			  >
				<Image
				  source={this.filterIcon}
				/>
			</TouchableOpacity>
		</View>
		
		<View style={[styles.overlay, styles.bottomOverlay]}>
			<View style={styles.buttonsSpace}>
				{this._renderMode()}
			</View>
		</View>
          
      </View>
    );
  }
}
