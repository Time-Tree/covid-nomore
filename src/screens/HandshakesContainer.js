import * as React from 'react';
import { SafeAreaView, View, Text, FlatList, Button } from 'react-native';
import DeviceInfo from 'react-native-device-info';
import reduxContainer from '../redux/reduxContainer';
import { styles } from './styles';
import HandshakeActions from '../redux/handshakes/actions';

class HandshakesContainer extends React.Component {
  renderItem = ({ item }) => (
    <View style={styles.eventContainer}>
      <Text style={styles.eventType}>{item}</Text>
    </View>
  );

  keyExtractor = (item, index) => index.toString();

  render() {
    const { handshakes } = this.props;
    return (
      <SafeAreaView>
        <View style={styles.headerContainer}>
          <Text>
            My device UUID:
            <Text style={styles.publishCode}> {DeviceInfo.getUniqueId()}</Text>
          </Text>
          <Button title="Clear" onPress={this.props.clearHandshakeAction} />
        </View>
        <Text>Handshakes:</Text>
        <FlatList
          style={{ marginBottom: 80 }}
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
    handshakes: state.handshakes.handshakes
  };
}

const dispatchToProps = {
  clearHandshakeAction: HandshakeActions.clearHandshakeAction
};

export default reduxContainer(
  HandshakesContainer,
  mapStateToProps,
  dispatchToProps
);
