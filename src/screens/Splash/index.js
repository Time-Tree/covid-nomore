import React from 'react';
import { Image, View, StyleSheet } from 'react-native';

import splashImg from './splash.png';

const Splash = () => {
  return (
    <View style={styles.container}>
      <Image source={splashImg} style={styles.img} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#fff',
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  img: {
    width: 223,
    height: 193,
    marginTop: -100
  }
});

export default Splash;
