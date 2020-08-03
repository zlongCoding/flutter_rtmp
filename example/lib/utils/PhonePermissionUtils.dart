import 'package:permission_handler/permission_handler.dart';

class PhonePermissionUtils {
  // 申请存储权限
  static Future<bool> checkPermission() async {
    // if (Theme.of(context).platform == TargetPlatform.android) {// 平台进行判断
    bool toRequest = false;
    List<PermissionGroup> permissions = [
      PermissionGroup.storage,
      PermissionGroup.camera,
      PermissionGroup.microphone
    ];
    print("开始判断权限");
    for (PermissionGroup permission in permissions) {
      if (await PermissionHandler().checkPermissionStatus(permission) !=
          PermissionStatus.granted) {
        toRequest = true;
        break;
      }
    }
    if (toRequest) {
      print("开始请求权限");
      return await PhonePermissionUtils.reqPermissions(permissions);
    } else {
      return true;
    }
  }

  static reqPermissions(List<PermissionGroup> permissions) async {
    bool isGranted = true;
    Map<PermissionGroup, PermissionStatus> reqResult =
        await PermissionHandler().requestPermissions(permissions);
    for (PermissionGroup permission in permissions) {
      if (reqResult[permission] != PermissionStatus.granted) {
        isGranted = false;
        break;
      }
    }
    if (isGranted) {
      print("授权通过");
      return true;
    } else {
      print("授权失败，重新授权");
      return await PhonePermissionUtils.reqPermissions(permissions);
    }
  }
}
