import { Platform } from 'react-native';
import NearbyModule from 'react-native-nearby-module';
import SQLite from 'react-native-sqlite-storage';
import { store } from '../redux/store';
import eventsActions from '../redux/events/actions';
import handshakeActions from '../redux/handshakes/actions';
import settingsActions from '../redux/settings/actions';
import keys from '../../keys';

class NearbyAPI {
  lock = false;

  startService() {
    NearbyModule.startService(keys.NEARBY_KEY);
  }

  nearbyCheck() {
    this.getEvents();
    this.getStatus();
  }

  getEvents = async () => {
    try {
      if (this.lock) return;
      this.lock = true;
      let { sync } = store.getState().events;

      SQLite.enablePromise(true);
      let db;
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
            if (
              event.eventType === 'NEARBY_FOUND' ||
              event.eventType === 'BLE_FOUND'
            ) {
              const { eventType } = event;
              const type = eventType.substring(0, eventType.indexOf('_'));
              handshakes.push({
                type,
                time: parseInt(event.timestamp, 10),
                formated: event.formatDate,
                target: event.message.split('-')[0]
              });
            }
          }
        }
      }
      console.log('Events found: ', newEvents.length);

      store.dispatch(eventsActions.addEventAction(newEvents));
      store.dispatch(handshakeActions.addHandshakeAction(handshakes));
      if (newEvents.length) {
        const lastSync = newEvents[0].timestamp;
        await db.executeSql(
          `DELETE FROM NearbyEvents WHERE timestamp <= ${lastSync}`
        );
      }
      await db.close();
      this.lock = false;
    } catch (error) {
      this.lock = false;
    } finally {
      this.lock = false;
    }
  };

  getStatus = async () => {
    try {
      const response = await NearbyModule.getStatus();
      store.dispatch(settingsActions.changeStatusAction(response));
    } catch (error) {}
  };

  clearEvents = async () => {
    try {
      await NearbyModule.toggleState();
    } catch (error) {
      console.error('clearEvents', error);
    }
  };

  saveSettings = async data => {
    try {
      SQLite.enablePromise(true);
      let db;
      if (Platform.OS === 'android') {
        db = await SQLite.openDatabase('NearbyEvents');
      } else {
        db = await SQLite.openDatabase({
          name: 'NearbyEvents.sqlite',
          location: 'Documents'
        });
      }
      console.log('Saving settings data', data);
      let query = `UPDATE Settings SET `;
      const labels = Object.keys(data);
      labels.map(async (key, index) => {
        query += `${key} = ${data[key]}`;
        if (index < labels.length - 1) {
          query += ', ';
        }
      });
      query += ' WHERE _ID = 1';
      await db.executeSql(query);
      await db.close();
      NearbyModule.restartService();
    } catch (error) {}
  };

  getSettings = async () => {
    try {
      SQLite.enablePromise(true);
      let db;
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
        `SELECT * FROM Settings WHERE _ID = 1`
      );
      if (result[0]) {
        const res = result[0];
        settings = res.rows.item(0);
      }
      await db.close();
      delete settings._ID;
      return settings;
    } catch (error) {}
  };

  setToggle = async (type, status) => {
    try {
      SQLite.enablePromise(true);
      let db;
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
      await db.close();
      NearbyModule.restartService();
    } catch (error) {}
  };
}

export default new NearbyAPI();
