import 'package:flutter/material.dart';

class Logo extends StatelessWidget {
  final double size;

  const Logo({Key key, this.size: 100.0}) : super(key: key);

  // Logo({this.size: 100.0});

  @override
  Widget build(BuildContext context) {
    return Image.asset('assets/images/logo_light.png', width: size);
  }
}
