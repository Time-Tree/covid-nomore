import * as React from 'react';
import { StyleSheet, View, StatusBar, Text } from 'react-native';
import {
  isIOS,
  isIphoneX,
  statusBarHeight,
  borderWidth
} from '../../utils/deviceHelper';

class NavbarComponent extends React.PureComponent {
  render() {
    return (
      <View>
        <StatusBar
          translucent
          barStyle="dark-content"
          backgroundColor="transparent"
        />
        <View style={[styles.container, this.props.customStyle]}>
          <View style={styles.left} />
          <View style={styles.body}>
            <Text style={styles.title}>{this.props.title}</Text>
          </View>
          <View style={styles.right} />
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: 'white',
    justifyContent: 'center',
    flexDirection: 'row',
    paddingTop: isIOS ? (isIphoneX ? 30 : 15) : statusBarHeight,
    borderBottomWidth: borderWidth,
    borderBottomColor: '#ccddee',
    height: isIOS ? (isIphoneX ? 22 + 64 : 64) : 56 + statusBarHeight,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 1.2,
    top: 0,
    left: 0,
    right: 0,
    zIndex: 5
  },
  body: {
    flex: 2,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row-reverse'
  },
  title: {
    fontSize: isIOS ? 17 : 19,
    textAlign: 'center',
    paddingTop: 1,
    color: 'darkblue'
  },
  left: {
    flex: 1,
    alignSelf: 'center',
    alignItems: 'flex-start'
  },
  right: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'flex-end'
  }
});

export default NavbarComponent;
