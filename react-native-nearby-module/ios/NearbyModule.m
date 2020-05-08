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
    [nearbyService startService:apiKey];
}

RCT_REMAP_METHOD(getEvents,
                 getEventsWithResolver:(RCTPromiseResolveBlock)resolve
                 getEventsRejecter:(RCTPromiseRejectBlock)reject) {
    NSMutableArray *events = [nearbyService getEvents];
    resolve(events);
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

@end
