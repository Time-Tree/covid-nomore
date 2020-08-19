import React from 'react';
import { StyleSheet, ScrollView, StatusBar } from 'react-native';
import AppLogo from './AppLogo';

const styles = StyleSheet.create({
  scroll: {
    backgroundColor: '#f7f8fc',
    flexGrow: 1
  },
  logo: {
    marginTop: 20,
    marginBottom: 7
  }
});

const ScrollableScreenShell = ({
  showLogo = false,
  noPadding = false,
  children
}) => {
  const screenStyles = {
    ...styles.scroll,
    padding: noPadding ? 0 : 16,
    paddingTop: StatusBar.currentHeight
  };

  return (
    <ScrollView contentContainerStyle={screenStyles}>
      {showLogo && <AppLogo style={styles.logo} />}
      {children}
    </ScrollView>
  );
};

export default ScrollableScreenShell;
