#import <CoreBluetooth/CoreBluetooth.h>

@interface BLEScanner : NSObject

@property (nonatomic, strong) CBCentralManager *centralManager;

- (void) scan;
- (void) stopScan;
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error;

@end
