#import <GNSMessages.h>

@interface NearbyService : NSObject

@property(nonatomic, strong) id<GNSPublication> publication;
@property(nonatomic, strong) id<GNSSubscription> subscription;
@property (nonatomic, retain) NSTimer *silenceTimer;

- (void) startService:(nonnull NSString*) apiKey;
- (Boolean) isSubscribing;
- (Boolean) isConnected;
- (void) checkAndConnect;
- (void) deleteAllData;

@end
