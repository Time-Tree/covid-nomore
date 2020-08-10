import React from 'react';
import { StyleSheet } from 'react-native';
import { Button } from 'react-native-elements';

const RoundedButton = ({ containerStyle = {}, buttonStyle = {}, ...props }) => {
  return (
    <Button
      containerStyle={{ ...styles.container, ...containerStyle }}
      buttonStyle={{ ...styles.button, ...buttonStyle }}
      titleStyle={styles.title}
      {...props}
    />
  );
};

const styles = StyleSheet.create({
  container: {
    /* ios only!
    shadowColor: 'rgba(255, 67, 109)',
    shadowOffset: { width: 0, height: 8 },
    shadowRadius: 0, */
    elevation: 10,
    width: 255
  },
  button: {
    borderRadius: 25,
    height: 54
  },
  title: {
    fontSize: 15
  }
});

export default RoundedButton;
