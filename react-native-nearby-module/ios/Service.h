
@interface Service : NSObject

@property (nonatomic, retain) NSTimer * _Nullable nearbyStartTimer;
@property (nonatomic, retain) NSTimer * _Nullable nearbyStopTimer;
@property (nonatomic, retain) NSTimer * _Nullable bleStartTimer;
@property (nonatomic, retain) NSTimer * _Nullable bleStopTimer;

- (void) startService:(nonnull NSString*) apiKey;
- (void) deleteAllData;
- (void) restartService;

@end
