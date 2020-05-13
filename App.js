import React, { Component } from 'react';
import 'react-native-gesture-handler';
import { AppState, View, ActivityIndicator } from 'react-native';
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';
import { store, persistor } from './src/redux/store';
import MainContainer from './src/screens/MainContainer';
import NearbyAPI from './src/utils/nearbyAPI';
import {
  requestMultiple,
  PERMISSIONS,
  RESULTS
} from 'react-native-permissions';

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

export default class App extends Component {
  _interval = null;

  _handleAppStateChange = nextAppState => {
    console.log('_handleAppStateChange', nextAppState);
    if (nextAppState === 'active' && !this._interval) {
      NearbyAPI.nearbyCheck();
      this._interval = setInterval(() => {
        NearbyAPI.nearbyCheck();
      }, 10000);
    } else if (this._interval) {
      clearInterval(this._interval);
      this._interval = null;
    }
  };

  onBeforeLift = () => {
    AppState.addEventListener('change', this._handleAppStateChange);
  };

  componentWillUnmount = () => {
    AppState.removeEventListener('change', this._handleAppStateChange);
  };

  renderLoading = () => {
    return (
      <View>
        <ActivityIndicator size={'large'} />
      </View>
    );
  };

  render() {
    console.disableYellowBox = true;
    return (
      <Provider store={store}>
        <PersistGate
          loading={this.renderLoading()}
          persistor={persistor}
          onBeforeLift={this.onBeforeLift}
        >
          <MainContainer />
        </PersistGate>
      </Provider>
    );
  }
}
