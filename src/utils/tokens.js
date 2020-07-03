import SQLite from 'react-native-sqlite-storage';
import { Platform } from 'react-native';
import { store } from '../redux/store';
import { approximateDistance } from './distance.js';

export async function getTokens() {
  let db;
  try {
    let { sync } = store.getState().tokens;
    SQLite.enablePromise(true);
    if (Platform.OS === 'android') {
      db = await SQLite.openDatabase('NearbyEvents');
    } else {
      db = await SQLite.openDatabase({
        name: 'NearbyEvents.sqlite',
        location: 'Documents'
      });
    }
    console.log('Fetching tokens from sync', sync);
    const result = await db.executeSql(
      'SELECT * FROM Tokens ORDER BY created DESC'
    );
    const tokens = [];
    if (result[0]) {
      const res = result[0];
      for (let i = 0; i <= res.rows.length; i++) {
        const data = res.rows.item(i);
        if (data) {
          tokens.push({
            token: data.token,
            created: data.created,
            used: data.used
          });
          if (data.created > sync) {
            sync = data.created;
          }
        }
      }
    }
    console.log('New tokens found: ', tokens.length);
    await db.close();

    return Promise.resolve({ tokens, sync });
  } catch (error) {
    console.log('Get tokens failed', error);
    if (db) {
      await db.close();
    }
    return Promise.reject(error);
  }
}

export async function deleteTokens() {
  let db;
  try {
    SQLite.enablePromise(true);
    if (Platform.OS === 'android') {
      db = await SQLite.openDatabase('NearbyEvents');
    } else {
      db = await SQLite.openDatabase({
        name: 'NearbyEvents.sqlite',
        location: 'Documents'
      });
    }
    console.log('Delete all tokens');
    const deleted = await db.executeSql('DELETE FROM Tokens');
    console.log('Deleted no: ', deleted);
    await db.close();

    return Promise.resolve(deleted);
  } catch (error) {
    console.log('Delete tokens failed', error);
    if (db) {
      await db.close();
    }
    return Promise.reject(error);
  }
}

export async function getHandshakes() {
  let db;
  try {
    let { sync } = store.getState().handshakes;
    SQLite.enablePromise(true);
    if (Platform.OS === 'android') {
      db = await SQLite.openDatabase('NearbyEvents');
    } else {
      db = await SQLite.openDatabase({
        name: 'NearbyEvents.sqlite',
        location: 'Documents'
      });
    }
    console.log('Fetching handshakes from sync', sync);
    const result = await db.executeSql(
      'SELECT * FROM Handshakes ORDER BY discovered DESC'
    );
    const handshakes = [];
    if (result[0]) {
      const res = result[0];
      for (let i = 0; i <= res.rows.length; i++) {
        const data = res.rows.item(i);
        if (data) {
          handshakes.push({
            token: data.token,
            discovered: data.discovered,
            rssi: data.rssi,
            approximatedDistance: approximateDistance(data.rssi),
            characteristicData: data.characteristicData
          });
          if (data.discovered > sync) {
            sync = data.created;
          }
        }
      }
    }
    console.log('New handshakes found: ', handshakes.length);
    await db.close();

    return Promise.resolve({ handshakes, sync });
  } catch (error) {
    console.log('Get handshakes failed', error);
    if (db) {
      await db.close();
    }
    return Promise.reject(error);
  }
}

export async function deleteHandshakes() {
  let db;
  try {
    SQLite.enablePromise(true);
    if (Platform.OS === 'android') {
      db = await SQLite.openDatabase('NearbyEvents');
    } else {
      db = await SQLite.openDatabase({
        name: 'NearbyEvents.sqlite',
        location: 'Documents'
      });
    }
    console.log('Delete all handshakes');
    const deleted = await db.executeSql('DELETE FROM Handshakes');
    console.log('Deleted no: ', deleted.rowsAffected);
    await db.close();

    return Promise.resolve(deleted);
  } catch (error) {
    console.log('Delete handshakes failed', error);
    if (db) {
      await db.close();
    }
    return Promise.reject(error);
  }
}
