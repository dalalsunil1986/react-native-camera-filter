# react-native-camera-filter
This is simple demo for android camera with filter component in react native

### Installation
Download project and put ```react-native-camera-filter``` folder in ```node_modules``` folder

Add permission to ```AndroidManifest.xml```
```bash
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

Add library to ```settings.gradle```
```bash
include ':react-native-camera-filter'
project(':react-native-camera-filter').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-camera-filter/android')
```

compile project in ```build.gradle```
```bash
compile project(':react-native-camera-filter')
```

Import library and add package to ```MainApplication.java```
```bash
import com.vietpt.RCTCamera.RCTCameraPackage;
```
```bash
@Override
protected List<ReactPackage> getPackages() {
  return Arrays.<ReactPackage>asList(
      new MainReactPackage(),
      new RCTCameraPackage()
  );
}
```

Import Camera class into ```index.js```
```bash
import Camera from 'react-native-camera-filter';
```

<i>You can see example in ```example.js``` of example project</i>

### Run example 
Download project and open example folder

Run below command
```bash
npm install
react-native start
react-native run-android
```

### Running On Device

There is apk demo file in apk folder

## License

MIT

