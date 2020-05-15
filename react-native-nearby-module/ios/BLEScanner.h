#import <CoreBluetooth/CoreBluetooth.h>

@interface BLEScanner : NSObject

@property (nonatomic, strong) CBCentralManager *centralManager;

- (void) scan;
- (void) stopScan;

@end
