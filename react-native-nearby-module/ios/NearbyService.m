#import "NearbyService.h"
#import "DBUtil.h"
#import "BLEScanner.h"
#import "BLEAdvertiser.h"
#import <GNSMessages.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import <BackgroundTasks/BackgroundTasks.h>
#include <stdlib.h>


static GNSMessageManager *_messageManager;
static NSString *_apiKey;
static NSString *uniqueIdentifier;
static int code;
static CBCentralManager *myCentralManager;
static NSMutableArray *events;
static DBUtil *myDBUtil;
static BLEScanner *myBLEScanner;
static BLEAdvertiser *myBLEAdvertiser;

@implementation NearbyService

- init {
    self = [super init];
    if([NSThread isMainThread]) {
        if ([UIApplication instancesRespondToSelector:@selector(registerUserNotificationSettings:)]) {
            [[UIApplication sharedApplication] registerUserNotificationSettings:
             [UIUserNotificationSettings settingsForTypes:
              UIUserNotificationTypeAlert | UIUserNotificationTypeBadge | UIUserNotificationTypeSound
                                               categories:nil]];
        }
        myDBUtil = [[DBUtil alloc] init];
        myBLEScanner = [[BLEScanner alloc] init];
        myBLEAdvertiser = [[BLEAdvertiser alloc] init];
    }
    [self setBackgroundTask];
    return self;
}

- (void) setBackgroundTask {
    UIBackgroundTaskIdentifier bgTask;
    UIApplication  *app = [UIApplication sharedApplication];
    bgTask = [app beginBackgroundTaskWithExpirationHandler:^{
        [app endBackgroundTask:bgTask];
    }];
}

- (void) startService:(nonnull NSString*) apiKey {
    uniqueIdentifier = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
    [self createMessageManagerWithApiKey: apiKey];
    if([NSThread isMainThread] == false) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self setTimer];
        });
    } else {
        [self setTimer];
    }
    events = [NSMutableArray array];
    [myBLEScanner scan];
    [myBLEAdvertiser startAdvertising];
}

- (void) setTimer {
    [self startTimer];
    self.silenceTimer = [NSTimer scheduledTimerWithTimeInterval: 30.0
                                                         target: self
                                                       selector: @selector(startTimer)
                                                       userInfo: nil repeats:YES];
}

- (void) startTimer {
    code = 1000 + arc4random_uniform(9000);
    NSLog(@"code = %d", code);
    [self unpublish];
    [self checkAndConnect];
    [self publish: code];
}

- (void) stopTimer {
    NSLog(@"stopTimer");
    [self.silenceTimer invalidate];
}

- (id)createMessageManagerWithApiKey:(nonnull NSString*) apiKey {
    if(apiKey == nil) {
        @throw [NSException
                exceptionWithName:@"ApiKeyNotGiven"
                reason:@"No Api Key was given."
                userInfo:nil];
    }
    _apiKey = apiKey;
    return [self sharedMessageManager];
}

- (id)sharedMessageManager {
    @synchronized(self) {
        if(_messageManager == nil) {
            if(_apiKey == nil) {
                @throw [NSException
                        exceptionWithName:@"ApiKeyNil"
                        reason:@"Api Key was nil."
                        userInfo:nil];
            }
            _messageManager = [[GNSMessageManager alloc] initWithAPIKey: _apiKey];
        }
    }
    return _messageManager;
}

- (void) disconnect {
    NSLog(@"disconnect");
    // iOS Doesn't have a disconnect: method
    // Try setting messageManager to nil & save _apiKey
    @synchronized(self) {
        _messageManager = nil;
    }
}

- (Boolean) isConnected {
    @synchronized(self) {
        if(_messageManager == nil) {
            return false;
        } else {
            return true;
        }
    }
}

