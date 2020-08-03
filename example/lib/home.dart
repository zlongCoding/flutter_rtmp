import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_plugin_record/flutter_plugin_record.dart';
import 'package:flutter_rtmp/flutter_rtmp.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:wakelock/wakelock.dart';
import 'package:socket_io_client/socket_io_client.dart' as IO;

class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  RtmpManager _manager;
  bool isStreaming = false;
  String streamUrl;
  FlutterPluginRecord recordPlugin;
  IO.Socket socket;

  @override
  void initState() {
    super.initState();
    _manager = RtmpManager();
    Wakelock.enable();
  }

  @override
  void dispose() {
    super.dispose();
    _manager.dispose();
    setState(() {
      isStreaming = false;
    });
    Wakelock.disable();
  }

  getHttp() async {
    try {
      streamUrl = "rtmp://192.168.3.114:1935/live/aaaaa";
      // final response = await Dio().get("http://lo.3tilabs.com/live/open-live");
      // if (response.data["code"] == 200) {
      //   streamUrl = response.data["data"];
      //   this.startVideoStream();
      // }
    } catch (e) {
      print(e);
    }
  }

  startVideoStream() async {
    await _manager.startLive(streamUrl);
    setState(() {
      isStreaming = true;
    });
  }

  // 停止直播
  stopVideoStream() async {
    await _manager.stopLive();
    setState(() {
      isStreaming = false;
    });
  }

  switchVideoCamera() async {
    await _manager.switchCamera();
  }

  @override
  Widget build(Object context) {
    // TODO: implement build
    ScreenUtil.init(context, width: 760, height: 1650, allowFontScaling: false);
    return Scaffold(
        body: Center(
      child: SafeArea(
        child: Stack(
          // fit: StackFit.expand,
          children: <Widget>[
            RtmpView(
              manager: _manager,
            ),
            this.buttonArea()
          ],
        ),
      ),
    ));
  }

  Widget buttonArea() {
    return Container(
      // alignment: Alignment(0, 0),
      child: Wrap(
        spacing: 20,
        // runSpacing: 20,
        children: <Widget>[
          this.switchCameraWidget(),
          this.staticIconPerson(),
        ],
      ),
    );
  }

  Widget switchCameraWidget() {
    return Padding(
      padding: EdgeInsets.only(
          top: ScreenUtil().setHeight(80),
          right: ScreenUtil().setWidth(35),
          left: ScreenUtil().setWidth(40)),
      child: new Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        crossAxisAlignment: CrossAxisAlignment.center,
        // verticalDirection: VerticalDirection.up,
        children: <Widget>[
          Container(
            child: GestureDetector(
              child: new Image.asset("images/btn_close.png"),
              onTap: () => {stopVideoStream()},
            ),
            // child: new Image.asset("images/btn_close.png"),
            width: ScreenUtil().setWidth(70),
          ),
          new Row(
            children: [
              Container(
                child: GestureDetector(
                  child: new Image.asset("images/carame.png"),
                  onTap: () => {switchVideoCamera()},
                ),
                width: ScreenUtil().setWidth(84),
                // on
              ),
              Padding(
                padding: EdgeInsets.only(
                    right: ScreenUtil().setWidth(20),
                    left: ScreenUtil().setWidth(20)),
                child: Container(
                  child: new Image.asset("images/wode.png"),
                  width: ScreenUtil().setWidth(84),
                ),
              ),
              Container(
                child: new Image.asset("images/gengduo.png"),
                width: ScreenUtil().setWidth(84),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget staticIconPerson() {
    if (isStreaming) {
      return Padding(
        padding: EdgeInsets.only(
            right: ScreenUtil().setWidth(55), top: ScreenUtil().setHeight(60)),
        child: new Row(
          mainAxisAlignment: MainAxisAlignment.end,
          children: <Widget>[
            Container(
              height: ScreenUtil().setWidth(90),
              child: new Image.asset("images/pindao.png"),
              // getHttp();
            ),
            // new Image.asset("images/pindao.png"),
          ],
        ),
      );
    }
    return Column(
      children: <Widget>[
        Padding(
          padding: EdgeInsets.only(
              right: ScreenUtil().setWidth(55),
              top: ScreenUtil().setHeight(60)),
          child: new Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: <Widget>[
              Container(
                height: ScreenUtil().setWidth(90),
                child: new Image.asset("images/pindao.png"),
                // getHttp();
              ),
              // new Image.asset("images/pindao.png"),
            ],
          ),
        ),
        Center(
          child: Padding(
            padding: EdgeInsets.only(
                top: ScreenUtil().setHeight(580),
                bottom: ScreenUtil().setHeight(120)),
            child: Container(
              width: ScreenUtil().setWidth(600),
              child: new Image.asset("images/shexiang.png"),
              // getHttp();
            ),
          ),
        ),
        Center(
          child: Container(
            width: ScreenUtil().setWidth(405),
            child: GestureDetector(
              child: new Image.asset("images/zhibo.png"),
              onTap: () => {
                // print("======")
                getHttp()
              },
            ),
          ),
        ),
        Center(
            child: Padding(
          padding: EdgeInsets.only(top: ScreenUtil().setHeight(50)),
          child: new Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Container(
                child: new Image.asset("images/pingtai.png"),
                height: ScreenUtil().setHeight(45),
              ),
            ],
          ),
        ))
      ],
    );
  }
}
