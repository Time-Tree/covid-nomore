import React from 'react';
import { Image, Text, View, StyleSheet, ScrollView } from 'react-native';
import { RoundedCard, AppLogo, RoundedButton } from '../../components';

import picHistory from './picResultHistory.png';
import picMyResult from './picMyResult.png';
import picChat from './picChat.png';

const Test = () => {
  return (
    <ScrollView contentContainerStyle={styles.container}>
      <AppLogo />
      <RoundedCard style={styles.card}>
        <Text style={styles.cardText}>Result{'\n'}history</Text>
        <RoundedButton
          title="SHOW"
          containerStyle={styles.btnContainer}
          buttonStyle={styles.blueBtn}
          onPress={() => {}}
        />
        <Image source={picHistory} style={styles.img} resizeMode="contain" />
      </RoundedCard>
      <RoundedCard style={{ ...styles.card, backgroundColor: '#00C0FF' }}>
        <Text style={styles.cardText}>My result</Text>
        <RoundedButton
          title="GET RESULT"
          containerStyle={styles.btnContainer}
          buttonStyle={styles.whiteBtn}
          titleStyle={styles.whiteBtnTitle}
          onPress={() => {}}
        />
        <Image source={picMyResult} style={styles.img} resizeMode="contain" />
      </RoundedCard>
      <RoundedCard style={styles.card}>
        <Text style={styles.cardText}>Healthcare{'\n'}provider</Text>
        <RoundedButton
          title="START CHAT"
          containerStyle={styles.btnContainer}
          buttonStyle={styles.blueBtn}
          onPress={() => {}}
        />
        <Image source={picChat} style={styles.img} resizeMode="contain" />
      </RoundedCard>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: 16,
    paddingTop: 30,
    backgroundColor: '#f7f8fc'
  },
  card: {
    marginTop: 16,
    height: 195,
    position: 'relative'
  },
  cardText: {
    marginTop: 31,
    marginLeft: 16,
    fontSize: 28,
    color: '#2c314c'
  },
  img: {
    height: 195,
    width: 145,
    position: 'absolute',
    top: 0,
    right: -1
  },
  btnContainer: {
    position: 'absolute',
    bottom: 30,
    left: 16,
    width: 140,
    height: 36
  },
  blueBtn: {
    backgroundColor: '#00c0ff'
  },
  whiteBtn: {
    backgroundColor: '#fff'
  },
  whiteBtnTitle: {
    color: '#2c314c'
  }
});

export default Test;
