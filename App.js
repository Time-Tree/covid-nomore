import React, { Component } from 'react';
import 'react-native-gesture-handler';
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';
import { store, persistor } from './src/redux/store';
import MainContainer from './src/screens/MainContainer';

export default class App extends Component {
  onBeforeLift = () => {};

  render() {
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
