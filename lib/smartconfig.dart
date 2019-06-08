import 'dart:async';

import 'package:flutter/services.dart';

class Smartconfig {
  static const MethodChannel _channel =
      const MethodChannel('smartconfig');

  static Future<String> start(String ssid, String pass) async {
    final String version = await _channel.invokeMethod('getPlatformVersion', {"ssid": ssid, "pass": pass});
    return version;
  }
}
