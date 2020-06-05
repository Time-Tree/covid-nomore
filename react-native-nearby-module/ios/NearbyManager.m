#import "NearbyManager.h"
#import "DBUtil.h"
#import <GNSMessages.h>
#import <CoreBluetooth/CoreBluetooth.h>
#include <stdlib.h>


static GNSMessageManager *_messageManager;
static NSString *_apiKey;
static NSString *uniqueIdentifier;
static DBUtil *myDBUtil;

@implementation NearbyManager

- init {
    self = [super init];
    myDBUtil = [[DBUtil alloc] init];
    return self;
}

- (void) startService:(nonnull NSString*) apiKey {
    uniqueIdentifier = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
    [self createMessageManagerWithApiKey: apiKey];
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

- (void) publish: (int)code {
    NSLog(@"publish");
    NSString *messageString = [NSString stringWithFormat:@"%@-%d", uniqueIdentifier, code];
    NSLog(@"Attempting to publish: %@", messageString);
    @try {
        if(![self isConnected]) {
            [myDBUtil createEvent: @"NEARBY_PUBLISH_FAILED" withMessage:@"Google API Client not connected. Call .connect() before publishing."];
            @throw [NSException
                    exceptionWithName:@"NotConnected"
                    reason:@"Messenger not connected. Call connect: before publishing."
                    userInfo:nil];
        }
        if(messageString == nil) {
            [myDBUtil createEvent: @"NEARBY_PUBLISH_FAILED" withMessage:@"Cannot publish an empty message"];
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
        [myDBUtil createEvent: @"NEARBY_PUBLISH_SUCCESS" withMessage:messageString];
        NSLog(@"Successfully published: %@", messageString);
    } @catch(NSException *exception) {
        if(exception.reason != nil) {
            [myDBUtil createEvent: @"NEARBY_PUBLISH_FAILED" withMessage:exception.reason];
            NSLog(@"Publish failed: %@", exception.reason);
        }
    }
}



- (void) subscribe {
    NSLog(@"subscribe");
    @try {
        if(![self isConnected]) {
            [myDBUtil createEvent: @"NEARBY_SUBSCRIBE_FAILED" withMessage: @"Google API Client not connected. Call .connect() before publishing."];
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
            NSLog(@"NEARBY_FOUND: %@", message);
            NSString *messageString = [[NSString alloc] initWithData:message.content encoding: NSUTF8StringEncoding];
            [myDBUtil createEvent: @"NEARBY_FOUND" withMessage:messageString];
        } messageLostHandler:^(GNSMessage *message) {
            NSLog(@"NEARBY_LOST: %@", message);
            // NSString *messageString = [[NSString alloc] initWithData:message.content encoding: NSUTF8StringEncoding];
            // [weakSelf createEvent: @"NEARBY_LOST" withMessage:messageString];
        } paramsBlock:^(GNSSubscriptionParams *params) {
            params.strategy = [GNSStrategy strategyWithParamsBlock:^(GNSStrategyParams *params) {
                params.allowInBackground = YES;
            }];
        }];
        [myDBUtil createEvent: @"NEARBY_SUBSCRIBE_SUCCESS" withMessage:@""];
        NSLog(@"Successfully Subscribed.");
    } @catch(NSException *exception) {
        if(exception.reason != nil) {
            [myDBUtil createEvent: @"NEARBY_SUBSCRIBE_FAILED" withMessage:exception.reason];
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

- (void) unpublish {
    NSLog(@"unpublish");
    [myDBUtil createEvent: @"NEARBY_UNPUBLISH" withMessage:@""];
    _publication = nil;
}

- (void) unsubscribe {
    NSLog(@"unsubscribe");
    [myDBUtil createEvent: @"NEARBY_UNSUBSCRIBE" withMessage:@""];
    _subscription = nil;
}

- (void) checkAndConnect {
    if(![self isConnected]) {
        [self sharedMessageManager];
    }
}

@end
