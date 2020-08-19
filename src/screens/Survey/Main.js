import React from 'react';
import { View, Text, StyleSheet, ScrollView, StatusBar } from 'react-native';
import { RoundedCard, AppLogo, RoundedButton } from '../../components';

const Main = ({ navigation }) => {
  const statusBarHeight = StatusBar.currentHeight;

  return (
    <View
      style={{
        ...styles.container,
        paddingTop: statusBarHeight
      }}
    >
      <ScrollView contentContainerStyle={styles.scroll}>
        <AppLogo />
        <RoundedCard style={styles.card}>
          <Text style={styles.header}>Survey</Text>
          <Text style={styles.text}>
            Description description description description description
            description description.
          </Text>
          <RoundedButton
            title="START SURVEY"
            containerStyle={styles.btnContainer}
            buttonStyle={styles.btn}
            onPress={() => {}}
          />
        </RoundedCard>
        <RoundedCard style={styles.card}>
          <Text style={styles.header}>Activity</Text>
          <Text style={styles.text}>
            Description description description description description
            description description.
          </Text>
          <RoundedButton
            title="VIEW ACTIVITY"
            containerStyle={styles.btnContainer}
            buttonStyle={styles.btn}
            onPress={() => {}}
          />
        </RoundedCard>
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#f7f8fc',
    flex: 1
  },
  scroll: {
    padding: 16,
    paddingTop: 20
  },
  card: {
    paddingVertical: 24,
    paddingHorizontal: 16,
    marginBottom: 16
  },
  header: {
    fontSize: 34,
    lineHeight: 41,
    color: '#2c314c',
    textAlign: 'center'
  },
  text: {
    marginTop: 16,
    fontSize: 14,
    lineHeight: 20,
    textAlign: 'center'
  },
  btnContainer: {
    marginLeft: 'auto',
    marginRight: 'auto',
    marginTop: 24
  }
});

export default Main;
