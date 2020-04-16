import { NativeEventEmitter } from 'react-native';
import NearbyModule from 'react-native-nearby-module';
import { store } from '../redux/store';
import eventsActions from '../redux/events/actions';

const NearbyModuleEmitter = new NativeEventEmitter(NearbyModule);

class NearbyAPI {
  eventListener = null;

  init = () => {
    if (this.eventListener) {
      this.eventListener.remove();
      NearbyModuleEmitter.removeListener('subscribe', this.eventHandler);
    }
    this.eventListener = NearbyModuleEmitter.addListener(
      'subscribe',
      this.eventHandler
    );
    store.dispatch(eventsActions.clearEventsAction());
    this.connect();
  };

  connect = () => {
    NearbyModule.unpublish();
    NearbyModule.unsubscribe();
    NearbyModule.connect('AIzaSyDvS8l_WlCWvb582wXHewYF1Nbcn0HqIas', true);
  };

  eventHandler = event => {
    if (event.event === 'CONNECTED') {
      this.connectedHandler();
    } else if (event.event === 'CONNECTION_FAILED') {
      setTimeout(this.connect, 5000);
    }
    store.dispatch(eventsActions.addEventAction(event));
  };

  connectedHandler = () => {
    const state = store.getState();
    const publishCode = state.settings.publishCode.toString();
    NearbyModule.publish(publishCode);
    NearbyModule.subscribe();
  };
}

export default new NearbyAPI();
