import * as React from 'react';
import { SafeAreaView, View, Text, FlatList, Button } from 'react-native';
import reduxContainer from '../redux/reduxContainer';
import { styles } from './styles';
import EventsActions from '../redux/events/actions';
import NavbarComponent from './components/NavbarComponent';
import 'react-native-console-time-polyfill';
import NearbyApi from '../utils/nearbyAPI';

class NearbyContainer extends React.Component {
  clearLogsHandler = () => {
    this.props.clearEventsAction();
    NearbyApi.clearEvents();
  };

  renderItem = ({ item }) => (
    <View style={styles.eventContainer}>
      <Text style={styles.eventType}>
        [{item.formated}] {item.event}: {item.message}
      </Text>
    </View>
  );

  keyExtractor = (item, index) => index.toString();

  render() {
    const { events, isConnected, isSubscribing } = this.props;
    return (
      <SafeAreaView style={styles.screen}>
        <NavbarComponent title="Nearby Logs" />
        <View style={styles.headerContainer}>
          <Text>
            Connected:{' '}
            <Text style={[styles.value, !isConnected && styles.error]}>
              {isConnected ? 'TRUE' : 'FALSE'}
            </Text>
          </Text>
          <Text>
            Subscribing:{' '}
            <Text style={[styles.value, !isSubscribing && styles.error]}>
              {isSubscribing ? 'TRUE' : 'FALSE'}
            </Text>
          </Text>
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
    isSubscribing: state.settings.isSubscribing,
    isConnected: state.settings.isConnected,
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
