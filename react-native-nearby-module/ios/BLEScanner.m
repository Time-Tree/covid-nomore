#import "BLEScanner.h"
#import "DBManager.h"

static DBManager *myDBManager;
long timestamp;

@implementation BLEScanner

- (instancetype) init {
    self = [super init];
    self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    timestamp = [[NSDate date] timeIntervalSince1970];
    [self centralManagerDidUpdateState:self.centralManager];
    myDBManager = [[DBManager alloc] init];
    return self;
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    switch (central.state) {
        case CBManagerStatePoweredOff:
            NSLog(@"CoreBluetooth BLE hardware is powered off");
            break;
        case CBManagerStatePoweredOn:{
            NSLog(@"CoreBluetooth BLE hardware is powered on and ready");
            break;
        }
        case CBManagerStateResetting:
            NSLog(@"CoreBluetooth BLE hardware is resetting");
            break;
        case CBManagerStateUnauthorized:
            NSLog(@"CoreBluetooth BLE state is unauthorized");
            break;
        case CBManagerStateUnknown:
            NSLog(@"CoreBluetooth BLE state is unknown");
            break;
        case CBManagerStateUnsupported:
            NSLog(@"CoreBluetooth BLE hardware is unsupported on this platform");
            break;
        default:
            break;
    }
}

- (void) scan {
    NSLog(@"Scanning started");
    NSDictionary *scanOptions = [NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:CBCentralManagerScanOptionAllowDuplicatesKey];
    [self.centralManager scanForPeripheralsWithServices:nil
                                                options:scanOptions];
}

- (void) stopScan {
    NSLog(@"Scanning stopped");
    [self.centralManager stopScan];
}


- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral
                        advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {
    if (peripheral.name) {
        long newDate =[[NSDate date] timeIntervalSince1970];
        long newTimestamp = newDate / 10 * 10;
        long trimTime = timestamp / 10 * 10;
        if (newTimestamp != trimTime) {
            timestamp = [[NSDate date] timeIntervalSince1970];
            NSLog(@"peripheral.name %@ RSSI %@  peripheral.services: %@", peripheral.name, RSSI, peripheral.services);
            NSString *messageString = [NSString stringWithFormat: @"NM: %@, RSSI: %@, SU: %@", peripheral.name, RSSI, peripheral.services];
            [self createEvent: @"BLE SCAN" withMessage:messageString];
        }
    }
    
}


- (void) createEvent:(nonnull NSString*)eventType withMessage:(nonnull NSString*) message {
    NSLog(@"-----> BLE createEvent: eventType=%@ message=%@", eventType, message);
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

@end
