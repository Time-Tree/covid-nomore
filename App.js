import React, { Component } from 'react';
import 'react-native-gesture-handler';
import timer from 'react-native-timer';
import { AppState, Platform } from 'react-native';
import settingsActions from './src/redux/settings/actions';
import reduxContainer from './src/redux/reduxContainer';
import { PersistGate } from 'redux-persist/integration/react';
import { persistor } from './src/redux/store';
import NearbyAPI from './src/utils/nearbyAPI';
import {
  requestMultiple,
  PERMISSIONS,
  RESULTS
} from 'react-native-permissions';
import { setCrashlytics } from './src/utils/crashlythics';
import { generateRandomUUID } from './src/utils/uuid';

import SplashScreen from './src/screens/Splash';
import IntroScreen from './src/screens/Intro';
import ScreenTabs from './ScreenTabs';

class App extends Component {
  state = { interval: null, appState: null };

  requestPermissions = () => {
    requestMultiple([
      PERMISSIONS.ANDROID.ACCESS_BACKGROUND_LOCATION,
      PERMISSIONS.ANDROID.ACCESS_COARSE_LOCATION,
      PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION
    ])
      .then(result => {
        switch (result) {
          case RESULTS.UNAVAILABLE:
            console.log(
              'This feature is not available (on this device / in this context)'
            );
            break;
          case RESULTS.DENIED:
            console.log(
              'The permission has not been requested / is denied but requestable'
            );
            break;
          case RESULTS.GRANTED:
            console.log('The permission is granted');
            break;
          case RESULTS.BLOCKED:
            console.log('The permission is denied and not requestable anymore');
            break;
        }
      })
      .catch(error => {
        // …
      });
  };

  _handleAppStateChange = nextAppState => {
    console.log('_handleAppStateChange', nextAppState);
    if (nextAppState === 'active' && this.state.appState === 'background') {
      this.startPoller();
      NearbyAPI.restartServices();
    } else if (nextAppState !== 'active') {
      this.stopPoller();
    }
    this.setState({ appState: nextAppState });
  };

  startPoller = () => {
    this.stopPoller();
    console.log('Starting poller...');
    NearbyAPI.nearbyCheck();
    timer.setInterval(
      'poller',
      () => {
        NearbyAPI.nearbyCheck();
      },
      10000
    );
  };

  stopPoller = () => {
    console.log('Stopping poller...');
    timer.clearInterval('poller');
  };

  onBeforeLift = () => {
    AppState.removeEventListener('change');
    AppState.addEventListener('change', this._handleAppStateChange);
    this.startPoller();
    if (Platform.OS === 'android') {
      this.requestPermissions();
    }
  };

  componentDidMount() {
    setCrashlytics();

    if (!this.props.clientId) {
      this.props.setClientIdAction(generateRandomUUID());
    }
  }

  componentWillUnmount = () => {
    AppState.removeEventListener('change', this._handleAppStateChange);
  };

  render() {
    return (
      <PersistGate
        loading={<SplashScreen />}
        persistor={persistor}
        onBeforeLift={this.onBeforeLift}
      >
        <ScreenTabs showEasterEggScreens={this.props.easterEgg} />
      </PersistGate>
    );
  }
}

function mapStateToProps(state) {
  return {
    easterEgg: state.settings.easterEgg,
    clientId: state.settings.clientId
  };
}

const dispatchToProps = {
  setClientIdAction: settingsActions.setClientIdAction
};

export default reduxContainer(App, mapStateToProps, dispatchToProps);
