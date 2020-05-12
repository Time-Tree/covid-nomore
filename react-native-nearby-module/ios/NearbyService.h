#import <GNSMessages.h>

@interface NearbyService : NSObject

@property(nonatomic, strong) id<GNSPublication> publication;
@property(nonatomic, strong) id<GNSSubscription> subscription;
@property (nonatomic, retain) NSTimer *silenceTimer;

- (void) startService:(nonnull NSString*) apiKey;
- (void) createEvent:(nonnull NSString*)eventType withMessage:(nonnull NSString*) message;
- (nonnull NSMutableArray*) getEvents;
- (Boolean) isSubscribing;
- (Boolean) isConnected;
- (void) checkAndConnect;

@end
