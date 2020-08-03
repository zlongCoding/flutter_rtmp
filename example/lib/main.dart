import 'package:flutter/material.dart';
import 'dart:async';
//
// import 'package:flutter_rtmp/flutter_rtmp.dart';

import 'home.dart';
import 'utils/PhonePermissionUtils.dart';

void main() {
  // if (Platform.isAndroid) {
  //   SystemUiOverlayStyle systemUiOverlayStyle = SystemUiOverlayStyle(
  //     statusBarColor: Colors.transparent, //设置为透明
  //   );
  //   SystemChrome.setSyst emUIOverlayStyle(systemUiOverlayStyle);
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    // PhonePermissionUtils.checkPermission;
    PhonePermissionUtils.checkPermission().then((onValue) {});
    return MaterialApp(
      // debugShowCheckedModeBanner: false,
      home: HomePage(),
    );
  }
}
