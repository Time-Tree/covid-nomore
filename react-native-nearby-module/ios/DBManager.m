#import "DBManager.h"
#import <sqlite3.h>

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
static sqlite3 *sqlite3Database;

@implementation DBManager

- (instancetype) init {
    self = [super init];
    [self createEventsTable];
    [self createSettingsTable];
    [self insertDefaultSettingsData];
    return self;
}

- (void) createEventsTable {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    self.databasePath =  [documentsDirectory stringByAppendingPathComponent:@"NearbyEvents.sqlite"];
    NSFileManager *filemgr = [NSFileManager defaultManager];
    if ([filemgr fileExistsAtPath: self.databasePath] == NO) {
        const char *dbPath = [self.databasePath UTF8String];
        if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
            NSString *createTableSQL = [NSString stringWithFormat: @"CREATE TABLE IF NOT EXISTS %@ (ID integer primary key AUTOINCREMENT, %@ text, %@ text, %@ text, %@ text)", EVENTS_TABLE_NAME, COLUMN_NAME_EVENT_TYPE, COLUMN_NAME_MESSAGE, COLUMN_NAME_FORMAT_DATE, COLUMN_NAME_TIMESTAMP];
            char *errMsg;
            const char *sql_stmt = [createTableSQL UTF8String];
            if (sqlite3_exec(sqlite3Database, sql_stmt, NULL, NULL, &errMsg) != SQLITE_OK) {
                NSLog(@"Failed to create table: %s", sqlite3_errmsg(sqlite3Database));
            } else {
                NSLog(@"%@ table created", EVENTS_TABLE_NAME);
            }
            sqlite3_close(sqlite3Database);
        } else {
            NSLog(@"Failed to open/create database: %s", sqlite3_errmsg(sqlite3Database));
        }
    } else {
        NSLog(@"Database already exists at filePath: %@", self.databasePath);
    }
}


- (void) saveEventData: (NSDictionary *) event {
    const char *dbPath = [self.databasePath UTF8String];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *insertSQL = [NSString stringWithFormat: @"INSERT INTO %@ (%@, %@, %@, %@) VALUES (\"%@\", \"%@\", \"%@\", \"%@\")",EVENTS_TABLE_NAME, COLUMN_NAME_EVENT_TYPE, COLUMN_NAME_MESSAGE, COLUMN_NAME_FORMAT_DATE, COLUMN_NAME_TIMESTAMP, event[@"eventType"], event[@"message"], event[@"formatDate"], event[@"timestamp"]];
        
        const char *insert_stmt = [insertSQL UTF8String];
        sqlite3_stmt  *statement;
        sqlite3_prepare_v2(sqlite3Database, insert_stmt, -1, &statement, NULL);
        if (sqlite3_step(statement) == SQLITE_DONE) {
            NSLog(@"Event added");
        } else {
            NSLog(@"Failed to add event: %s", sqlite3_errmsg(sqlite3Database));
        }
        sqlite3_finalize(statement);
        sqlite3_close(sqlite3Database);
    } else {
        NSLog(@"Database is closed: %s", sqlite3_errmsg(sqlite3Database));
    }
}

- (void) deleteAllData {
    const char *dbPath = [self.databasePath UTF8String];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *deleteSQL = [NSString stringWithFormat: @"DELETE FROM %@", EVENTS_TABLE_NAME];
        const char *delete_stmt = [deleteSQL UTF8String];
        sqlite3_stmt  *statement;
        
        sqlite3_prepare_v2(sqlite3Database, delete_stmt, -1, &statement, NULL);
        if (sqlite3_step(statement) == SQLITE_DONE) {
            NSLog(@"Events were successfully deleted");
        } else {
            NSLog(@"Failed to delete all events: %s", sqlite3_errmsg(sqlite3Database));
        }
        sqlite3_finalize(statement);
        sqlite3_close(sqlite3Database);
    }
}

- (void) createSettingsTable {
    const char *dbPath = [self.databasePath UTF8String];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *createTableSQL = [NSString stringWithFormat: @"CREATE TABLE IF NOT EXISTS %@ (_ID integer primary key AUTOINCREMENT, %@ integer, %@ integer, %@ integer, %@ integer, %@ integer, %@ integer)", SETTINGS_TABLE_NAME, COLUMN_NAME_BLE_PROCESS, COLUMN_NAME_BLE_INTEVAL, COLUMN_NAME_BLE_DURATION, COLUMN_NAME_NEARBY_PROCESS, COLUMN_NAME_NEARBY_INTEVAL, COLUMN_NAME_NEARBY_DURATION];
        char *errMsg;
        const char *sql_stmt = [createTableSQL UTF8String];
        if (sqlite3_exec(sqlite3Database, sql_stmt, NULL, NULL, &errMsg) != SQLITE_OK) {
            NSLog(@"Failed to create table: %s", sqlite3_errmsg(sqlite3Database));
        } else {
            NSLog(@"%@ table created", SETTINGS_TABLE_NAME);
        }
        sqlite3_close(sqlite3Database);
    } else {
        NSLog(@"Failed to open database: %s", sqlite3_errmsg(sqlite3Database));
    }
}

