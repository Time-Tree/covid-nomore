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
  const [process, setProcess] = useState(props.bleProcess);
  const [loading, setLoading] = useState(false);

  const buttonHandler = () => {
    const value = process ? 0 : 1;
    setLoading(true);
    NearbyAPI.setToggle('bleProcess', value);
  };

  useEffect(() => {
    setProcess(props.bleProcess);
    setLoading(false);
  }, [props.bleProcess]);

  return (
    <>
      <NavbarComponent title="Protect" />
      <View style={styles.content}>
        <Text style={styles.bigText}>
          {process ? 'Protection activated' : 'Protection deactivated'}
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
            process ? styles.activeButton : styles.deactiveButton
          ]}
        >
          <ActivityIndicator color="white" />
        </View>
      ) : (
        <TouchableOpacity
          style={[
            styles.button,
            process ? styles.activeButton : styles.deactiveButton
          ]}
          onPress={buttonHandler}
        >
          <Text style={styles.textButton}>
            {process ? 'Deactivate CovidNoMore' : 'Activate CovidNoMore'}{' '}
          </Text>
        </TouchableOpacity>
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
  }
});

function mapStateToProps(state) {
  return {
    bleProcess: state.settings.bleProcess
  };
}

const dispatchToProps = {};

export default reduxContainer(
  ProtectContainer,
  mapStateToProps,
  dispatchToProps
);
