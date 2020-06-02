#import "BLEAdvertiser.h"
#import "DBUtil.h"

static DBUtil *myDBUtil;
static NSString * const appIdentifier = @"a9ecdb59-974e-43f0-9d93-27d5dcb060d6";
static NSString *uniqueIdentifier;

@implementation BLEAdvertiser

- (instancetype) init {
    self = [super init];
    self.peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil options:@{CBPeripheralManagerOptionShowPowerAlertKey: @NO}];
    myDBUtil = [[DBUtil alloc] init];
    uniqueIdentifier = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
    return self;
}

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
    NSString *string = @"Unknown state";
    NSString *message = @"";
    switch (peripheral.state) {
        case CBManagerStatePoweredOff:
            string = @"CoreBluetooth BLE hardware is powered off.";
            message = string;
            break;
        case CBManagerStatePoweredOn:
            string = @"CoreBluetooth BLE hardware is powered on and ready.";
            break;
        case CBManagerStateUnauthorized:
            string = @"CoreBluetooth BLE state is unauthorized.";
            message = string;
            break;
        case CBManagerStateUnknown:
            string = @"CoreBluetooth BLE state is unknown.";
            message = string;
            break;
        case CBManagerStateUnsupported:
            string = @"CoreBluetooth BLE hardware is unsupported on this platform.";
            message = string;
            break;
        default:
            break;
    }
    NSLog(@"CBManagerState: %@", string);
    if(![message isEqual:@""]) {
        [myDBUtil createEvent: @"BLE_ADVERTISER_ERROR" withMessage:message];
    }
}

- (void) restartAdvertising {
    [self.peripheralManager stopAdvertising];
    [self performSelector:@selector(startAdvertising) withObject:nil afterDelay:5];
}

- (void) startAdvertising {
    
    CBManagerState state = [self.peripheralManager state];
    if(state == CBManagerStateUnauthorized) {
        NSLog(@"Start advertising failed. CoreBluetooth BLE state is unauthorized.");
    }
    [self.peripheralManager removeAllServices];
    if(self.peripheralManager.isAdvertising) {
        NSLog(@"Already advertising");
        [myDBUtil createEvent: @"BLE_ADVERTISER_ERROR" withMessage:message];
        return;
    }
    CBMutableCharacteristic *characteristic = [[CBMutableCharacteristic alloc] initWithType:[CBUUID UUIDWithString:uniqueIdentifier]
                                                                                 properties:CBCharacteristicPropertyRead
                                                                                      value:nil
                                                                                permissions:CBAttributePermissionsReadable];
    
    CBMutableService *service = [[CBMutableService alloc] initWithType:[CBUUID UUIDWithString:appIdentifier] primary:YES];
    [service setCharacteristics:@[characteristic]];
    [self.peripheralManager addService:service];
}

- (void) stopAdvertising {
    NSLog(@"stopAdvertising");
    [self.peripheralManager stopAdvertising];
}

- (void) peripheralManager:(CBPeripheralManager *)peripheral didAddService:(CBService *)service error:(NSError *)error {
    NSLog(@"peripheralManagerDidAddService");
    if (error) {
        NSLog(@"Error publishing service: %@", [error localizedDescription]);
        [myDBUtil createEvent: @"Error publishing service" withMessage:[error localizedDescription]];
    } else {
        NSString *deviceName = [[UIDevice currentDevice] name];
        [self.peripheralManager startAdvertising:@{
            CBAdvertisementDataLocalNameKey: deviceName,
            CBAdvertisementDataServiceUUIDsKey: @[[CBUUID UUIDWithString:appIdentifier]]
        }];
    }
}

- (void)peripheralManagerDidStartAdvertising:(CBPeripheralManager *)peripheral error:(NSError *)error {
    NSLog(@"peripheralManagerDidStartAdvertising: %@", peripheral.description);
    if (error) {
        NSLog(@"Error advertising: %@", [error localizedDescription]);
        NSString *message = [NSString stringWithFormat: @"Error advertising: %@", [error localizedDescription]];
        [myDBUtil createEvent: @"BLE_ADVERTISER_ERROR" withMessage:message];
    } else {
        NSLog(@"Start advertising success");
        NSString *message = [NSString stringWithFormat: @"Start advertising success with UUID: %@", uniqueIdentifier];
        [myDBUtil createEvent: @"BLE_ADVERTISER" withMessage:message];
    }
}


@end
