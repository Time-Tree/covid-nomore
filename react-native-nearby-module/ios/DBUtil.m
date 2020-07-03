#import "DBUtil.h"
#import "DBManager.h"

static DBManager *myDBManager;

@implementation DBUtil

+ (id) sharedInstance {
    static DBUtil *sharedInstance = nil;
    @synchronized(self) {
        if (sharedInstance == nil)
            sharedInstance = [[self alloc] init];
    }
    return sharedInstance;
}

- (instancetype) init {
    self = [super init];
    myDBManager = [DBManager sharedInstance];
    return self;
}

- (void) createEvent:(nonnull NSString*)eventType withMessage:(nonnull NSString*) message {
    NSLog(@"createEvent: eventType=%@ message=%@", eventType, message);
    NSTimeInterval timestamp = [[NSDate date] timeIntervalSince1970];
    NSNumber *timestampObj = [NSNumber numberWithLong:timestamp * 1000.0];
    NSString *formattedDate = [self getFormattedDate: timestamp];
    NSDictionary *dict = @{
        @"eventType": eventType,
        @"message": message,
        @"formatDate": formattedDate,
        @"timestamp": timestampObj
    };
    [myDBManager saveEventData: dict];
}

- (NSString *) getFormattedDate:(NSTimeInterval) timestamp {
    NSDateFormatter *dateFormatter=[[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"HH:mm:ss:SS"];
    NSString *formattedDate = [dateFormatter stringFromDate:[NSDate dateWithTimeIntervalSince1970:timestamp]];
    return formattedDate;
}

- (void) deleteAllData {
    [myDBManager deleteAllData];
}

- (NSDictionary *) getSettingsData {
    return [myDBManager getSettingsData];
}

- (NSDictionary *) getLastToken {
    return [myDBManager getLastToken];
}

- (void) addToken:(nonnull NSString*)token {
    NSLog(@"addToken: token=%@", token);
    NSTimeInterval timestamp = [[NSDate date] timeIntervalSince1970];
    NSNumber *timestampObj = [NSNumber numberWithLong:timestamp * 1000.0];
    NSDictionary *dict = @{
        @"token": token,
        @"created": timestampObj,
        @"used": @0
    };
    NSLog(@"dict = %@", dict);
    [myDBManager saveToken: dict];
}

- (void) addHandshake: (NSDictionary *) data {
    NSTimeInterval timestamp = [[NSDate date] timeIntervalSince1970];
    NSNumber *timestampObj = [NSNumber numberWithLong:timestamp * 1000.0];
    [data setValue:timestampObj forKey:@"discovered"];
    [myDBManager saveHandshake:data];
}

- (void) updateTokenUsed: (NSString *) token {
    [myDBManager updateTokenUsed:token];
}

@end
