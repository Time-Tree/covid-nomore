import * as React from 'react';
import {
  SafeAreaView,
  View,
  Text,
  FlatList,
  Button,
  ActivityIndicator
} from 'react-native';
import DeviceInfo from 'react-native-device-info';
import firestore from '@react-native-firebase/firestore';
import reduxContainer from '../redux/reduxContainer';
import { styles } from './styles';
import HandshakeActions from '../redux/handshakes/actions';
import NavbarComponent from './components/NavbarComponent';

class HandshakesContainer extends React.Component {
  state = { pending: false };

  renderItem = ({ item }) => (
    <View style={styles.eventContainer}>
      <Text style={styles.eventType}>
        [{item.formated}] handshake with {item.target}
      </Text>
    </View>
  );

  keyExtractor = (item, index) => index.toString();

  addData = () => {
    this.setState({ pending: true });
    let { lastUpdated } = this.props;
    if (!lastUpdated) {
      lastUpdated = 0;
    }
    const source = DeviceInfo.getUniqueId();
    const db = firestore();
    const batch = db.batch();
    this.props.handshakes.forEach(h => {
      if (h.time > lastUpdated) {
        const entry = db.collection('handshakes').doc();
        batch.set(entry, { time: h.time, target: h.target, source });
      }
    });
    batch.commit().then(() => {
      this.props.saveHandshakeAction();
      this.setState({ pending: false });
    });
  };

  getTimestamp = t => {
    if (!t) {
      return 'N/A';
    }
    const time = new Date(t);
    return `${time.getDate()}/${time.getMonth()}/${time.getFullYear()}  ${time.getHours()}:${time.getMinutes()}:${time.getSeconds()}.${time.getMilliseconds()}`;
  };

  render() {
    const { handshakes, lastUpdated } = this.props;
    const { pending } = this.state;
    return (
      <SafeAreaView style={styles.screen}>
        <NavbarComponent title="Nearby Handshakes" />
        <View style={styles.headerContainer}>
          <Text>
            My device UUID:
            <Text style={styles.publishCode}> {DeviceInfo.getUniqueId()}</Text>
          </Text>
          <Button title="Clear" onPress={this.props.clearHandshakeAction} />
        </View>
        <View style={styles.container}>
          <Text>Last save: </Text>
          {pending ? (
            <ActivityIndicator />
          ) : (
            <Text>{this.getTimestamp(lastUpdated)}</Text>
          )}

          <Button title="Save data" onPress={this.addData} disabled={pending} />
        </View>
        <Text>Handshakes:</Text>
        <FlatList
          data={handshakes}
          renderItem={this.renderItem}
          keyExtractor={this.keyExtractor}
        />
      </SafeAreaView>
    );
  }
}

function mapStateToProps(state) {
  return {
    publishCode: state.settings.publishCode,
    handshakes: state.handshakes.handshakes,
    lastUpdated: state.handshakes.lastUpdated
  };
}

const dispatchToProps = {
  clearHandshakeAction: HandshakeActions.clearHandshakeAction,
  saveHandshakeAction: HandshakeActions.saveHandshakeAction
};

export default reduxContainer(
  HandshakesContainer,
  mapStateToProps,
  dispatchToProps
);
