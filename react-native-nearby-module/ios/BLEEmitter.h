#import <CoreBluetooth/CoreBluetooth.h>

@interface BLEEmitter : NSObject

@property (nonatomic, strong) CBPeripheralManager *peripheralManager;

- (void) startAdvertising: (NSString *)serviceUUID;
- (void) stopAdvertising;

@end
