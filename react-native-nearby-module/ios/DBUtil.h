@interface DBUtil : NSObject

- (void) createEvent:(nonnull NSString*)eventType withMessage:(nonnull NSString*) message;
- (void) deleteAllData;

@end
