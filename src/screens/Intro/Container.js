import React from 'react';
import { ScrollView, StatusBar, StyleSheet } from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import GestureRecognizer from 'react-native-swipe-gestures';
import DotsPagination from './DotsPagination';

const Container = ({
  step = 1,
  onGoBack = () => {},
  onGoForward = () => {},
  children
}) => {
  const gestureProps = {
    config: {
      velocityThreshold: 0.3,
      directionalOffsetThreshold: 80
    },
    style: { flex: 1 },
    onSwipeLeft: onGoForward,
    onSwipeRight: onGoBack
  };

  return (
    <GestureRecognizer {...gestureProps}>
      <StatusBar translucent backgroundColor="transparent" />
      <ScrollView contentContainerStyle={styles.container}>
        <LinearGradient
          start={{ x: 1, y: 1 }}
          end={{ x: 0, y: 0 }}
          colors={['#0060a8', '#00c0ff']}
          style={styles.gradient}
        >
          {children}
          <DotsPagination stepsCount={4} step={step} />
        </LinearGradient>
      </ScrollView>
    </GestureRecognizer>
  );
};

export const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  gradient: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-around',
    alignItems: 'center',
    padding: 16
  },
  imgContainer: {
    marginTop: 10,
    width: '80%',
    flexGrow: 1
  },
  img: {
    flex: 1,
    width: undefined,
    height: undefined,
    resizeMode: 'contain'
  },
  header: {
    marginTop: 35,
    marginBottom: 35,
    color: '#fff',
    fontSize: 22,
    fontWeight: 'bold',
    textAlign: 'center',
    textTransform: 'uppercase'
  },
  info: {
    marginTop: 16,
    color: '#fff',
    fontSize: 17,
    textAlign: 'center'
  },
  btn: {
    backgroundColor: '#fff'
  },
  btnLabel: {
    color: '#2c314c'
  },
  btnContainer: {
    width: '100%',
    marginTop: 16
  }
});

export default Container;
