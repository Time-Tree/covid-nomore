#import "Service.h"
#import "DBUtil.h"
#import "BLEScanner.h"
#import "BLEAdvertiser.h"
#import "NearbyManager.h"
#import <GNSMessages.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import <BackgroundTasks/BackgroundTasks.h>
#include <stdlib.h>

static CBCentralManager *myCentralManager;
static DBUtil *myDBUtil;
static BLEScanner *myBLEScanner;
static BLEAdvertiser *myBLEAdvertiser;
static NearbyManager *myNearbyManager;
static int code;

static const int BLE_REPEATS_INTERVAL = 3 * 60.0; //minutes
static const int BLE_PAUSE_TIME = 1 * 60.0;
static const int NEARBY_REPEATS_INTERVAL = 5 * 60.0;
static const int NEARBY_PAUSE_TIME = 2 * 60.0;

@implementation Service

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
        myNearbyManager = [[NearbyManager alloc] init];
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
    [myNearbyManager startService: apiKey];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self nearbyTimerTask];
        [self bleTimerTask];
    });
}

- (void) nearbyTimerTask {
    [self startNearbyTimerTask];
    self.nearbyStartTimer = [NSTimer scheduledTimerWithTimeInterval: NEARBY_REPEATS_INTERVAL
                                                             target: self
                                                           selector: @selector(startNearbyTimerTask)
                                                           userInfo: nil repeats:YES];
}

- (void) bleTimerTask {
    self.bleStartTimer = [NSTimer scheduledTimerWithTimeInterval: BLE_REPEATS_INTERVAL
                                                          target: self
                                                        selector: @selector(startBLETimerTask)
                                                        userInfo: nil repeats:YES];
}

- (void) startNearbyTimerTask {
    code = 1000 + arc4random_uniform(9000);
    NSLog(@"code = %d", code);
    [myNearbyManager checkAndConnect];
    [myNearbyManager subscribe];
    [myNearbyManager publish: code];
    [self sendNotification:@"Start nearby timer task"];
    self.nearbyStopTimer = [NSTimer scheduledTimerWithTimeInterval: NEARBY_PAUSE_TIME
                                                            target: self
                                                          selector: @selector(stopNearbyTimerTask)
                                                          userInfo: nil repeats:NO];
}

- (void) stopNearbyTimerTask {
    NSLog(@"stopTimer");
    [self sendNotification:@"STOP nearby timer task"];
    [myNearbyManager unpublish];
    [myNearbyManager unsubscribe];
}

- (void) startBLETimerTask {
    NSLog(@"startBLETimerTask");
    [myBLEScanner scan];
    [myBLEAdvertiser startAdvertising];
    [self sendNotification:@"Start BLE timer task"];
    self.bleStopTimer = [NSTimer scheduledTimerWithTimeInterval: BLE_PAUSE_TIME
                                                         target: self
                                                       selector: @selector(stopBLETimerTask)
                                                       userInfo: nil repeats:NO];
}

- (void) stopBLETimerTask {
    NSLog(@"stopBLETimerTask");
    [myBLEScanner stopScan];
    [myBLEAdvertiser stopAdvertising];
    [self sendNotification:@"STOP BLE timer task"];
}

- (void) deleteAllData {
    [myDBUtil deleteAllData];
}

- (void) sendNotification:(NSString*) message {
    UILocalNotification *localNotification = [[UILocalNotification alloc] init];
    localNotification.alertBody = message;
    [[UIApplication sharedApplication] presentLocalNotificationNow:localNotification];
}

@end