#import "DBManager.h"
#import <sqlite3.h>

static sqlite3 *sqlite3Database;

@implementation DBManager

+ (id) sharedInstance {
    static DBManager *sharedInstance = nil;
    @synchronized(self) {
        if (sharedInstance == nil)
            sharedInstance = [[self alloc] init];
    }
    return sharedInstance;
}

- (instancetype) init {
    self = [super init];
    [self createEventsTable];
    [self createSettingsTable];
    [self insertDefaultSettingsData];
    [self createTokensTable];
    [self createHandshakesTable];
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

- (void) createTokensTable {
    const char *dbPath = [self.databasePath UTF8String];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *createTableSQL = [NSString stringWithFormat: @"CREATE TABLE IF NOT EXISTS %@ (_ID integer primary key AUTOINCREMENT, %@ text, %@ integer, %@ integer)", TOKENS_TABLE_NAME, COLUMN_NAME_TOKEN, COLUMN_NAME_CREATED, COLUMN_NAME_USED];
        char *errMsg;
        const char *sql_stmt = [createTableSQL UTF8String];
        if (sqlite3_exec(sqlite3Database, sql_stmt, NULL, NULL, &errMsg) != SQLITE_OK) {
            NSLog(@"Failed to create table: %s", sqlite3_errmsg(sqlite3Database));
        } else {
            NSLog(@"%@ table created", TOKENS_TABLE_NAME);
        }
        sqlite3_close(sqlite3Database);
    } else {
        NSLog(@"Failed to open database: %s", sqlite3_errmsg(sqlite3Database));
    }
}

- (void) saveToken: (NSDictionary *) data {
    const char *dbPath = [self.databasePath UTF8String];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *insertSQL = [NSString stringWithFormat: @"INSERT INTO %@ (%@, %@, %@) VALUES (\"%@\", %@, %@)", TOKENS_TABLE_NAME, COLUMN_NAME_TOKEN, COLUMN_NAME_CREATED, COLUMN_NAME_USED, data[@"token"], data[@"created"], data[@"used"]];
        
        const char *insert_stmt = [insertSQL UTF8String];
        sqlite3_stmt  *statement;
        sqlite3_prepare_v2(sqlite3Database, insert_stmt, -1, &statement, NULL);
        if (sqlite3_step(statement) == SQLITE_DONE) {
            NSLog(@"Token added");
        } else {
            NSLog(@"Failed to add Token: %s", sqlite3_errmsg(sqlite3Database));
        }
        sqlite3_finalize(statement);
        sqlite3_close(sqlite3Database);
    } else {
        NSLog(@"Database is closed: %s", sqlite3_errmsg(sqlite3Database));
    }
}

- (void) updateTokenUsed: (NSString *) token {
    const char *dbPath = [self.databasePath UTF8String];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *updateSQL = [NSString stringWithFormat: @"UPDATE %@ SET %@ = 1 WHERE  %@ = \"%@\"", TOKENS_TABLE_NAME, COLUMN_NAME_USED, COLUMN_NAME_TOKEN, token];
        
        const char *update_stmt = [updateSQL UTF8String];
        sqlite3_stmt  *statement;
        sqlite3_prepare_v2(sqlite3Database, update_stmt, -1, &statement, NULL);
        if (sqlite3_step(statement) == SQLITE_DONE) {
            NSLog(@"Token updated");
        } else {
            NSLog(@"Failed to update token: %s", sqlite3_errmsg(sqlite3Database));
        }
        sqlite3_finalize(statement);
        sqlite3_close(sqlite3Database);
    } else {
        NSLog(@"Database is closed: %s", sqlite3_errmsg(sqlite3Database));
    }
}


- (NSDictionary *) getLastToken {
    NSLog(@"getLastToken");
    const char *dbPath = [self.databasePath UTF8String];
    NSDictionary *response = [[NSDictionary alloc] init];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *selectSQL = [NSString stringWithFormat: @"SELECT \"%@\", \"%@\" FROM \"%@\" ORDER BY \"%@\" DESC LIMIT 1;", COLUMN_NAME_TOKEN, COLUMN_NAME_CREATED, TOKENS_TABLE_NAME, COLUMN_NAME_CREATED];
        NSLog(@"selectSQL = %@", selectSQL);
        const char *select_stmt = [selectSQL UTF8String];
        sqlite3_stmt  *select_statement;
        if (sqlite3_prepare_v2(sqlite3Database, select_stmt, -1, &select_statement, NULL) == SQLITE_OK) {
            while(sqlite3_step(select_statement) == SQLITE_ROW) {
                const unsigned char *token =  sqlite3_column_text(select_statement, 0);
                double created =  sqlite3_column_double(select_statement, 1);
                response = @{
                   @"token": [NSString stringWithUTF8String:(char *)token],
                   @"created": [NSNumber numberWithDouble:created]
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

- (void) createHandshakesTable {
    const char *dbPath = [self.databasePath UTF8String];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *createTableSQL = [NSString stringWithFormat: @"CREATE TABLE IF NOT EXISTS %@ (_ID integer primary key AUTOINCREMENT, %@ text UNIQUE, %@ integer, %@ text, %@ integer)", HANDSHAKES_TABLE_NAME, COLUMN_NAME_HANDSHAKE_TOKEN, COLUMN_NAME_HANDSHAKE_DISCOVERED, COLUMN_NAME_HANDSHAKE_RSSI, COLUMN_NAME_HANDSHAKE_DATA];
        char *errMsg;
        const char *sql_stmt = [createTableSQL UTF8String];
        if (sqlite3_exec(sqlite3Database, sql_stmt, NULL, NULL, &errMsg) != SQLITE_OK) {
            NSLog(@"Failed to create table: %s", sqlite3_errmsg(sqlite3Database));
        } else {
            NSLog(@"%@ table created", TOKENS_TABLE_NAME);
        }
        sqlite3_close(sqlite3Database);
    } else {
        NSLog(@"Failed to open database: %s", sqlite3_errmsg(sqlite3Database));
    }
}

- (void) saveHandshake: (NSDictionary *) data {
    const char *dbPath = [self.databasePath UTF8String];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *insertSQL = [NSString stringWithFormat: @"INSERT OR REPLACE INTO %@ (%@, %@ ,%@, %@) VALUES (\"%@\", %@, \"%@\", %@)", HANDSHAKES_TABLE_NAME, COLUMN_NAME_HANDSHAKE_TOKEN, COLUMN_NAME_HANDSHAKE_DISCOVERED, COLUMN_NAME_HANDSHAKE_RSSI, COLUMN_NAME_HANDSHAKE_DATA, data[@"token"], data[@"discovered"], data[@"rssi"], data[@"characteristicData"]];
        
        const char *insert_stmt = [insertSQL UTF8String];
        sqlite3_stmt  *statement;
        sqlite3_prepare_v2(sqlite3Database, insert_stmt, -1, &statement, NULL);\
        if (sqlite3_step(statement) == SQLITE_DONE) {
            NSLog(@"Handshake added");
        } else {
            NSLog(@"Failed to add Handshake: %s", sqlite3_errmsg(sqlite3Database));
        }
        sqlite3_finalize(statement);
        sqlite3_close(sqlite3Database);
    } else {
        NSLog(@"Database is closed: %s", sqlite3_errmsg(sqlite3Database));
    }
}


@end
