import React from 'react';
import { View, StyleSheet } from 'react-native';

const StatusHeader = ({ children, style = {} }) => {
  return <View style={{ ...styles.card, ...style }}>{children}</View>;
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 36
  }
});

export default StatusHeader;
