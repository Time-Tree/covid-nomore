import * as React from 'react';
import {
  View,
  Text,
  FlatList,
  Button,
  ActivityIndicator,
  TouchableOpacity,
  Platform
} from 'react-native';
import GetLocation from 'react-native-get-location';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import DeviceInfo from 'react-native-device-info';
import firestore from '@react-native-firebase/firestore';
import reduxContainer from '../redux/reduxContainer';
import { styles } from './styles';
import HandshakeActions from '../redux/handshakes/actions';
import NavbarComponent from './components/NavbarComponent';
import Btn from './components/BtnComponent';

class HandshakesContainer extends React.Component {
  state = { pending: false };
  questions = [
    {
      index: 'q1',
      text: "Q1: What's the distance?",
      values: ['< 1m', '1-2m', '> 2m']
    },
    {
      index: 'q2',
      text: 'Q2: Do you where masks?',
      values: ['Yes', 'No']
    }
  ];

  btnHandler = (item, question, value) => () => {
    item[question] = value;
    this.props.changeHandshakeAction(item);
  };

  removeHandler = time => () => {
    this.props.removeHanshakeAction(time);
  };

  renderItem = ({ item }) => (
    <View style={styles.tile} key={item.key}>
      <View style={styles.tileHeader}>
        <Text>
          <Text style={styles.handshakeType}>{item.type}:</Text> {item.target} @{' '}
          {item.formated}
        </Text>
        <TouchableOpacity onPress={this.removeHandler(item.time)}>
          <Icon name="delete-outline" size={25} color="red" />
        </TouchableOpacity>
      </View>
      {this.questions.map(q => (
        <View key={q.index} style={styles.question}>
          <Text>{q.text}</Text>
          <View style={styles.questionBtns}>
            {q.values.map((v, iv) => (
              <Btn
                key={iv}
                title={v}
                active={item[q.index] === v}
                onPress={this.btnHandler(item, q.index, v)}
              />
            ))}
          </View>
        </View>
      ))}
    </View>
  );

  keyExtractor = (item, index) => index.toString();

  addData = async () => {
    this.setState({ pending: true });
    let { lastUpdated } = this.props;
    if (!lastUpdated) {
      lastUpdated = 0;
    }
    const source = DeviceInfo.getUniqueId();
    let device = {};
    try {
      device = {
        app: DeviceInfo.getReadableVersion(),
        phone: DeviceInfo.getBrand(),
        deviceId: DeviceInfo.getDeviceId(),
        deviceName: await DeviceInfo.getDeviceName(),
        ip: await DeviceInfo.getIpAddress()
      };
    } catch (e) {
      console.log('DEVICE INFO ERROR', e);
    }
    let geo = {};
    try {
      geo = await GetLocation.getCurrentPosition({
        enableHighAccuracy: true,
        timeout: 15000
      });
    } catch (e) {
      console.log('GEO ERROR', e);
    }

    const db = firestore();
    const batch = db.batch();
    this.props.handshakes.forEach(h => {
      if (h.time > lastUpdated) {
        const entry = db.collection('handshakes').doc();
        delete h.formated;
        batch.set(entry, { ...h, source, device, geo });
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

  getUUID = () => {
    const id = DeviceInfo.getUniqueId();
    if (Platform.OS === 'ios') return id;
    return `${id.substring(0, 8)}-${id.substring(8, 12)}-${id.substring(12)}`;
  };

  render() {
    const { lastUpdated, handshakes } = this.props;
    const { pending } = this.state;
    return (
      <View style={styles.screen}>
        <NavbarComponent title="Nearby Handshakes" />
        <View style={styles.headerContainer}>
          <Text>
            My device UUID:
            <Text style={styles.publishCode}> {this.getUUID()}</Text>
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
        <FlatList
          data={handshakes}
          renderItem={this.renderItem}
          keyExtractor={this.keyExtractor}
        />
      </View>
    );
  }
}

function mapStateToProps(state) {
  return {
    handshakes: state.handshakes.handshakes,
    lastUpdated: state.handshakes.lastUpdated
  };
}

const dispatchToProps = {
  clearHandshakeAction: HandshakeActions.clearHandshakeAction,
  changeHandshakeAction: HandshakeActions.changeHandshakeAction,
  saveHandshakeAction: HandshakeActions.saveHandshakeAction,
  removeHanshakeAction: HandshakeActions.removeHanshakeAction
};

export default reduxContainer(
  HandshakesContainer,
  mapStateToProps,
  dispatchToProps
);