- (void) insertDefaultSettingsData {
    const char *dbPath = [self.databasePath UTF8String];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSLog(@"insertDefaultSettingsData");
        NSString *selectSQL = [NSString stringWithFormat: @"SELECT * FROM \"%@\"", SETTINGS_TABLE_NAME];
        const char *select_stmt = [selectSQL UTF8String];
        sqlite3_stmt  *select_statement;
        sqlite3_prepare_v2(sqlite3Database, select_stmt, -1, &select_statement, NULL);
        if (sqlite3_step(select_statement) == SQLITE_DONE) {
            Boolean recordExist = false;
            while (sqlite3_step(select_statement) == SQLITE_ROW) {
                recordExist = true;
                return;
            }
            if(recordExist) {
                NSLog(@"recordExist");
            } else {
                NSString *insertSQL = [NSString stringWithFormat: @"INSERT INTO %@ (%@, %@, %@, %@, %@, %@) VALUES (1, 3, 1, 1, 3, 1)", SETTINGS_TABLE_NAME, COLUMN_NAME_BLE_PROCESS, COLUMN_NAME_BLE_INTEVAL, COLUMN_NAME_BLE_DURATION, COLUMN_NAME_NEARBY_PROCESS, COLUMN_NAME_NEARBY_INTEVAL, COLUMN_NAME_NEARBY_DURATION];
                const char *insert_stmt = [insertSQL UTF8String];
                sqlite3_stmt  *insert_statement;
                sqlite3_prepare_v2(sqlite3Database, insert_stmt, -1, &insert_statement, NULL);
                if (sqlite3_step(insert_statement) == SQLITE_DONE) {
                    NSLog(@"Default settings created");
                } else {
                    NSLog(@"Failed to add default settings: %s", sqlite3_errmsg(sqlite3Database));
                }
                sqlite3_finalize(insert_statement);
            }
        }
        sqlite3_finalize(select_statement);
        sqlite3_close(sqlite3Database);
    } else {
        NSLog(@"Database is closed: %s", sqlite3_errmsg(sqlite3Database));
    }
}

- (NSDictionary *) getSettingsData {
    NSLog(@"getSettingsData");
    const char *dbPath = [self.databasePath UTF8String];
    NSDictionary *response = [[NSDictionary alloc] init];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *selectSQL = [NSString stringWithFormat: @"SELECT * FROM \"%@\" WHERE _ID = 1", SETTINGS_TABLE_NAME];
        const char *select_stmt = [selectSQL UTF8String];
        sqlite3_stmt  *select_statement;
        if (sqlite3_prepare_v2(sqlite3Database, select_stmt, -1, &select_statement, NULL) == SQLITE_OK) {
            while(sqlite3_step(select_statement) == SQLITE_ROW) {
                int bleProcess =  sqlite3_column_int(select_statement, 1);
                int bleInterval =  sqlite3_column_int(select_statement, 2);
                int bleDuration =  sqlite3_column_int(select_statement, 3);
                int nearbyProcess =  sqlite3_column_int(select_statement, 4);
                int nearbyInterval =  sqlite3_column_int(select_statement, 5);
                int nearbyDuration =  sqlite3_column_int(select_statement, 6);
                response = @{
                    @"bleProcess": [NSNumber numberWithInt:bleProcess],
                    @"bleInterval": [NSNumber numberWithInt:bleInterval],
                    @"bleDuration": [NSNumber numberWithInt:bleDuration],
                    @"nearbyProcess": [NSNumber numberWithInt:nearbyProcess],
                    @"nearbyInterval": [NSNumber numberWithInt:nearbyInterval],
                    @"nearbyDuration": [NSNumber numberWithInt:nearbyDuration]
                };
                break;
            }
        }
        sqlite3_finalize(select_statement);
        sqlite3_close(sqlite3Database);
    } else {
        NSLog(@"Database is closed: %s", sqlite3_errmsg(sqlite3Database));
    }
    return response;
}

@end
