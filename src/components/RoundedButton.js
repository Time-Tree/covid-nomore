import React from 'react';
import { StyleSheet } from 'react-native';
import { Button } from 'react-native-elements';

const RoundedButton = ({
  containerStyle = {},
  buttonStyle = {},
  titleStyle = {},
  shadow = true,
  ...props
}) => {
  const shadowStyles = shadow ? styles.shadow : {};

  return (
    <Button
      containerStyle={{
        ...styles.container,
        ...containerStyle,
        ...shadowStyles
      }}
      buttonStyle={{ ...styles.button, ...buttonStyle }}
      titleStyle={{ ...styles.title, ...titleStyle }}
      iconContainerStyle={styles.iconContainer}
      {...props}
    />
  );
};

const styles = StyleSheet.create({
  container: {
    width: 255,
    height: 48
  },
  shadow: {
    /* ios only!
    shadowColor: 'rgba(255, 67, 109)',
    shadowOffset: { width: 0, height: 8 },
    shadowRadius: 0, */
    elevation: 8
  },
  button: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 25
  },
  title: {
    fontSize: 15
  },
  iconContainer: {
    position: 'relative'
  }
});

export default RoundedButton;
