//
//  Generated file. Do not edit.
//

#import "GeneratedPluginRegistrant.h"

#if __has_include(<flutter_audio_capture/FlutterAudioCapturePlugin.h>)
#import <flutter_audio_capture/FlutterAudioCapturePlugin.h>
#else
@import flutter_audio_capture;
#endif

#if __has_include(<flutter_ijkplayer/IjkplayerPlugin.h>)
#import <flutter_ijkplayer/IjkplayerPlugin.h>
#else
@import flutter_ijkplayer;
#endif

#if __has_include(<flutter_plugin_record/FlutterPluginRecordPlugin.h>)
#import <flutter_plugin_record/FlutterPluginRecordPlugin.h>
#else
@import flutter_plugin_record;
#endif

#if __has_include(<flutter_rtmp/FlutterRtmpPlugin.h>)
#import <flutter_rtmp/FlutterRtmpPlugin.h>
#else
@import flutter_rtmp;
#endif

#if __has_include(<flutter_socket_io/FlutterSocketIoPlugin.h>)
#import <flutter_socket_io/FlutterSocketIoPlugin.h>
#else
@import flutter_socket_io;
#endif

#if __has_include(<flutter_splash_screen/FlutterSplashScreenPlugin.h>)
#import <flutter_splash_screen/FlutterSplashScreenPlugin.h>
#else
@import flutter_splash_screen;
#endif

#if __has_include(<permission_handler/PermissionHandlerPlugin.h>)
#import <permission_handler/PermissionHandlerPlugin.h>
#else
@import permission_handler;
#endif

#if __has_include(<wakelock/WakelockPlugin.h>)
#import <wakelock/WakelockPlugin.h>
#else
@import wakelock;
#endif

@implementation GeneratedPluginRegistrant

+ (void)registerWithRegistry:(NSObject<FlutterPluginRegistry>*)registry {
  [FlutterAudioCapturePlugin registerWithRegistrar:[registry registrarForPlugin:@"FlutterAudioCapturePlugin"]];
  [IjkplayerPlugin registerWithRegistrar:[registry registrarForPlugin:@"IjkplayerPlugin"]];
  [FlutterPluginRecordPlugin registerWithRegistrar:[registry registrarForPlugin:@"FlutterPluginRecordPlugin"]];
  [FlutterRtmpPlugin registerWithRegistrar:[registry registrarForPlugin:@"FlutterRtmpPlugin"]];
  [FlutterSocketIoPlugin registerWithRegistrar:[registry registrarForPlugin:@"FlutterSocketIoPlugin"]];
  [FlutterSplashScreenPlugin registerWithRegistrar:[registry registrarForPlugin:@"FlutterSplashScreenPlugin"]];
  [PermissionHandlerPlugin registerWithRegistrar:[registry registrarForPlugin:@"PermissionHandlerPlugin"]];
  [WakelockPlugin registerWithRegistrar:[registry registrarForPlugin:@"WakelockPlugin"]];
}

@end
