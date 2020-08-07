import React from 'react';
import { Image, View, StyleSheet } from 'react-native';
import logo from '../../images/appLogo.png';

const StatusHeader = ({ style = {} }) => {
  return (
    <View style={style}>
      <Image style={styles.logo} source={logo} />
    </View>
  );
};

const styles = StyleSheet.create({
  logo: {
    width: 204,
    height: 41
  }
});

export default StatusHeader;
