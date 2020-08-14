import React from 'react';
import { View, Image, StyleSheet } from 'react-native';

import LinearGradient from 'react-native-linear-gradient';
import concaveMask from './concave.png';

const GradientProgress = ({ containerStyle, value }) => {
  const containerStyles = { ...styles.container, ...containerStyle };
  const { width } = containerStyles;

  const maskStyles = {
    ...styles.mask,
    width: width - Math.round((width * value) / 100)
  };

  if (value === 0) {
    maskStyles.width += 1;
    maskStyles.borderRadius = 4;
  }

  return (
    <LinearGradient
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 0 }}
      colors={['#00de66', '#ffdb00', '#ff436d']}
      style={containerStyles}
    >
      {value < 100 && (
        <View style={maskStyles}>
          {value > 0 && <Image source={concaveMask} style={styles.concave} />}
        </View>
      )}
    </LinearGradient>
  );
};

const styles = StyleSheet.create({
  container: {
    borderRadius: 4,
    width: 311,
    height: 8,
    backgroundColor: '#f4f5fa',
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'flex-end'
  },
  mask: {
    backgroundColor: '#f4f5fa',
    height: 8,
    borderTopRightRadius: 4,
    borderBottomRightRadius: 4,
    marginRight: -1
  },
  concave: {
    height: 8,
    width: 4,
    marginLeft: -4
  }
});

export default GradientProgress;
