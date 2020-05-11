import React, { Component } from 'react';
import 'react-native-gesture-handler';
import { AppState } from 'react-native';
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';
import { store, persistor } from './src/redux/store';
import MainContainer from './src/screens/MainContainer';
import NearbyAPI from './src/utils/nearbyAPI';

AppState.addEventListener('change', _handleAppStateChange);
let _interval = null;

function _handleAppStateChange(nextAppState) {
  if (nextAppState === 'active') {
    startPoller();
  } else {
    if (_interval) {
      clearInterval(_interval);
    }
  }
}

function startPoller() {
  NearbyAPI.nearbyCheck();
  _interval = setInterval(() => {
    NearbyAPI.nearbyCheck();
  }, 10000);
}

export default class App extends Component {
  onBeforeLift = () => {};

  render() {
    startPoller();
    console.disableYellowBox = true;
    return (
      <Provider store={store}>
        <PersistGate
          loading={null}
          persistor={persistor}
          onBeforeLift={this.onBeforeLift}
        >
          <MainContainer />
        </PersistGate>
      </Provider>
    );
  }
}
