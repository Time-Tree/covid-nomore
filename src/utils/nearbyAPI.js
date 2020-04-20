import { NativeEventEmitter } from 'react-native';
import NearbyModule from 'react-native-nearby-module';
import DeviceInfo from 'react-native-device-info';
import { store } from '../redux/store';
import eventsActions from '../redux/events/actions';
import settingsActions from '../redux/settings/actions';
import handshakeActions from '../redux/handshakes/actions';

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
    this.connect();
  };

  connect = () => {
    this.changeCode();
    NearbyModule.unpublish();
    NearbyModule.unsubscribe();
    NearbyModule.connect('AIzaSyDvS8l_WlCWvb582wXHewYF1Nbcn0HqIas', true);
  };

  changeCode = () => {
    const code = 1000 + Math.floor(Math.random() * 1000);
    store.dispatch(settingsActions.changePublishCodeAction(code));
  };

  eventHandler = event => {
    if (event.event === 'CONNECTED') {
      this.connectedHandler();
    } else if (event.event === 'CONNECTION_FAILED') {
      setTimeout(this.connect, 5000);
    }
    event.time = this.getTimestamp();
    if (event.event !== 'CONNECTED' && event.event !== 'SUBSCRIBE_SUCCESS') {
      store.dispatch(eventsActions.addEventAction(event));
    }
    if (event.event === 'MESSAGE_FOUND') {
      store.dispatch(
        handshakeActions.addHandshakeAction(
          this.getTimestamp() + ' Handshake with ' + event.message.split('-')[0]
        )
      );
    }
  };

  connectedHandler = () => {
    const state = store.getState();
    const publishCode =
      DeviceInfo.getUniqueId() + '-' + state.settings.publishCode.toString();
    NearbyModule.publish(publishCode);
    NearbyModule.subscribe();
  };

  getTimestamp = () => {
    const time = new Date();
    return `[${time.getHours()}:${time.getMinutes()}:${time.getSeconds()}.${time.getMilliseconds()}]`;
  };
}

export default new NearbyAPI();
