import * as React from 'react';
import {
  View,
  Text,
  FlatList,
  Button,
  Switch,
  StyleSheet,
  ActivityIndicator
} from 'react-native';

import reduxContainer from '../redux/reduxContainer';
import {
  ScrollableScreenShell,
  RoundedCard,
  RoundedButton
} from '../components';

import EventsActions from '../redux/events/actions';
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
    } else if (item.event.indexOf('BEACON') > -1) {
      if (item.event.indexOf('BEACON_FOUND') > -1) {
        color = 'green';
      } else {
        color = 'darkgreen';
      }
    }
    return (
      <Text style={styles.logEntry}>
        [{item.formated}] <Text style={{ color }}>{item.event}</Text>:{' '}
        {item.message}
      </Text>
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
    const { bleStatus, nearbyStatus } = this.props.settings;
    const loading = !(settings && settings.bleProcess !== undefined);

    return (
      <ScrollableScreenShell showLogo>
        <RoundedCard style={styles.card}>
          <View style={styles.headerContainer}>
            <Text>BLE Active: </Text>
            {loading ? (
              <ActivityIndicator />
            ) : (
              <Switch
                onValueChange={this.toggleHandler('bleProcess')}
                value={!!settings.bleProcess}
              />
            )}
            <Text>Nearby Active: </Text>
            {loading ? (
              <ActivityIndicator />
            ) : (
              <Switch
                onValueChange={this.toggleHandler('nearbyProcess')}
                value={!!settings.nearbyProcess}
              />
            )}
          </View>

          <View style={styles.headerContainer}>
            <Text>BLE Status: </Text>
            {bleStatus ? (
              <Text style={{ color: 'green' }}>ON</Text>
            ) : (
              <Text style={{ color: 'red' }}>OFF</Text>
            )}
            <Text>Nearby Satus: </Text>
            {nearbyStatus ? (
              <Text style={{ color: 'green' }}>ON</Text>
            ) : (
              <Text style={{ color: 'red' }}>OFF</Text>
            )}
          </View>

          <View style={[styles.headerContainer, { marginTop: 15 }]}>
            <Button title="settings" onPress={this.settingsHandler} />
            {status !== 0 && (
              <Button title="Reset Status" onPress={this.healthyHandler} />
            )}
            <Button title="clear logs" onPress={this.clearLogsHandler} />
            <Button title="Close logs" onPress={this.easterEggHandler} />
          </View>
        </RoundedCard>

        {/* <RoundedCard style={styles.card}>
          <Text style={styles.info}>
            Current publishing code: <Text style={styles.code}>1143</Text>
          </Text>
          <View style={styles.btnRow}>
            <RoundedButton
              title="PUBLISH"
              containerStyle={{
                ...styles.btnContainer,
                maginLeft: 10,
                marginRight: 4
              }}
              buttonStyle={styles.btn}
              onPress={() => {}}
            />
            <RoundedButton
              title="CLEAR"
              containerStyle={{
                ...styles.btnContainer,
                maginRight: 10,
                marginLeft: 4
              }}
              buttonStyle={styles.btn}
              onPress={() => {}}
            />
          </View>
        </RoundedCard> */}

        <RoundedCard style={styles.card}>
          <FlatList
            data={events}
            renderItem={this.renderItem}
            keyExtractor={this.keyExtractor}
            scrollEnabled={false}
          />
        </RoundedCard>
        <SettingsModal
          visible={settingsVisible}
          visibleHandler={this.settingsHandler}
          settings={settings}
        />
      </ScrollableScreenShell>
    );
  }
}

function mapStateToProps(state) {
  return {
    status: state.settings.status,
    settings: state.settings,
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

const styles = StyleSheet.create({
  card: {
    marginBottom: 16,
    paddingVertical: 24,
    paddingHorizontal: 16
  },
  info: {
    color: '#999',
    textAlign: 'center',
    textTransform: 'uppercase'
  },
  code: {
    color: '#0DC3FF'
  },
  btnRow: {
    display: 'flex',
    flexDirection: 'row',
    marginTop: 16
  },
  btnContainer: {
    width: 'auto',
    flexGrow: 1
  },
  header: {
    fontSize: 34,
    lineHeight: 41,
    textAlign: 'center',
    marginBottom: 8
  },
  logEntry: {
    marginTop: 8,
    fontSize: 14,
    opacity: 0.87
  },

  headerContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  }
});
