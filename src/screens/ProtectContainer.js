import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator
} from 'react-native';
import reduxContainer from '../redux/reduxContainer';
import NavbarComponent from './components/NavbarComponent';
import NearbyAPI from '../utils/nearbyAPI';

const ProtectContainer = props => {
  const [activated, setActivated] = useState(
    props.bleStatus || props.nearbyStatus
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);
  const isDisabled = !props.nearbyProcess && !props.bleProcess;

  const buttonHandler = async () => {
    setLoading(true);
    setError(false);
    let status = null;
    if (!activated) {
      status = await NearbyAPI.startService();
    } else {
      status = await NearbyAPI.stopService();
    }
    console.log('STATUS CHANGE', status);
    if (status === 'SUCCESS') {
      const settings = await NearbyAPI.getSettings();
      console.log('Success status change', settings);
    } else if (status === 'BLE_ERROR') {
      setError('Bluetooth adapter is off or has some problems. Please retry!');
    } else {
      setError('Error with service. Please retry!');
    }
    setLoading(false);
  };

  useEffect(() => {
    setActivated(props.bleStatus || props.nearbyStatus);
    setLoading(false);
  }, [props.bleStatus, props.nearbyStatus]);

  return (
    <>
      <NavbarComponent title="Protect" />
      <View style={styles.content}>
        <Text style={styles.bigText}>
          {activated ? 'Protection activated' : 'Protection deactivated'}
        </Text>
        <Text style={styles.services}>
          {activated &&
            `(${props.bleStatus && 'BLE'} ${props.nearbyStatus && 'Nearby'})`}
        </Text>
        <Text style={styles.headerText}>
          Play an active role in fighting COVID-19!
        </Text>
        <Text style={styles.text}>
          Activate CovidNoMore to be informed and inform others about
          contaimation risks.
        </Text>
        <Text style={styles.text}>
          You can activate or deactivate CovidNoMore at any time.
        </Text>
      </View>
      {loading ? (
        <View
          style={[
            styles.button,
            activated ? styles.activeButton : styles.deactiveButton
          ]}
        >
          <ActivityIndicator color="white" />
        </View>
      ) : (
        <TouchableOpacity
          style={[
            styles.button,
            activated ? styles.activeButton : styles.deactiveButton
          ]}
          onPress={buttonHandler}
          disabled={isDisabled}
        >
          <Text style={styles.textButton}>
            {activated ? 'Deactivate CovidNoMore' : 'Activate CovidNoMore'}
          </Text>
        </TouchableOpacity>
      )}
      {error && <Text style={styles.error}>{error}</Text>}
      {isDisabled && (
        <Text style={styles.error}>
          Services are disabled, please enable them from settings.
        </Text>
      )}
    </>
  );
};

const styles = StyleSheet.create({
  content: {
    paddingVertical: 50
  },
  headerText: {
    fontWeight: 'bold',
    textAlign: 'center'
  },
  text: {
    marginHorizontal: 50,
    paddingVertical: 10,
    textAlign: 'center'
  },
  button: {
    borderRadius: 10,
    width: 200,
    height: 45,
    marginVertical: 25,
    backgroundColor: '#f7f8fc',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
    alignSelf: 'center',
    alignItems: 'center',
    justifyContent: 'center'
  },
  activeButton: {
    backgroundColor: '#b81d1d'
  },
  deactiveButton: {
    backgroundColor: '#1db87f'
  },
  textButton: {
    color: 'white'
  },
  bigText: {
    textAlign: 'center',
    fontSize: 24,
    fontWeight: 'bold',
    marginTop: 5,
    marginBottom: 30
  },
  services: {
    textAlign: 'center'
  },
  error: {
    color: 'red',
    textAlign: 'center'
  }
});

function mapStateToProps(state) {
  return {
    bleStatus: state.settings.bleStatus,
    nearbyStatus: state.settings.nearbyStatus,
    bleProcess: state.settings.bleProcess,
    nearbyProcess: state.settings.nearbyProcess
  };
}

const dispatchToProps = {};

export default reduxContainer(
  ProtectContainer,
  mapStateToProps,
  dispatchToProps
);
