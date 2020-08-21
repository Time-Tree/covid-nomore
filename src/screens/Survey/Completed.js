import React from 'react';
import { StatusBar, StyleSheet, Text } from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import MaterialCommunityIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import { RoundedButton } from '../../components';

const Completed = () => {
  return (
    <>
      <StatusBar translucent backgroundColor="transparent" />
      <LinearGradient
        start={{ x: 1, y: 1 }}
        end={{ x: 0, y: 0 }}
        colors={['#0060a8', '#00c0ff']}
        style={styles.gradient}
      >
        <MaterialCommunityIcon
          name="check-circle-outline"
          size={88}
          color="#fff"
        />
        <Text style={styles.header}>The survey{'\n'}is completed</Text>
        <Text style={styles.info}>Thank you!</Text>
        <RoundedButton
          title="CONTINUE"
          containerStyle={styles.btnContainer}
          buttonStyle={styles.btn}
          titleStyle={styles.btnLabel}
          onPress={() => {}}
        />
      </LinearGradient>
    </>
  );
};

const styles = StyleSheet.create({
  gradient: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 16
  },
  header: {
    marginTop: 35,
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
    marginTop: 75,
    maxWidth: 300
  }
});

export default Completed;
