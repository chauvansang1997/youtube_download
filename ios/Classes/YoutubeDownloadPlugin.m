#import "YoutubeDownloadPlugin.h"
#if __has_include(<youtube_download/youtube_download-Swift.h>)
#import <youtube_download/youtube_download-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "youtube_download-Swift.h"
#endif

@implementation YoutubeDownloadPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftYoutubeDownloadPlugin registerWithRegistrar:registrar];
}
@end
