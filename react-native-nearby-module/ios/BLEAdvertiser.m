#import "BLEAdvertiser.h"
#import "DBUtil.h"

static DBUtil *myDBUtil;
static NSString * const appIdentifier = @"a9ecdb59-974e-43f0-9d93-27d5dcb060d6";
static NSString *currentToken;
static NSMutableDictionary *handshakeDevices;
static int TOKEN_EXPIRATION = 5 * 60 * 1000.0;


@implementation BLEAdvertiser

- (instancetype) init {
    self = [super init];
    self.peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil options:@{CBPeripheralManagerOptionShowPowerAlertKey: @NO}];
    myDBUtil = [DBUtil sharedInstance];
    handshakeDevices = [[NSMutableDictionary alloc] init];
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

- (void) setToken {
    NSLog(@"setToken");
    NSDictionary *response = [myDBUtil getLastToken];
    NSString *token = [response valueForKey:@"token"];
    long created = [[response valueForKey:@"created"] longValue];
    
    NSTimeInterval timestamp = [[NSDate date] timeIntervalSince1970];
    long timestampMiliseconds = timestamp * 1000.0;
    if([token length] > 0 && created + TOKEN_EXPIRATION > timestampMiliseconds) {
        currentToken = token;
    } else {
        NSString *uuid = [[NSUUID UUID] UUIDString];
        [myDBUtil addToken:uuid];
        currentToken = uuid;
    }
}

- (void) restartAdvertising {
    [self.peripheralManager stopAdvertising];
    [self performSelector:@selector(startAdvertising) withObject:nil afterDelay:5];
    [handshakeDevices removeAllObjects];
}

- (void) startAdvertising {
    
    CBManagerState state = [self.peripheralManager state];
    if(state == CBManagerStateUnauthorized) {
        NSLog(@"Start advertising failed. CoreBluetooth BLE state is unauthorized.");
        NSString *message = [NSString stringWithFormat: @"Start advertising failed: CoreBluetooth BLE state is unauthorized."];
        [myDBUtil createEvent: @"BLE_ADVERTISER_ERROR" withMessage:message];
    }
    if(self.peripheralManager.isAdvertising) {
        NSLog(@"Already advertising");
//        NSString *message = [NSString stringWithFormat: @"Start advertising failed: Already advertising."];
//        [myDBUtil createEvent: @"BLE_ADVERTISER_ERROR" withMessage:message];
        return;
    }
    [self.peripheralManager removeAllServices];
    [self setToken];
    CBMutableCharacteristic *characteristic = [[CBMutableCharacteristic alloc] initWithType:[CBUUID UUIDWithString:currentToken]
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
    [myDBUtil createEvent: @"BLE_ADVERTISER" withMessage:@"Advertising stopped"];
    [handshakeDevices removeAllObjects];
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
        NSString *message = [NSString stringWithFormat: @"Start advertising success with UUID: %@", currentToken];
        [myDBUtil createEvent: @"BLE_ADVERTISER" withMessage:message];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didModifyServices:(NSArray<CBService *> *)invalidatedServices {
    NSLog(@"in advertiser didModifyServices: %@", peripheral.services);
}


- (void)peripheralManager:(CBPeripheralManager *)peripheral didReceiveReadRequest:(CBATTRequest *)request {
    NSLog(@"didReceiveReadRequest: %@ %@", request.central, request.characteristic.UUID);
    request.value = [currentToken dataUsingEncoding:NSUTF8StringEncoding];
    [self.peripheralManager respondToRequest:request withResult:CBATTErrorSuccess];
    NSNumber *handshakeTime = [handshakeDevices objectForKey:request.central.identifier];
    if(!handshakeTime) {
        NSString *message = [NSString stringWithFormat: @"Device %@ tried to read my characteristic", request.central.identifier];
        [myDBUtil createEvent: @"BLE_ADVERTISER" withMessage:message];
        [myDBUtil updateTokenUsed:currentToken];
        [handshakeDevices setObject:[NSNumber numberWithLong:[[NSDate date] timeIntervalSince1970]] forKey:request.central.identifier];
    }
}

@end
