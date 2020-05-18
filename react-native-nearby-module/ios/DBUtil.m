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
    NSTimeInterval timeStamp = [[NSDate date] timeIntervalSince1970];
    NSNumber *timeStampObj = [NSNumber numberWithDouble: timeStamp];
    NSString *formattedDate = [self getFormattedDate];
    NSDictionary *dict = @{
        @"eventType": eventType,
        @"message": message,
        @"formatDate": formattedDate,
        @"timestamp": timeStampObj
    };
    [myDBManager saveData: dict];
}

- (NSString *) getFormattedDate {
    NSDateFormatter *dateFormatter=[[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"HH:mm:ss:SS"];
    NSString *formattedDate = [dateFormatter stringFromDate:[NSDate date]];
    return formattedDate;
}

- (void) deleteAllData {
    [myDBManager deleteAllData];
}

@end
