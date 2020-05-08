#import "NearbyService.h"
#import <GNSMessages.h>
#import <CoreBluetooth/CoreBluetooth.h>
#include <stdlib.h>

/// The main message manager to handle connection, publications, and subscriptions.
static GNSMessageManager *_messageManager = nil;
static NSString *_apiKey = nil;
static BOOL _isBLEOnly = false;
static NSTimer *timer;
static NSString *uniqueIdentifier;
static int code;
static CBCentralManager *myCentralManager;
static NSMutableArray *events;

@implementation NearbyService


- (void) startService:(nonnull NSString*) apiKey {
    uniqueIdentifier = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
    [self createMessageManagerWithApiKey: apiKey];
    if([NSThread isMainThread] == false) {
        dispatch_async(dispatch_get_main_queue(), ^{
            NSLog(@"-------------------------> Am inceput din if");
            [self setTimer];
        });
    } else {
        NSLog(@"-------------------------> Am inceput din else");
        [self setTimer];
    }
    events = [NSMutableArray array];
}

- (void) setTimer {
    timer = [NSTimer scheduledTimerWithTimeInterval: 10.0
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
    [timer invalidate];
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

- (void) connect: (nonnull NSString *)apiKey isBLEOnly:(BOOL)bleOnly {
    NSLog(@"connect");
    // iOS Doesn't have a connect: method
    _isBLEOnly = bleOnly;
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
    NSLog(@"isConnected");
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
            [self createEvent: @"PUBLISH_FAILED" withMessage:@"Google API Client not connected. Call .connect() before publishing."];
            @throw [NSException
                    exceptionWithName:@"NotConnected"
                    reason:@"Messenger not connected. Call connect: before publishing."
                    userInfo:nil];
        }
        if(messageString == nil) {
            [self createEvent: @"PUBLISH_FAILED" withMessage:@"Cannot publish an empty message"];
            NSLog(@"Cannot publish an empty message");
            return;
        }
        // Create new message
        GNSMessage *message = [GNSMessage messageWithContent: [messageString dataUsingEncoding: NSUTF8StringEncoding]];
        _publication = [[self sharedMessageManager] publicationWithMessage: message paramsBlock:^(GNSPublicationParams *params) {
            params.strategy = [GNSStrategy strategyWithParamsBlock:^(GNSStrategyParams *params) {
                params.discoveryMediums = _isBLEOnly ? kGNSDiscoveryMediumsBLE : kGNSDiscoveryModeDefault;
            }];
        }];
        [self createEvent: @"PUBLISH_SUCCESS" withMessage:messageString];
        NSLog(@"Successfully published: %@", messageString);
    } @catch(NSException *exception) {
        if(exception.reason != nil) {
            [self createEvent: @"PUBLISH_FAILED" withMessage:exception.reason];
            NSLog(@"Publish failed: %@", exception.reason);
        }
    }
}



-(void) subscribe {
    NSLog(@"subscribe");
    if([NSThread isMainThread] == false) return;
    @try {
        if(![self isConnected]) {
            [self createEvent: @"SUBSCRIBE_FAILED" withMessage: @"Google API Client not connected. Call .connect() before publishing."];
            @throw [NSException
                    exceptionWithName:@"NotConnected"
                    reason:@"Messenger not connected. Call connect: before subscribing."
                    userInfo:nil];
        }
        // Create _subscription object
        _subscription = [[self sharedMessageManager] subscriptionWithMessageFoundHandler:^(GNSMessage *message) {
            NSLog(@"MESSAGE_FOUND: %@", message);
            NSString *messageString = [[NSString alloc] initWithData:message.content encoding: NSUTF8StringEncoding];
            [self createEvent: @"MESSAGE_FOUND" withMessage:messageString];
        } messageLostHandler:^(GNSMessage *message) {
            NSLog(@"MESSAGE_LOST: %@", message);
            NSString *messageString = [[NSString alloc] initWithData:message.content encoding: NSUTF8StringEncoding];
            [self createEvent: @"MESSAGE_LOST" withMessage:messageString];
        } paramsBlock:^(GNSSubscriptionParams *params) {
            params.strategy = [GNSStrategy strategyWithParamsBlock:^(GNSStrategyParams *params) {
                params.allowInBackground = false; //TODO: Make this configurable
                params.discoveryMediums = _isBLEOnly ? kGNSDiscoveryMediumsBLE : kGNSDiscoveryModeDefault;
                if (_isBLEOnly == YES) {
                    [self createEvent: @"STRATEGY" withMessage:@"BLE"];
                } else {
                    [self createEvent: @"STRATEGY" withMessage:@"UltraSonic"];
                }
            }];
        }];
        [self createEvent: @"SUBSCRIBE_SUCCESS" withMessage:@""];
        NSLog(@"Successfully Subscribed.");
    } @catch(NSException *exception) {
        if(exception.reason != nil) {
            [self createEvent: @"SUBSCRIBE_FAILED" withMessage:exception.reason];
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

- (void) createEvent:(nonnull NSString*)eventType withMessage:(nonnull NSString*) message {
    NSLog(@"-----> createEvent: eventType=%@ message=%@", eventType, message);
    NSTimeInterval timeStamp = [[NSDate date] timeIntervalSince1970];
    NSNumber *timeStampObj = [NSNumber numberWithDouble: timeStamp];
    NSString *formattedDate = [self getFormattedDate];
    NSDictionary *dict = @{
        @"eventType": eventType,
        @"message": message,
        @"formatDate": formattedDate,
        @"timestamp": timeStampObj
    };
    [events addObject: dict];
}

- (NSString *) getFormattedDate {
    NSDateFormatter *dateFormatter=[[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"HH:mm:ss:SS"];
    NSString *formattedDate = [dateFormatter stringFromDate:[NSDate date]];
    return formattedDate;
}

- (NSMutableArray *) getEvents {
    return events;
}

@end
