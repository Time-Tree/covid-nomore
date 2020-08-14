import React from 'react';
import { StyleSheet } from 'react-native';
import { Button } from 'react-native-elements';

const RoundedButton = ({
  containerStyle = {},
  buttonStyle = {},
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
      iconContainerStyle={styles.iconContainer}
      titleStyle={styles.title}
      {...props}
    />
  );
};

const styles = StyleSheet.create({
  container: {
    width: 255
  },
  shadow: {
    /* ios only!
    shadowColor: 'rgba(255, 67, 109)',
    shadowOffset: { width: 0, height: 8 },
    shadowRadius: 0, */
    elevation: 8
  },
  button: {
    borderRadius: 25,
    height: 54
  },
  title: {
    fontSize: 15
  },
  iconContainer: {
    position: 'relative'
  }
});

export default RoundedButton;
