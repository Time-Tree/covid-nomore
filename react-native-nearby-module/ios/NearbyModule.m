#import "NearbyModule.h"

/*
 * @brief Events broadcasted through the RCTEventEmitter. Handles all stages of connections through the NearbyModule SDK.
 * @constant CONNECTED The GNSMessageManager instance has been initialized with an API key.
 * @constant CONNECTION_SUSPENDED The connection was cancelled by the GNSMessageManager (iOS unused).
 * @constant CONNECTION_FAILED The connection has failed.
 * @constant DISCONNECTED The GNSMessageManager has disconnected or deallocated.
 * @constant MESSAGE_FOUND A GNSMessage has been found through the GNSSubscription.
 * @constant MESSAGE_LOST A GNSMessage has been lost through the GNSSubscription.
 * @constant DISTANCE_CHANGED: A GNSMessage distance changed (iOS unused).
 * @constant BLE_SIGNAL_CHANGED A GNSMessage signal strength changed (iOS unused).
 * @constant PUBLISH_SUCCESS The GNSPublication has successfully started publishing.
 * @constant PUBLISH_FAILED The GNSPublication has failed to start publishing.
 * @constant SUBSCRIBE_SUCCESS The GNSSubscription has successfully started subscribing.
 * @constant SUBSCRIBE_FAILED The GNSSubscription has failed start subscribing.
 */
typedef NS_ENUM(NSInteger, NearbyMessagesEvent) {
    CONNECTED,
    CONNECTION_SUSPENDED,
    CONNECTION_FAILED,
    DISCONNECTED,
    MESSAGE_FOUND,
    MESSAGE_LOST,
    DISTANCE_CHANGED,
    BLE_SIGNAL_CHANGED,
    PUBLISH_SUCCESS,
    PUBLISH_FAILED,
    SUBSCRIBE_SUCCESS,
    SUBSCRIBE_FAILED,
};

/// The main message manager to handle connection, publications, and subscriptions.
static GNSMessageManager *_messageManager = nil;

/// The Google Play Service API key supplied.
static NSString *_apiKey = nil;
static BOOL _isBLEOnly = false;

@implementation NearbyModule 

RCT_EXPORT_MODULE()

/**
 * @brief Converts a valid NearbyMessagesEvent enum to a NSString equivalent.
 * @param event The NearbyMessagesEvent to convert.
 * @return NSString conversion of the event type.
 */
- (nonnull NSString *)stringForAPIEvent:(NearbyMessagesEvent)event {
    switch(event) {
        case CONNECTED: return @"CONNECTED";
        case CONNECTION_SUSPENDED: return @"CONNECTION_SUSPENDED";
        case CONNECTION_FAILED: return @"CONNECTION_FAILED";
        case DISCONNECTED: return @"DISCONNECTED";
        case MESSAGE_FOUND: return @"MESSAGE_FOUND";
        case MESSAGE_LOST: return @"MESSAGE_LOST";
        case DISTANCE_CHANGED: return @"DISTANCE_CHANGED";
        case BLE_SIGNAL_CHANGED: return @"BLE_SIGNAL_CHANGED";
        case PUBLISH_SUCCESS: return @"PUBLISH_SUCCESS";
        case PUBLISH_FAILED: return @"PUBLISH_FAILED";
        case SUBSCRIBE_SUCCESS: return @"SUBSCRIBE_SUCCESS";
        case SUBSCRIBE_FAILED: return @"SUBSCRIBE_FAILED";
        default: return @"CONNECTED";
    }
}

- (NSArray<NSString *> *)supportedEvents {
    return @[@"subscribe"];
}

RCT_EXPORT_METHOD(sampleMethod:(NSString *)stringArgument numberParameter:(nonnull NSNumber *)numberArgument callback:(RCTResponseSenderBlock)callback)
{
    // TODO: Implement some actually useful functionality
    callback(@[[NSString stringWithFormat: @"numberArgument: %@ stringArgument: %@", numberArgument, stringArgument]]);
}

- (void)sendEvent:(NearbyMessagesEvent)event withMessage:(GNSMessage *)message {
    NSString *eventString = [self stringForAPIEvent:event];
    NSString *messageString = [[NSString alloc] initWithData:message.content encoding: NSUTF8StringEncoding];
    NSDictionary *body = @{
                           @"event": eventString,
                           @"message": messageString
                           };
    [self sendEventWithName:@"subscribe" body:body];
}

