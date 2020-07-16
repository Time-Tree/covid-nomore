import React, { useCallback, useState } from 'react';
import {
  Text,
  StyleSheet,
  TouchableOpacity,
  View,
  ActivityIndicator
} from 'react-native';
import reduxContainer from '../redux/reduxContainer';
import NavbarComponent from './components/NavbarComponent';
import tokenActions from '../redux/tokens/actions';
import handshakesActions from '../redux/handshakes/actions';
import { getIntersection } from '../utils/psi';
import { DEVICE_WIDTH } from '../utils/deviceHelper';
import settingsActions from '../redux/settings/actions';

const StatusContainer = props => {
  const { status, sendHandshakesAction, changeStatusAction } = props;
  const statuses = ['Healthy', 'Infected', 'Exposed'];

  const checkExposure = useCallback(() => {
    sendHandshakesAction()
      .then(data => getIntersection(data))
      .then(aaa => {
        if (aaa > 0) {
          changeStatusAction(2);
        }
      });
  }, [sendHandshakesAction, changeStatusAction]);

  return (
    <>
      <NavbarComponent title="Health" />
      <View
        style={[{ backgroundColor: colors[status] }, styles.statusContainer]}
      >
        <Text style={styles.status}>STATUS:</Text>
        <Text style={styles.status}>{statuses[status]}</Text>
      </View>
      {props.pending ? (
        <View style={styles.button}>
          <ActivityIndicator color="darkblue" />
        </View>
      ) : (
        <TouchableOpacity style={styles.button} onPress={checkExposure}>
          <Text style={styles.textButton}> Check exposure </Text>
        </TouchableOpacity>
      )}
    </>
  );
};

const colors = ['#1db87f', '#b81d1d', '#5939cc'];

const styles = StyleSheet.create({
  status: {
    color: 'white',
    textAlign: 'center',
    fontWeight: 'bold',
    fontSize: 25
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
  statusContainer: {
    borderRadius: DEVICE_WIDTH * 0.7,
    marginVertical: 50,
    width: DEVICE_WIDTH * 0.7,
    height: DEVICE_WIDTH * 0.7,
    alignSelf: 'center',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5
  },
  textButton: {
    color: 'darkblue'
  }
});

function mapStateToProps(state) {
  return {
    status: state.settings.status,
    pending: state.handshakes.send_handshakes_pending
  };
}

const dispatchToProps = {
  sendHandshakesAction: handshakesActions.sendHandshakesAction,
  changeStatusAction: settingsActions.changeStatusAction
};

export default reduxContainer(
  StatusContainer,
  mapStateToProps,
  dispatchToProps
);
