import { Platform } from 'react-native';
import NearbyModule from 'react-native-nearby-module';
import SQLite from 'react-native-sqlite-storage';
import { store } from '../redux/store';
import eventsActions from '../redux/events/actions';
import handshakeActions from '../redux/handshakes/actions';
import settingsActions from '../redux/settings/actions';
import keys from '../../keys';
import { getCoarseProximity, approximateDistance } from './distance.js';

class NearbyAPI {
  lock = false;

  startService(needKey) {
    if (needKey) {
      NearbyModule.startService(keys.NEARBY_KEY);
    } else {
      NearbyModule.startService();
    }
  }

  nearbyCheck() {
    this.getEvents();
  }

  getEvents = async () => {
    let db;
    try {
      if (this.lock) return;
      this.lock = true;
      let { sync } = store.getState().events;
      SQLite.enablePromise(true);
      if (Platform.OS === 'android') {
        db = await SQLite.openDatabase('NearbyEvents');
      } else {
        db = await SQLite.openDatabase({
          name: 'NearbyEvents.sqlite',
          location: 'Documents'
        });
      }
      console.log('Fetching events from sync', sync);
      const result = await db.executeSql(
        `SELECT * FROM NearbyEvents n WHERE n.timestamp > ${sync} ORDER BY n.timestamp DESC`
      );

      const newEvents = [];
      const handshakes = [];
      if (result[0]) {
        const res = result[0];
        for (let i = 0; i <= res.rows.length; i++) {
          const event = res.rows.item(i);
          if (event) {
            newEvents.push({
              event: event.eventType,
              formated: event.formatDate,
              message: event.message,
              timestamp: event.timestamp
            });
            if (event.timestamp > sync) {
              sync = event.timestamp;
            }
            if (
              event.eventType === 'NEARBY_FOUND' ||
              event.eventType === 'BLE_FOUND'
            ) {
              const { eventType } = event;
              const type = eventType.substring(0, eventType.indexOf('_'));
              const inputs = event.message.split(', ');
              const handshake = {
                type,
                time: parseInt(event.timestamp, 10),
                formated: event.formatDate,
                target: inputs.length === 3 ? inputs[1] : inputs[0]
              };
              if (type === 'BLE') {
                const rssi = event.message.split('RSSI: ')[1] || null;
                handshake.rssi = rssi;
                handshake.approximatedDistance = approximateDistance(rssi);
                handshake.coarseProximity = getCoarseProximity(rssi);
              }
              handshakes.push(handshake);
            }
          }
        }
      }
      console.log('Events found: ', newEvents.length);

      store.dispatch(eventsActions.addEventAction(newEvents));
      store.dispatch(handshakeActions.addHandshakeAction(handshakes));
      store.dispatch(eventsActions.setSync(sync));
      if (newEvents.length) {
        await db.executeSql(
          `DELETE FROM NearbyEvents WHERE timestamp <= ${sync}`
        );
      }
      this.lock = false;
    } catch (error) {
      console.log('Nearby check failed', JSON.stringify(error));
      this.lock = false;
    } finally {
      this.lock = false;
      if (db) {
        await db.close();
      }
    }
  };

  clearEvents = async () => {
    try {
      await NearbyModule.toggleState();
    } catch (error) {
      console.error('clearEvents', error);
    }
  };

  saveSettings = async data => {
    let db;
    try {
      delete data.bleProcess;
      delete data.nearbyProcess;
      SQLite.enablePromise(true);
      if (Platform.OS === 'android') {
        db = await SQLite.openDatabase('NearbyEvents');
      } else {
        db = await SQLite.openDatabase({
          name: 'NearbyEvents.sqlite',
          location: 'Documents'
        });
      }
      console.log('Saving settings data', data);
      let query = 'UPDATE Settings SET ';
      const labels = Object.keys(data);
      labels.map(async (key, index) => {
        query += `${key} = ${data[key]}`;
        if (index < labels.length - 1) {
          query += ', ';
        }
      });
      query += ' WHERE _ID = 1';
      await db.executeSql(query);
    } catch (error) {
      console.log('saveSettings error', error);
    } finally {
      if (db) {
        await db.close();
      }
    }
    NearbyModule.restartService();
  };

  getSettings = async () => {
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
      console.log('Getting setting data');
      let settings = {};
      const result = await db.executeSql(
        'SELECT * FROM Settings WHERE _ID = 1'
      );
      if (result[0]) {
        const res = result[0];
        settings = res.rows.item(0);
      }
      await db.close();
      delete settings._ID;
      return settings;
    } catch (error) {
      console.log('getSettings error', error);
      if (db) {
        await db.close();
      }
    }
  };

  setToggle = async (type, status) => {
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
      console.log('Saving new status', type, status);
      const query = `UPDATE Settings SET ${type} = ${status} WHERE _ID = 1`;
      await db.executeSql(query);
      store.dispatch(settingsActions.setProcessAction(type, !!status));
    } catch (error) {
      console.log('setToggle error', error);
    } finally {
      if (db) {
        await db.close();
      }
    }
    NearbyModule.restartService();
  };

  setNativeProcess = async status => {
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
      console.log('Update native process', status);
      const query = `UPDATE Settings SET bleProcess = ${status}, nearbyProcess = ${status} WHERE _ID = 1`;
      await db.executeSql(query);
      store.dispatch(settingsActions.setNearbyProcessAction(!!status));
      store.dispatch(settingsActions.setBleProcessAction(!!status));
    } catch (error) {
      console.log('setNativeProcess error', error);
    } finally {
      if (db) {
        await db.close();
      }
    }
    NearbyModule.restartService();
  };
}

export default new NearbyAPI();
