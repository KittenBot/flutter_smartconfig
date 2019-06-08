#import "SmartconfigPlugin.h"

@implementation EspTouchDelegateImpl

-(void) onEsptouchResultAddedWithResult:(ESPTouchResult *)result
{
    NSLog(@"EspTouchDelegate bssid %@", result.bssid);
    dispatch_async(dispatch_get_main_queue(), ^{
        //
    });
}

@end

@implementation SmartconfigPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"smartconfig"
            binaryMessenger:[registrar messenger]];
  SmartconfigPlugin* instance = [[SmartconfigPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (instancetype)init{
    if (self = [super init]){
        self._esptouchDelegate = [[EspTouchDelegateImpl alloc] init];
    }
    return self;
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  NSDictionary *arguments = [call arguments];
  if ([@"start" isEqualToString:call.method]) {
    NSString *ssid = [arguments objectForKey:@"ssid"];
    NSString *bssid = [arguments objectForKey:@"bssid"];
    NSString *pass = [arguments objectForKey:@"pass"];
    [self start:ssid bssid:bssid pass:pass result:result];
    // result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else {
    result(FlutterMethodNotImplemented);
  }
}

- (void)start:(NSString*)ssid
        bssid:(NSString*)bssid
         pass:(NSString*)pass
       result:(FlutterResult)result {

    dispatch_queue_t  queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    dispatch_async(queue, ^{
        
        NSArray *esptouchResultArray = [self executeForResults:ssid bssid:bssid pass:pass];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            BOOL resolved = false;
            NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];

            for (int i = 0; i < [esptouchResultArray count]; ++i)
            {
                ESPTouchResult *resultInArray = [esptouchResultArray objectAtIndex:i];
                
                if (![resultInArray isCancelled] && [resultInArray bssid] != nil) {
                    
                    unsigned char *ipBytes = (unsigned char *)[[resultInArray ipAddrData] bytes];
                    
                    NSString *ipv4String = [NSString stringWithFormat:@"%d.%d.%d.%d", ipBytes[0], ipBytes[1], ipBytes [2], ipBytes [3]];
                    
                    dict[[resultInArray bssid]] = ipv4String;
                    resolved = true;
                    if (![resultInArray isSuc])
                        break;
                }
                
                
            }
            if(resolved)
                result(dict);
            else
                result(nil);
            
        });
        
    });
}

- (void)cancel {
    [self._condition lock];
    if (self._esptouchTask != nil)
    {
        [self._esptouchTask interrupt];
    }
    [self._condition unlock];
}

#pragma mark - the example of how to use executeForResults
- (NSArray *) executeForResults:(NSString*)ssid
                          bssid:(NSString*)bssid
                           pass:(NSString*)pass
{
    [self cancel];
    [self._condition lock];
    
    NSLog(@"ssid %@ pass %@ bssid %@", ssid, pass, bssid);
    self._esptouchTask =
    [[ESPTouchTask alloc]initWithApSsid:ssid andApBssid:bssid andApPwd:pass];
    // set delegate
    [self._esptouchTask setEsptouchDelegate:self._esptouchDelegate];
    [self._condition unlock];
    NSArray * esptouchResults = [self._esptouchTask executeForResults:1];
    
    return esptouchResults;
}

@end




