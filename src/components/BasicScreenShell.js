import React from 'react';
import { View, StyleSheet, ScrollView, StatusBar } from 'react-native';
import AppLogo from './AppLogo';

const BasicScreenShell = ({ children }) => {
  const statusBarHeight = StatusBar.currentHeight;

  return (
    <View
      style={{
        ...styles.container,
        paddingTop: statusBarHeight
      }}
    >
      <ScrollView contentContainerStyle={styles.scroll}>
        <AppLogo style={styles.logo} />
        {children}
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
  logo: {
    marginBottom: 7
  }
});

export default BasicScreenShell;
