import * as React from 'react';
import { View, Text, FlatList, Button, Switch } from 'react-native';
import reduxContainer from '../redux/reduxContainer';
import { styles } from './styles';
import EventsActions from '../redux/events/actions';
import NavbarComponent from './components/NavbarComponent';
import SettingsModal from './SettingsModal';
import 'react-native-console-time-polyfill';
import NearbyApi from '../utils/nearbyAPI';
import settingsActions from '../redux/settings/actions';

class NearbyContainer extends React.Component {
  state = {
    settingsVisible: false,
    settings: {}
  };

  componentDidMount() {
    NearbyApi.getSettings()
      .then(settings => this.setState({ settings }))
      .catch();
  }

  clearLogsHandler = () => {
    this.props.clearEventsAction();
    NearbyApi.clearEvents();
  };

  settingsHandler = () => {
    this.setState({
      settingsVisible: !this.state.settingsVisible
    });
  };

  toggleHandler = type => value => {
    const { settings } = this.state;
    settings[type] = value;
    this.setState({ settings });
    NearbyApi.setToggle(type, value ? 1 : 0);
  };

  renderItem = ({ item }) => {
    let color = 'darkmagenta';
    if (item.event.indexOf('ERROR') > -1) {
      color = 'darkred';
    } else if (item.event.indexOf('BLE') > -1) {
      if (item.event === 'BLE_FOUND') {
        color = 'blue';
      } else {
        color = 'darkblue';
      }
    } else if (item.event.indexOf('NEARBY') > -1) {
      if (item.event === 'NEARBY_FOUND') {
        color = 'green';
      } else {
        color = 'darkgreen';
      }
    }
    return (
      <View style={styles.eventContainer}>
        <Text style={{ ...styles.eventType, color }}>
          [{item.formated}]{' '}
          <Text style={{ fontWeight: 'bold' }}>{item.event}</Text>:{' '}
          {item.message}
        </Text>
      </View>
    );
  };

  keyExtractor = (item, index) => index.toString();

  easterEggHandler = () => {
    this.props.setEasterEggAction(false);
  };

  healthyHandler = () => {
    this.props.changeStatusAction(0);
  };

  render() {
    const { events, status } = this.props;
    const { settingsVisible, settings } = this.state;
    return (
      <View style={styles.screen}>
        <NavbarComponent title="Nearby Logs" />
        <View style={styles.headerContainer}>
          <Text>BLE: </Text>
          <Switch
            onValueChange={this.toggleHandler('bleProcess')}
            value={!!settings.bleProcess}
          />
          <Text>Nearby: </Text>
          <Switch
            onValueChange={this.toggleHandler('nearbyProcess')}
            value={!!settings.nearbyProcess}
          />
          <Button title="settings" onPress={this.settingsHandler} />
        </View>
        <View style={styles.headerContainer}>
          <Button title="Easter Egg" onPress={this.easterEggHandler} />
          {status !== 0 && (
            <Button title="Reset Status" onPress={this.healthyHandler} />
          )}
        </View>
        <View style={styles.headerContainer}>
          <Text>LOGS:</Text>
          <Button title="clear" onPress={this.clearLogsHandler} />
        </View>
        <FlatList
          data={events}
          renderItem={this.renderItem}
          keyExtractor={this.keyExtractor}
        />
        <SettingsModal
          visible={settingsVisible}
          visibleHandler={this.settingsHandler}
          settings={settings}
        />
      </View>
    );
  }
}

function mapStateToProps(state) {
  return {
    status: state.settings.status,
    events: state.events.events
  };
}

const dispatchToProps = {
  clearEventsAction: EventsActions.clearEventsAction,
  setEasterEggAction: settingsActions.setEasterEggAction,
  changeStatusAction: settingsActions.changeStatusAction
};

export default reduxContainer(
  NearbyContainer,
  mapStateToProps,
  dispatchToProps
);
