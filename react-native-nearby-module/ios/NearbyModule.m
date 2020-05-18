#import "NearbyModule.h"
#import "NearbyService.h"

static NearbyService* nearbyService = nil;

@implementation NearbyModule 

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[@""];
}
- init {
    self = [super init];
    nearbyService = [[NearbyService alloc] init];
    return self;
}

RCT_EXPORT_METHOD(startService: (nonnull NSString *)apiKey) {
    nearbyService = [[NearbyService alloc] init];
    [nearbyService startService:apiKey];
}

RCT_REMAP_METHOD(getStatus,
                 getStatusWithResolver:(RCTPromiseResolveBlock)resolve
                 getStatusRejecter:(RCTPromiseRejectBlock)reject) {
    
    @try {
        Boolean isConnected = [nearbyService isConnected];
        Boolean isSubscribing = [nearbyService isSubscribing];
        if (!isConnected || !isSubscribing) {
            [nearbyService checkAndConnect];
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
    [nearbyService deleteAllData];
    resolve([NSNumber numberWithBool:TRUE]);
}

@end
