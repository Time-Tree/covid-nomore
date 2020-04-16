import * as React from 'react';
import { SafeAreaView, View, Text, FlatList, Button } from 'react-native';
import reduxContainer from '../redux/reduxContainer';
import settingsActions from '../redux/settings/actions';
import NearbyAPI from '../utils/nearbyAPI';
import { styles } from './styles';

class NearbyContainer extends React.Component {
  componentDidMount() {
    if (!this.props.publishCode) {
      const code = 1000 + Math.floor(Math.random() * 1000);
      this.props.changePublishCodeAction(code);
    }
    NearbyAPI.init();
  }

  resetHandler = () => {
    NearbyAPI.init();
  };

  renderItem = ({ item }) => (
    <View style={styles.eventContainer}>
      <Text style={styles.eventType}>{item.event}</Text>
      <Text style={styles.eventMessage}>{item.message}</Text>
    </View>
  );

  keyExtractor = (item, index) => index.toString();

  render() {
    const { publishCode, events } = this.props;
    return (
      <SafeAreaView>
        <View style={styles.headerContainer}>
          <Text>
            My code is <Text style={styles.publishCode}>{publishCode}</Text>
          </Text>
          <Button title="Reset" onPress={this.resetHandler} />
        </View>
        <Text>Messages:</Text>
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
  changePublishCodeAction: settingsActions.changePublishCodeAction
};

export default reduxContainer(
  NearbyContainer,
  mapStateToProps,
  dispatchToProps
);
