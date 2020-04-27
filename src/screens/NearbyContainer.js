import * as React from 'react';
import { SafeAreaView, View, Text, FlatList, Button } from 'react-native';
import DeviceInfo from 'react-native-device-info';
import reduxContainer from '../redux/reduxContainer';
import NearbyAPI from '../utils/nearbyAPI';
import { styles } from './styles';
import EventsActions from '../redux/events/actions';
import NavbarComponent from './components/NavbarComponent';

class NearbyContainer extends React.Component {
  resetHandler = () => {
    NearbyAPI.init();
  };

  clearLogsHandler = () => {
    this.props.clearEventsAction();
  };

  renderItem = ({ item }) => (
    <View style={styles.eventContainer}>
      <Text style={styles.eventType}>
        {item.time} {item.event} {item.message}
      </Text>
    </View>
  );

  keyExtractor = (item, index) => index.toString();

  render() {
    const { publishCode, events } = this.props;
    return (
      <SafeAreaView style={styles.screen}>
        <NavbarComponent title="Nearby Logs" />
        <View style={styles.headerContainer}>
          <Text>
            Current publishing code:{' '}
            <Text style={styles.publishCode}>{publishCode}</Text>
          </Text>
          <Button title="publish" onPress={this.resetHandler} />
          <Button title="clear" onPress={this.clearLogsHandler} />
        </View>
        <Text> LOGS:</Text>
        <FlatList
          data={events}
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
    events: state.events.events
  };
}

const dispatchToProps = {
  clearEventsAction: EventsActions.clearEventsAction
};

export default reduxContainer(
  NearbyContainer,
  mapStateToProps,
  dispatchToProps
);