- (void)sendEvent:(NearbyMessagesEvent)event withString:(NSString *)string {
    NSString *eventString = [self stringForAPIEvent:event];
    NSDictionary *body = @{
                           @"event": eventString,
                           @"message": string
                           };
    [self sendEventWithName:@"subscribe" body:body];
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

- (NSNumber *) isConnected {
    @synchronized(self) {
        if(_messageManager == nil) {
            return [NSNumber numberWithBool:0];
        } else {
            return [NSNumber numberWithBool:1];
        }
    }
}

RCT_EXPORT_METHOD(isConnected:(RCTResponseSenderBlock) callback)
{
    NSNumber *connected = [self isConnected];
    callback(@[connected, [NSNull null]]);
}

RCT_EXPORT_METHOD(connect: (nonnull NSString *)apiKey isBLEOnly:(BOOL)bleOnly) {
    // iOS Doesn't have a connect: method
    @try {
        _isBLEOnly = bleOnly;
        [self createMessageManagerWithApiKey: apiKey];
        [self sendEvent:CONNECTED withString:@"Successfully connected."];
    } @catch(NSException *exception) {
        if(exception.reason != nil) {
            [self sendEvent:CONNECTION_FAILED withString: exception.reason];
        } else {
            [self sendEvent:CONNECTION_FAILED withString: @"Connection failed."];
        }
    }
}

RCT_EXPORT_METHOD(disconnect) {
    // iOS Doesn't have a disconnect: method
    // Try setting messageManager to nil & save _apiKey
    @synchronized(self) {
        _messageManager = nil;
    }
    [self sendEvent:DISCONNECTED withString:@"Successfully disconnected."];
}

RCT_EXPORT_METHOD(isPublishing:(RCTResponseSenderBlock) callback)
{
    if(_publication != nil) {
        callback(@[@true, [NSNull null]]);
    } else {
        callback(@[@false, [NSNull null]]);
    }
}

RCT_EXPORT_METHOD(publish:(nonnull NSString *)messageString) {
    @try {
        if(![self isConnected]) {
            @throw [NSException
                    exceptionWithName:@"NotConnected"
                    reason:@"Messenger not connected. Call connect: before publshing."
                    userInfo:nil];
        }
        if(messageString == nil) {
            [self sendEvent:PUBLISH_FAILED withString:@"Cannot publish an empty message"];
            return;
        }
        // Release old publication
        [self unpublish];
        // Create new message
        GNSMessage *message = [GNSMessage messageWithContent: [messageString dataUsingEncoding: NSUTF8StringEncoding]];
        _publication = [[self sharedMessageManager] publicationWithMessage: message paramsBlock:^(GNSPublicationParams *params) {
            params.strategy = [GNSStrategy strategyWithParamsBlock:^(GNSStrategyParams *params) {
                params.discoveryMediums = _isBLEOnly ? kGNSDiscoveryMediumsBLE : kGNSDiscoveryModeDefault;
            }];
        }];
        [self sendEvent:PUBLISH_SUCCESS withString:[NSString stringWithFormat:@"Successfully published: %@", messageString]];
    } @catch(NSException *exception) {
        if(exception.reason != nil) {
            [self sendEvent:PUBLISH_FAILED withString: exception.reason];
        }
    }
}

RCT_EXPORT_METHOD(unpublish) {
    _publication = nil;
}

RCT_EXPORT_METHOD(isSubscribing:(RCTResponseSenderBlock) callback)
{
    if(_subscription != nil) {
        callback(@[@true, [NSNull null]]);
    } else {
        callback(@[@false, [NSNull null]]);
    }
}

RCT_EXPORT_METHOD(subscribe) {
    @try {
        if(![self isConnected]) {
            @throw [NSException
                    exceptionWithName:@"NotConnected"
                    reason:@"Messenger not connected. Call connect: before subscribing."
                    userInfo:nil];
        }
        // Release old _subscription
        [self unsubscribe];
        // Create _subscription object
        __weak NearbyModule *welf = self;
        _subscription = [[self sharedMessageManager] subscriptionWithMessageFoundHandler:^(GNSMessage *message) {
            [welf sendEvent:MESSAGE_FOUND withMessage:message];
        } messageLostHandler:^(GNSMessage *message) {
            [welf sendEvent:MESSAGE_LOST withMessage:message];
        } paramsBlock:^(GNSSubscriptionParams *params) {
            params.strategy = [GNSStrategy strategyWithParamsBlock:^(GNSStrategyParams *params) {
                params.allowInBackground = false; //TODO: Make this configurable
                params.discoveryMediums = _isBLEOnly ? kGNSDiscoveryMediumsBLE : kGNSDiscoveryModeDefault;
            }];
        }];
        [self sendEvent:SUBSCRIBE_SUCCESS withString:@"Successfully Subscribed."];
    } @catch(NSException *exception) {
        if(exception.reason != nil) {
            [self sendEvent:SUBSCRIBE_FAILED withString: exception.reason];
        }
    }
}

RCT_EXPORT_METHOD(unsubscribe) {
    _subscription = nil;
}

@end
