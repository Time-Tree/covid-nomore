#import "BLEScanner.h"
#import "DBUtil.h"

static DBUtil *myDBUtil;
long timestamp;

@implementation BLEScanner

- (instancetype) init {
    self = [super init];
    self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    timestamp = [[NSDate date] timeIntervalSince1970];
    [self centralManagerDidUpdateState:self.centralManager];
    myDBUtil = [[DBUtil alloc] init];
    return self;
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    NSString *string = @"Unknown state";
    switch (central.state) {
        case CBManagerStatePoweredOff:
            string = @"CoreBluetooth BLE hardware is powered off";
            break;
        case CBManagerStatePoweredOn:{
            string = @"CoreBluetooth BLE hardware is powered on and ready";
            break;
        }
        case CBManagerStateResetting:
            string = @"CoreBluetooth BLE hardware is resetting";
            break;
        case CBManagerStateUnauthorized:
            string = @"CoreBluetooth BLE state is unauthorized";
            break;
        case CBManagerStateUnknown:
            string = @"CoreBluetooth BLE state is unknown";
            break;
        case CBManagerStateUnsupported:
            string = @"CoreBluetooth BLE hardware is unsupported on this platform";
            break;
        default:
            break;
    }
    NSLog(@"CBManagerState: %@", string);
}

- (void) scan {
    [self stopScan];
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
            [myDBUtil createEvent: @"BLE SCAN" withMessage:messageString];
        }
    }
    
}

@end
