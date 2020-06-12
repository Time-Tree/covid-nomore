@interface DBManager : NSObject

@property(nonatomic, strong) NSString *databasePath;

- (void) saveEventData: (NSDictionary *) event;
- (void) deleteAllData;
- (NSDictionary *) getSettingsData;

@end
