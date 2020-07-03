import React, { useCallback, useState } from 'react';
import {
  View,
  Text,
  ActivityIndicator,
  FlatList,
  StyleSheet,
  TouchableOpacity
} from 'react-native';
import reduxContainer from '../redux/reduxContainer';
import NavbarComponent from './components/NavbarComponent';
import tokenActions from '../redux/tokens/actions';
import handshakesActions from '../redux/handshakes/actions';
import { getIntersection } from '../utils/psi';

const StatusContainer = props => {
  // 0 healthy, 1 infected, 2 possibly infected
  const [status, setStatus] = useState(0);
  const { tokens, sendTokensAction, handshakes, sendHandshakesAction } = props;
  const statuses = ['Healthy', 'Infected', 'Exposed'];

  const handleInfected = useCallback(() => {
    sendTokensAction(tokens);
    setStatus(1);
  }, [sendTokensAction, tokens]);

  const checkExposure = useCallback(() => {
    sendHandshakesAction(handshakes)
      .then(data => getIntersection(data))
      .then(aaa => {
        if (aaa > 0) {
          setStatus(2);
        }
      });
  }, [handshakes, sendHandshakesAction]);

  return (
    <>
      <NavbarComponent title="Status" />
      <Text>
        My status is <Text>{statuses[status]}</Text>
      </Text>
      <TouchableOpacity style={styles.button} onPress={handleInfected}>
        <Text> I'm infected </Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.button} onPress={checkExposure}>
        <Text> Check exposure </Text>
      </TouchableOpacity>
    </>
  );
};

const styles = StyleSheet.create({
  button: {
    padding: 30,
    marginHorizontal: 50,
    marginVertical: 10,
    backgroundColor: 'lightgray',
    borderRadius: 50,
    alignItems: 'center',
    justifyContent: 'center'
  }
});

function mapStateToProps(state) {
  return {
    tokens: state.tokens.tokens,
    handshakes: state.handshakes.handshakes
  };
}

const dispatchToProps = {
  sendTokensAction: tokenActions.sendTokensAction,
  sendHandshakesAction: handshakesActions.sendHandshakesAction
};

export default reduxContainer(
  StatusContainer,
  mapStateToProps,
  dispatchToProps
);
