import React, { Component } from 'react';
import 'react-native-gesture-handler';
import { AppState, View, ActivityIndicator } from 'react-native';
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';
import { store, persistor } from './src/redux/store';
import MainContainer from './src/screens/MainContainer';
import NearbyAPI from './src/utils/nearbyAPI';

// AppState.addEventListener('change', _handleAppStateChange);

export default class App extends Component {
  _interval = null;

  _handleAppStateChange = nextAppState => {
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
