
static const NSString *DATABASE_NAME = @"NearbyEvents.db";
static const NSString *EVENTS_TABLE_NAME = @"NearbyEvents";
static const NSString *COLUMN_NAME_EVENT_TYPE = @"eventType";
static const NSString *COLUMN_NAME_MESSAGE = @"message";
static const NSString *COLUMN_NAME_FORMAT_DATE = @"formatDate";
static const NSString *COLUMN_NAME_TIMESTAMP = @"timestamp";

static const NSString *SETTINGS_TABLE_NAME = @"Settings";
static const NSString *COLUMN_NAME_BLE_PROCESS = @"bleProcess";
static const NSString *COLUMN_NAME_BLE_INTEVAL = @"bleInterval";
static const NSString *COLUMN_NAME_BLE_DURATION = @"bleDuration";
static const NSString *COLUMN_NAME_NEARBY_PROCESS = @"nearbyProcess";
static const NSString *COLUMN_NAME_NEARBY_INTEVAL = @"nearbyInterval";
static const NSString *COLUMN_NAME_NEARBY_DURATION = @"nearbyDuration";

static const NSString *TOKENS_TABLE_NAME = @"Tokens";
static const NSString *COLUMN_NAME_TOKEN = @"token";
static const NSString *COLUMN_NAME_CREATED = @"created";
static const NSString *COLUMN_NAME_USED = @"used";

static const NSString *HANDSHAKES_TABLE_NAME = @"Handshakes";
static const NSString *COLUMN_NAME_HANDSHAKE_TOKEN = @"token";
static const NSString *COLUMN_NAME_HANDSHAKE_DISCOVERED = @"discovered";
static const NSString *COLUMN_NAME_HANDSHAKE_RSSI = @"rssi";
static const NSString *COLUMN_NAME_HANDSHAKE_DATA = @"characteristicData";

@interface DBManager : NSObject

@property(nonatomic, strong) NSString *databasePath;

+ (id) sharedInstance;
- (void) saveEventData: (NSDictionary *) event;
- (void) deleteAllData;
- (NSDictionary *) getSettingsData;
- (void) saveToken: (NSDictionary *) data;
- (NSDictionary *) getLastToken;
- (void) saveHandshake: (NSDictionary *) data;
- (void) updateTokenUsed: (NSString *) token;

@end