-(void) publish: (int)code {
    NSLog(@"publish");
    NSString *messageString = [NSString stringWithFormat:@"%@-%d", uniqueIdentifier, code];
    NSLog(@"Attempting to publish: %@", messageString);
    @try {
        if(![self isConnected]) {
            [myDBUtil createEvent: @"PUBLISH_FAILED" withMessage:@"Google API Client not connected. Call .connect() before publishing."];
            @throw [NSException
                    exceptionWithName:@"NotConnected"
                    reason:@"Messenger not connected. Call connect: before publishing."
                    userInfo:nil];
        }
        if(messageString == nil) {
            [myDBUtil createEvent: @"PUBLISH_FAILED" withMessage:@"Cannot publish an empty message"];
            NSLog(@"Cannot publish an empty message");
            return;
        }
        // Create new message
        GNSMessage *message = [GNSMessage messageWithContent: [messageString dataUsingEncoding: NSUTF8StringEncoding]];
        _publication = [[self sharedMessageManager] publicationWithMessage: message paramsBlock:^(GNSPublicationParams *params) {
            params.strategy = [GNSStrategy strategyWithParamsBlock:^(GNSStrategyParams *params) {
                params.allowInBackground = YES;
            }];
        }];
        [myDBUtil createEvent: @"PUBLISH_SUCCESS" withMessage:messageString];
        NSLog(@"Successfully published: %@", messageString);
    } @catch(NSException *exception) {
        if(exception.reason != nil) {
            [myDBUtil createEvent: @"PUBLISH_FAILED" withMessage:exception.reason];
            NSLog(@"Publish failed: %@", exception.reason);
        }
    }
}



-(void) subscribe {
    NSLog(@"subscribe");
    if([NSThread isMainThread] == false) return;
    @try {
        if(![self isConnected]) {
            [myDBUtil createEvent: @"SUBSCRIBE_FAILED" withMessage: @"Google API Client not connected. Call .connect() before publishing."];
            @throw [NSException
                    exceptionWithName:@"NotConnected"
                    reason:@"Messenger not connected. Call connect: before subscribing."
                    userInfo:nil];
        }
        // Create _subscription object
        _subscription = [[self sharedMessageManager] subscriptionWithMessageFoundHandler:^(GNSMessage *message) {
            // Send a local notification if not in the foreground.
            if ([UIApplication sharedApplication].applicationState != UIApplicationStateActive) {
                UILocalNotification *localNotification = [[UILocalNotification alloc] init];
                localNotification.alertBody = @"Message received";
                [[UIApplication sharedApplication] presentLocalNotificationNow:localNotification];
            }
            NSLog(@"MESSAGE_FOUND: %@", message);
            NSString *messageString = [[NSString alloc] initWithData:message.content encoding: NSUTF8StringEncoding];
            [myDBUtil createEvent: @"MESSAGE_FOUND" withMessage:messageString];
        } messageLostHandler:^(GNSMessage *message) {
            NSLog(@"MESSAGE_LOST: %@", message);
            // NSString *messageString = [[NSString alloc] initWithData:message.content encoding: NSUTF8StringEncoding];
            // [weakSelf createEvent: @"MESSAGE_LOST" withMessage:messageString];
        } paramsBlock:^(GNSSubscriptionParams *params) {
            params.strategy = [GNSStrategy strategyWithParamsBlock:^(GNSStrategyParams *params) {
                params.allowInBackground = YES;
            }];
        }];
        [myDBUtil createEvent: @"SUBSCRIBE_SUCCESS" withMessage:@""];
        NSLog(@"Successfully Subscribed.");
    } @catch(NSException *exception) {
        if(exception.reason != nil) {
            [myDBUtil createEvent: @"SUBSCRIBE_FAILED" withMessage:exception.reason];
            NSLog(@"SUBSCRIBE_FAILED: %@", exception.reason);
        }
    }
}

- (Boolean) isPublishing {
    if(_publication != nil) {
        return true;
    } else {
        return false;
    }
}

- (Boolean) isSubscribing {
    if(_subscription != nil) {
        return true;
    } else {
        return false;
    }
}

-(void) unpublish {
    NSLog(@"unpublish");
    _publication = nil;
}

-(void) unsubscribe {
    NSLog(@"unsubscribe");
    _subscription = nil;
}

- (void) checkAndConnect {
    if(![self isConnected]) {
        [self sharedMessageManager];
    }
    if(![self isSubscribing]) {
        [self subscribe];
    }
}

- (void) deleteAllData {
    [myDBUtil deleteAllData];
}

@end
