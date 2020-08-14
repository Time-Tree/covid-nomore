import React from 'react';
import { View, StyleSheet } from 'react-native';

const DotsPagination = ({ stepsCount, step = 0 }) => {
  const dots = [];
  for (let i = 1; i <= stepsCount; i++) {
    const bgColor = i === step ? '#fff' : '#4D90C2';
    dots.push(<View style={{ ...styles.dot, backgroundColor: bgColor }} />);
  }

  return <View style={styles.container}>{dots}</View>;
};

const styles = StyleSheet.create({
  container: {
    display: 'flex',
    flexDirection: 'row',
    margin: 25
  },
  dot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    marginRight: 5,
    marginLeft: 5
  }
});

export default DotsPagination;
