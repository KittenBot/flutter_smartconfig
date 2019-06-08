#import <Flutter/Flutter.h>

#import "ESPTouchTask.h"
#import "ESPTouchResult.h"
#import "ESP_NetUtil.h"
#import "ESPTouchDelegate.h"

@interface EspTouchDelegateImpl : NSObject<ESPTouchDelegate>

@end

@interface SmartconfigPlugin : NSObject<FlutterPlugin>

@property (nonatomic, strong) NSDictionary *defaultOptions;
@property (nonatomic, strong) EspTouchDelegateImpl *_esptouchDelegate;
@property (nonatomic, strong) NSCondition *_condition;

@property (atomic, strong) ESPTouchTask *_esptouchTask;

@end
