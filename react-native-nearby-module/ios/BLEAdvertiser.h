#import <CoreBluetooth/CoreBluetooth.h>

@interface BLEAdvertiser : NSObject

@property (nonatomic, strong) CBPeripheralManager *peripheralManager;

- (void) startAdvertising;
- (void) stopAdvertising;
- (void) restartAdvertising;

@end
