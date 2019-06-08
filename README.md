# flutter_smartconfig

Quick network config for esp8266/esp32 modules~

This a basically a plain port of [react-native-smartconfig](https://github.com/tuanpmt/react-native-smartconfig) with latest esptouch native implements from [ESP Touch APP](https://github.com/EspressifApp)

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter, view our 
[online documentation](https://flutter.dev/docs), which offers tutorials, 
samples, guidance on mobile development, and a full API reference.

## Usage

Just put your ssid, bssid and pass word to `Smartconfig.start` and wait the async return.

```
void main() async {
  Smartconfig.start("Kittenbot", "78:44:fd:72:7e:68", "kittenbot123").then((onValue){
    print("sm version $onValue");  
  });
}
```

The bssid is necessary so you may want to use [connectivity plugin](https://github.com/flutter/plugins/tree/master/packages/connectivity)