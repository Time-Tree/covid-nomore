#import "BLEScanner.h"
#import "DBUtil.h"

static DBUtil *myDBUtil;
static NSString * const appIdentifier = @"a9ecdb59-974e-43f0-9d93-27d5dcb060d6";
static NSString *uniqueIdentifier;
static CBPeripheral * discoveredPeripheral;
static NSMutableDictionary *foundDevices;
static NSMutableDictionary *connectedDevices;

@implementation BLEScanner

- (instancetype) init {
    self = [super init];
    self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    [self centralManagerDidUpdateState:self.centralManager];
    myDBUtil = [DBUtil sharedInstance];
    uniqueIdentifier = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
    foundDevices = [[NSMutableDictionary alloc] init];
    connectedDevices = [[NSMutableDictionary alloc] init];
    return self;
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    NSString *string = @"Unknown state";
    NSString *message = @"";
    switch (central.state) {
        case CBManagerStatePoweredOff:
            string = @"CoreBluetooth BLE hardware is powered off";
            message = string;
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
            message = string;
            break;
        case CBManagerStateUnknown:
            string = @"CoreBluetooth BLE state is unknown";
            break;
        case CBManagerStateUnsupported:
            string = @"CoreBluetooth BLE hardware is unsupported on this platform";
            message = string;
            break;
        default:
            break;
    }
    NSLog(@"CBManagerState: %@", string);
    if(![message isEqual:@""]) {
        [myDBUtil createEvent: @"BLE_SCANNER_ERROR" withMessage:message];
    }
}

- (void) scan {
    [self.centralManager stopScan];
    NSLog(@"Scanning started");
    NSDictionary *options = [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithBool:YES], CBCentralManagerScanOptionAllowDuplicatesKey, nil];
//    NSDictionary *scanOptions = [NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:CBCentralManagerScanOptionAllowDuplicatesKey];
    [self.centralManager scanForPeripheralsWithServices:@[[CBUUID UUIDWithString:appIdentifier]]
                                                options:options];
    [myDBUtil createEvent: @"BLE_SCANNER" withMessage:@"Scanning started"];
}

- (void) stopScan {
    NSLog(@"Scanning stopped");
    [self.centralManager stopScan];
    [foundDevices removeAllObjects];
    [myDBUtil createEvent: @"BLE_SCANNER" withMessage:@"Scanning stopped"];
}


- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral
      advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {
    NSNumber *deviceTime = [foundDevices objectForKey:peripheral.identifier];
    if (deviceTime) {
        return;
    }
    long timestamp = [[NSDate date] timeIntervalSince1970];
    [foundDevices setObject:[NSNumber numberWithLong:timestamp] forKey:peripheral.identifier];
    discoveredPeripheral = peripheral;
    [central connectPeripheral:peripheral options:nil];
}

- (void) centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    NSLog(@"didConnectPeripheral peripheral: %@ identifier: %@", peripheral.name, peripheral.identifier);
    peripheral.delegate = self;
    if(peripheral.services)
        [self peripheral:peripheral didDiscoverServices:nil];
    else
        [peripheral discoverServices:@[[CBUUID UUIDWithString:appIdentifier]]];
}

- (void) centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    NSLog(@"didFailToConnectPeripheral peripheral:  %@ identifier: %@", peripheral.name, peripheral.identifier);
    [foundDevices removeObjectForKey:peripheral.identifier];
}

- (void) peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {
    NSLog(@"didDiscoverServices services:  %@ identifier: %@ services: %@", peripheral.services, peripheral.identifier, peripheral.services);
    if (error != nil) {
        [foundDevices removeObjectForKey:peripheral.identifier];
        return;
    }
    for (CBService *service in peripheral.services) {
        if( [service.UUID isEqual:[CBUUID UUIDWithString:appIdentifier]]) {
            if(service.characteristics)
                [self peripheral:peripheral didDiscoverCharacteristicsForService:service error:nil];
            else
                [peripheral discoverCharacteristics:nil forService:service];
        }
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    if (error != nil) {
        [foundDevices removeObjectForKey:peripheral.identifier];
        return;
    }
    for (CBCharacteristic *characteristic in service.characteristics) {
        NSLog(@"Discovered characteristic %@", characteristic);
        NSString *uuid = [NSString stringWithFormat: @"%@", characteristic.UUID];
        NSMutableDictionary *device = [[NSMutableDictionary alloc] init];
        device[@"token"] = uuid;
        device[@"characteristicData"] = @0;
        device[@"rssi"] = @"-";
        [connectedDevices setObject:device forKey:peripheral.identifier];
        [peripheral readRSSI];
        [peripheral setNotifyValue:YES forCharacteristic:characteristic];
        [peripheral readValueForCharacteristic:(CBCharacteristic *)characteristic];
//        [self.centralManager cancelPeripheralConnection:peripheral];
//        discoveredPeripheral = nil;
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    if (error) {
        NSLog(@"Error didUpdateValueForCharacteristic");
        return;
    }
    NSLog(@"didUpdateValueForCharacteristic %@", characteristic);
    NSString *stringFromData = [[NSString alloc] initWithData:characteristic.value encoding:NSUTF8StringEncoding];
    NSString *message = [NSString stringWithFormat: @"Characteristic data found. ID: %@", stringFromData];
    [myDBUtil createEvent: @"BLE_DATA_FOUND" withMessage:message];
    NSMutableDictionary *device = [connectedDevices objectForKey:peripheral.identifier];
    [device setValue:@1 forKey:@"characteristicData"];
    [myDBUtil addHandshake:device];
    [connectedDevices removeObjectForKey:peripheral.identifier];
}

- (void)peripheral:(CBPeripheral *)peripheral didModifyServices:(NSArray<CBService *> *)invalidatedServices {
    NSLog(@"in scanner didModifyServices: %@", peripheral.services);
    if(peripheral.services)
        [self peripheral:peripheral didDiscoverServices:nil];
    else
        [peripheral discoverServices:@[[CBUUID UUIDWithString:appIdentifier]]];
}

- (void)peripheral:(CBPeripheral *)peripheral didReadRSSI:(NSNumber *)RSSI error:(NSError *)error {
    NSLog(@"didReadRSSI RSSI:  %@ identifier: %@", RSSI, peripheral.identifier);
    NSMutableDictionary *device = [connectedDevices objectForKey:peripheral.identifier];
    NSString *message = [NSString stringWithFormat: @"ID: %@", [device valueForKey:@"token"]];
    if (error == nil) {
        [device setValue:RSSI forKey:@"rssi"];
        message = [NSString stringWithFormat: @"%@, RSSI: %@", message, RSSI];
    }
    [myDBUtil createEvent: @"BLE_FOUND" withMessage:message];
    [myDBUtil addHandshake:device];
//    [connectedDevices removeObjectForKey:peripheral.identifier];
}

@end
