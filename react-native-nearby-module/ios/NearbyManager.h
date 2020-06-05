#import <GNSMessages.h>

@interface NearbyManager : NSObject

@property(nonatomic, strong) id<GNSPublication> publication;
@property(nonatomic, strong) id<GNSSubscription> subscription;

- (Boolean) isSubscribing;
- (Boolean) isConnected;
- (void) checkAndConnect;
- (void) startService:(nonnull NSString*) apiKey;
- (void) publish: (int)code;
- (void) unpublish;
- (void) subscribe;
- (void) unsubscribe;

@end
