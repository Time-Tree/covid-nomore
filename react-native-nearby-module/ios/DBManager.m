#import "DBManager.h"
#import <sqlite3.h>

static const NSString *DATABASE_NAME = @"NearbyEvents.db";
static const NSString *TABLE_NAME = @"NearbyEvents";
static const NSString *COLUMN_NAME_EVENT_TYPE = @"eventType";
static const NSString *COLUMN_NAME_MESSAGE = @"message";
static const NSString *COLUMN_NAME_FORMAT_DATE = @"formatDate";
static const NSString *COLUMN_NAME_TIMESTAMP = @"timestamp";
static sqlite3 *sqlite3Database;

@implementation DBManager

- (instancetype) init {
    self = [super init];
    [self createTable];
    return self;
}

- (void) createTable {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    self.databasePath =  [documentsDirectory stringByAppendingPathComponent:@"NearbyEvents.sqlite3"];
    NSFileManager *filemgr = [NSFileManager defaultManager];
    if ([filemgr fileExistsAtPath: self.databasePath] == NO) {
        NSLog(@"Database already exists at filePath: %@", self.databasePath);
    }
    const char *dbPath = [self.databasePath UTF8String];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *createTableSQL = [NSString stringWithFormat: @"CREATE TABLE IF NOT EXISTS %@ (ID integer primary key AUTOINCREMENT, %@ text, %@ text, %@ text, %@ text)", TABLE_NAME, COLUMN_NAME_EVENT_TYPE, COLUMN_NAME_MESSAGE, COLUMN_NAME_FORMAT_DATE, COLUMN_NAME_TIMESTAMP];
        char *errMsg;
        const char *sql_stmt = [createTableSQL UTF8String];
        if (sqlite3_exec(sqlite3Database, sql_stmt, NULL, NULL, &errMsg) != SQLITE_OK) {
            NSLog(@"Failed to create table: %s", sqlite3_errmsg(sqlite3Database));
        } else {
            NSLog(@"%@ table created", TABLE_NAME);
        }
        sqlite3_close(sqlite3Database);
    } else {
        NSLog(@"Failed to open/create database: %s", sqlite3_errmsg(sqlite3Database));
    }
}
    

- (void) saveData: (NSDictionary *) event {
    const char *dbPath = [self.databasePath UTF8String];
    if (sqlite3_open(dbPath, &sqlite3Database) == SQLITE_OK) {
        NSString *insertSQL = [NSString stringWithFormat: @"INSERT INTO %@ (%@, %@, %@, %@) VALUES (\"%@\", \"%@\", \"%@\", \"%@\")",TABLE_NAME, COLUMN_NAME_EVENT_TYPE, COLUMN_NAME_MESSAGE, COLUMN_NAME_FORMAT_DATE, COLUMN_NAME_TIMESTAMP, event[@"eventType"], event[@"message"], event[@"formatDate"], event[@"timestamp"]];
        
        const char *insert_stmt = [insertSQL UTF8String];
        sqlite3_stmt  *statement;
        sqlite3_prepare_v2(sqlite3Database, insert_stmt, -1, &statement, NULL);
        if (sqlite3_step(statement) == SQLITE_DONE) {
            NSLog(@"Event added");
        } else {
            NSLog(@"Failed to add: %s", sqlite3_errmsg(sqlite3Database));
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
        NSString *deleteSQL = [NSString stringWithFormat: @"DELETE FROM %@", TABLE_NAME];
        const char *delete_stmt = [deleteSQL UTF8String];
        sqlite3_stmt  *statement;
        
        sqlite3_prepare_v2(sqlite3Database, delete_stmt, -1, &statement, NULL);
        if (sqlite3_step(statement) == SQLITE_DONE) {
            NSLog(@"Events were successfully deleted");
        } else {
            NSLog(@"Failed to delete all: %s", sqlite3_errmsg(sqlite3Database));
        }
        sqlite3_finalize(statement);
        sqlite3_close(sqlite3Database);
    }
}

@end
