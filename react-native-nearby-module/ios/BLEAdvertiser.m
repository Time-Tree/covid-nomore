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
    switch (peripheral.state) {
        case CBManagerStatePoweredOff:
            string = @"CoreBluetooth BLE hardware is powered off.";
            break;
        case CBManagerStatePoweredOn:
            string = @"CoreBluetooth BLE hardware is powered on and ready.";
            break;
        case CBManagerStateUnauthorized:
            string = @"CoreBluetooth BLE state is unauthorized.";
            break;
        case CBManagerStateUnknown:
            string = @"CoreBluetooth BLE state is unknown.";
            break;
        case CBManagerStateUnsupported:
            string = @"CoreBluetooth BLE hardware is unsupported on this platform.";
            break;
        default:
            break;
    }
    NSLog(@"CBManagerState: %@", string);
}

- (void) startAdvertising {
    [self stopAdvertising];
    
    CBManagerState state = [self.peripheralManager state];
    if(state == CBManagerStateUnauthorized) {
        NSLog(@"Start advertising failed. CoreBluetooth BLE state is unauthorized.");
        [myDBUtil createEvent: @"Start advertising failed" withMessage:@"CoreBluetooth BLE state is unauthorized."];
        return;
    }
    [self.peripheralManager removeAllServices];
    if(self.peripheralManager.isAdvertising) {
        NSLog(@"Already advertising");
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
        [myDBUtil createEvent: @"Error advertising" withMessage:[error localizedDescription]];
    } else {
        NSLog(@"Start advertising success");
        NSString *message = [NSString stringWithFormat: @"UUID: %@", uniqueIdentifier];
        [myDBUtil createEvent: @"Start advertising success" withMessage:message];
    }
}

-(void) stopAdvertising {
    NSLog(@"stopAdvertising");
    [self.peripheralManager stopAdvertising];
}

@end
