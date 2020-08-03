import 'package:flutter/material.dart';

class CheckBoxWidget extends StatefulWidget {
  @override
  createState() => new _CheckBox();
}

// ignore: must_be_immutable
class _CheckBox extends State<CheckBoxWidget> {
  // Logo({this.size: 100.0});
  bool _value = false;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: InkWell(
        onTap: () {
          setState(() {
            _value = !_value;
          });
        },
        child: Container(
          // decoration: BoxDecoration(
          //   image: DecorationImage(
          //     image: _value
          //         ? AssetImage("images/taobao.png")
          //         : AssetImage("images/weixin.png"),
          //     fit: BoxFit.cover,
          //   ),
          // ),
          decoration:
              BoxDecoration(shape: BoxShape.circle, color: Colors.transparent),
          child: Padding(
            padding: EdgeInsets.all(0),
            child: _value
                ? new Image.asset("images/taobao.png")
                : new Image.asset("images/weixin.png"),
          ),
        ),
      ),
    );
  }
}
