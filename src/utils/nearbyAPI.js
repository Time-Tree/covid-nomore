import NearbyModule from 'react-native-nearby-module';
import SQLite from 'react-native-sqlite-storage';
import { store } from '../redux/store';
import eventsActions from '../redux/events/actions';
import handshakeActions from '../redux/handshakes/actions';
import settingsActions from '../redux/settings/actions';
import keys from '../../keys';

class NearbyAPI {
  startService() {
    NearbyModule.startService(keys.NEARBY_KEY);
  }

  nearbyCheck() {
    console.log('Nearby check');
    this.getEvents();
    this.getStatus();
  }

  getEvents = async () => {
    try {
      let { sync } = store.getState().events;

      SQLite.enablePromise(true);
      const db = await SQLite.openDatabase('NearbyEvents');
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
            if (event.eventType === 'MESSAGE_FOUND') {
              handshakes.push({
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
        const lastSync = newEvents[newEvents.length - 1].timestamp;
        await db.executeSql(
          `DELETE FROM NearbyEvents WHERE timestamp <= ${lastSync}`
        );
      }
      db.close();
    } catch (error) {}
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
}

export default new NearbyAPI();
