#import "NearbyModule.h"
#import "Service.h"
#import "NearbyManager.h"

static Service* service = nil;
static NearbyManager* nearbyManager = nil;

@implementation NearbyModule 

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[@""];
}
- init {
    self = [super init];
    service = [[Service alloc] init];
    nearbyManager = [[NearbyManager alloc] init];
    return self;
}

RCT_EXPORT_METHOD(startService: (nonnull NSString *)apiKey) {
    [service startService:apiKey];
}

RCT_EXPORT_METHOD(restartService) {
    [service restartService];
}

RCT_REMAP_METHOD(getStatus,
                 getStatusWithResolver:(RCTPromiseResolveBlock)resolve
                 getStatusRejecter:(RCTPromiseRejectBlock)reject) {
    
    @try {
        Boolean isConnected = [nearbyManager isConnected];
        Boolean isSubscribing = [nearbyManager isSubscribing];
        if (!isConnected || !isSubscribing) {
            [nearbyManager checkAndConnect];
        }
        NSDictionary *dict = @{
            @"isConnected": [NSNumber numberWithBool:isConnected],
            @"isSubscribing": [NSNumber numberWithBool:isSubscribing]
        };
        resolve(dict);
    } @catch(NSException *exception) {
        NSLog(@"getStatus error: %@", exception.reason);
    }
}

RCT_REMAP_METHOD(toggleState,
                 toggleStateWithResolver:(RCTPromiseResolveBlock)resolve
                 toggleStateRejecter:(RCTPromiseRejectBlock)reject) {
    [service deleteAllData];
    resolve([NSNumber numberWithBool:TRUE]);
}

@end
