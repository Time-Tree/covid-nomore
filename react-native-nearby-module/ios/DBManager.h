@interface DBManager : NSObject

@property(nonatomic, strong) NSString *databasePath;

- (void) saveData: (NSDictionary *) event;
- (void) deleteAllData;

@end
