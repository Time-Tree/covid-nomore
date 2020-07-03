@interface DBUtil : NSObject

+ (id) sharedInstance;
- (void) createEvent:(nonnull NSString*)eventType withMessage:(nonnull NSString*) message;
- (void) deleteAllData;
- (NSDictionary *_Nonnull) getSettingsData;
- (NSDictionary *) getLastToken;
- (void) addToken:(nonnull NSString*)token;
- (void) addHandshake: (NSDictionary *) data;
- (void) updateTokenUsed: (NSString *) token;

@end
