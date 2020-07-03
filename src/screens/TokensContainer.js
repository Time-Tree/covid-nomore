import React, { useEffect, useState } from 'react';
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

const MyTokens = props => {
  const { getTokensAction, pending, tokens } = props;

  useEffect(() => {
    getTokensAction()
      .then(response => console.log('response', response))
      .catch(error => console.log('error', error));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const renderItem = ({ item }) => (
    <View style={styles.tokenContainer}>
      <Text>
        Token: <Text style={styles.value}>{item.token}</Text>
      </Text>
      <Text>
        Created:
        <Text>{new Date(item.created).toLocaleDateString()}</Text>{' '}
        <Text>{new Date(item.created).toLocaleTimeString()}</Text>
      </Text>
      <Text>
        Used: <Text style={styles.value}>{item.used ? 'YES' : 'NO'}</Text>
      </Text>
    </View>
  );

  const keyExtractor = item => item.created && item.created.toString();

  return (
    <>
      {pending ? (
        <ActivityIndicator />
      ) : (
        <FlatList
          data={tokens}
          renderItem={renderItem}
          contentContainerStyle={styles.list}
          keyExtractor={keyExtractor}
        />
      )}
      <TouchableOpacity
        style={styles.deleteButton}
        onPress={props.deleteTokensAction}
      >
        <Text>Delete all tokens</Text>
      </TouchableOpacity>
    </>
  );
};

const MyHandshakes = props => {
  const { handshakes, pending, getHandshakesAction } = props;

  useEffect(() => {
    getHandshakesAction()
      .then(response => console.log('response', response))
      .catch(error => console.log('error', error));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const renderItem = ({ item }) => (
    <View style={styles.tokenContainer}>
      <Text>
        Token: <Text style={styles.value}>{item.token}</Text>
      </Text>
      <Text>
        Discovered:
        <Text>{new Date(item.discovered).toLocaleDateString()}</Text>{' '}
        <Text>{new Date(item.discovered).toLocaleTimeString()}</Text>
      </Text>
      <Text>
        Characteristic data:{' '}
        <Text style={styles.value}>
          {item.characteristicData ? 'YES' : 'NO'}
        </Text>
      </Text>
      <Text>
        RSSI: <Text style={styles.value}>{item.rssi}</Text> Approximated{' '}
        Distance: <Text style={styles.value}>{item.approximatedDistance}</Text>
      </Text>
    </View>
  );

  const keyExtractor = item => item.discovered && item.discovered.toString();

  return (
    <>
      {pending ? (
        <ActivityIndicator />
      ) : (
        <FlatList
          data={handshakes}
          renderItem={renderItem}
          contentContainerStyle={styles.list}
          keyExtractor={keyExtractor}
        />
      )}
      <TouchableOpacity
        style={styles.deleteButton}
        onPress={props.deleteHandshakesAction}
      >
        <Text>Delete all handshakes</Text>
      </TouchableOpacity>
    </>
  );
};

const TokensContainer = props => {
  const [tab, setTab] = useState('myTokens');

  const {
    getTokensAction,
    deleteTokensAction,
    getHandshakesAction,
    deleteHandshakesAction
  } = props;

  return (
    <>
      <NavbarComponent title="Tokens" />
      <View style={styles.row}>
        <TouchableOpacity
          style={styles.button}
          onPress={() => setTab('myTokens')}
        >
          <Text style={[tab === 'myTokens' && styles.value]}>My tokens</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={() => setTab('myHandshakes')}
        >
          <Text style={[tab === 'myHandshakes' && styles.value]}>
            My handshakes
          </Text>
        </TouchableOpacity>
      </View>
      {tab === 'myTokens' ? (
        <MyTokens
          tokens={props.tokens}
          pending={props.tokens_pending || props.delete_tokens_pending}
          getTokensAction={getTokensAction}
          deleteTokensAction={deleteTokensAction}
        />
      ) : (
        <MyHandshakes
          handshakes={props.handshakes}
          pending={props.handshakes_pending || props.delete_handshakes_pending}
          getHandshakesAction={getHandshakesAction}
          deleteHandshakesAction={deleteHandshakesAction}
        />
      )}
    </>
  );
};

const styles = StyleSheet.create({
  row: {
    height: 48,
    flexDirection: 'row'
  },
  button: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  list: {
    flexGrow: 1
  },
  tokenContainer: {
    marginHorizontal: 10,
    paddingVertical: 10,
    borderBottomWidth: 0.5,
    borderBottomColor: 'lightgray'
  },
  value: {
    fontWeight: 'bold',
    color: 'darkblue'
  },
  deleteButton: {
    height: 48,
    justifyContent: 'center',
    alignItems: 'center'
  }
});

function mapStateToProps(state) {
  return {
    tokens: state.tokens.tokens,
    tokens_pending: state.tokens.get_tokens_pending,
    delete_tokens_pending: state.tokens.delete_tokens_pending,
    handshakes: state.handshakes.handshakes,
    handshakes_pending: state.handshakes.get_handshakes_pending,
    delete_handshakes_pending: state.handshakes.delete_handshakes_pending
  };
}

const dispatchToProps = {
  getTokensAction: tokenActions.getTokensAction,
  getHandshakesAction: handshakesActions.getHandshakesAction,
  deleteHandshakesAction: handshakesActions.deleteHandshakesAction,
  deleteTokensAction: tokenActions.deleteTokensAction
};

export default reduxContainer(
  TokensContainer,
  mapStateToProps,
  dispatchToProps
);
