#import "DBUtil.h"
#import "DBManager.h"

static DBManager *myDBManager;

@implementation DBUtil

- (instancetype) init {
    self = [super init];
    myDBManager = [[DBManager alloc] init];
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
    [myDBManager saveData: dict];
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

@end
