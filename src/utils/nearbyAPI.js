import { NativeEventEmitter } from 'react-native';
import NearbyModule from 'react-native-nearby-module';
import DeviceInfo from 'react-native-device-info';
import { store } from '../redux/store';
import eventsActions from '../redux/events/actions';
import settingsActions from '../redux/settings/actions';
import handshakeActions from '../redux/handshakes/actions';
import keys from './keys';

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
    NearbyModule.connect(keys.NEARBY_KEY, true);
  };

  changeCode = () => {
    const code = 1000 + Math.floor(Math.random() * 1000);
    store.dispatch(settingsActions.changePublishCodeAction(code));
  };

  eventHandler = event => {
    const date = new Date();
    if (event.event === 'CONNECTED') {
      this.connectedHandler();
    } else if (event.event === 'CONNECTION_FAILED') {
      setTimeout(this.connect, 5000);
    }
    event.time = date.getTime();
    event.formated = this.getTimestamp(date);
    if (event.event !== 'CONNECTED' && event.event !== 'SUBSCRIBE_SUCCESS') {
      store.dispatch(eventsActions.addEventAction(event));
    }
    if (event.event === 'MESSAGE_FOUND') {
      store.dispatch(
        handshakeActions.addHandshakeAction({
          time: date.getTime(),
          formated: this.getTimestamp(date),
          target: event.message.split('-')[0]
        })
      );
    }
  };

  getTimestamp = time => {
    let hour = time.getHours();
    if (hour < 10) {
      hour = '0' + hour;
    }
    let minutes = time.getMinutes();
    if (minutes < 10) {
      minutes = '0' + minutes;
    }
    let seconds = time.getSeconds();
    if (seconds < 10) {
      seconds = '0' + seconds;
    }
    let ms = time.getMilliseconds();
    if (ms < 10) {
      ms = '00' + ms;
    } else if (ms < 100) {
      ms = '0' + ms;
    }
    return `[${hour}:${minutes}:${seconds}.${ms}]`;
  };

  connectedHandler = () => {
    const state = store.getState();
    const publishCode =
      DeviceInfo.getUniqueId() + '-' + state.settings.publishCode.toString();
    NearbyModule.publish(publishCode);
    NearbyModule.subscribe();
  };
}

export default new NearbyAPI();
