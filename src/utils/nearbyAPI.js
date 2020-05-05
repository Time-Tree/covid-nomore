import NearbyModule from 'react-native-nearby-module';
import { store } from '../redux/store';
import eventsActions from '../redux/events/actions';
import handshakeActions from '../redux/handshakes/actions';
import settingsActions from '../redux/settings/actions';

class NearbyAPI {
  nearbyCheck() {
    this.getEvents();
    this.getStatus();
  }

  getEvents = async () => {
    try {
      const response = await NearbyModule.getEvents();
      response.forEach(event => {
        store.dispatch(
          eventsActions.addEventAction({
            event: event.eventType,
            formated: event.formatDate,
            message: event.message
          })
        );
        if (event.eventType === 'MESSAGE_FOUND') {
          store.dispatch(
            handshakeActions.addHandshakeAction({
              time: parseInt(event.timestamp, 10),
              formated: event.formatDate,
              target: event.message.split('-')[0]
            })
          );
        }
      });
    } catch (error) {}
  };

  getStatus = async () => {
    try {
      const response = await NearbyModule.getStatus();
      store.dispatch(settingsActions.changeStatusAction(response));
    } catch (error) {}
  };

  toggleState = async () => {
    try {
      const response = await NearbyModule.toggleState();
      console.log('TOGGLE STATE', response);
    } catch (error) {
      console.error('toggleState', error);
    }
  };
}

export default new NearbyAPI();
