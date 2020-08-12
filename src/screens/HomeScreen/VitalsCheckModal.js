import React from 'react';
import { Text, StyleSheet } from 'react-native';
import Modal from 'react-native-modal';

import { RoundedCard, RoundedButton } from '../../components';

const FeelingFeedbackModal = ({ show, check, snooze }) => {
  return (
    <Modal isVisible={show}>
      <RoundedCard style={styles.card}>
        <Text style={styles.header}>Now check your vitals</Text>
        <RoundedButton
          title="CHECK VITALS"
          containerStyle={styles.buttonContainer}
          buttonStyle={styles.btnCheck}
          onPress={() => {}}
        />
        <RoundedButton
          title="REMIND ME IN 1 HOUR"
          containerStyle={styles.buttonContainer}
          buttonStyle={styles.btnSnooze}
          titleStyle={styles.btnSnoozeTitle}
          shadow={false}
          onPress={() => {}}
        />
      </RoundedCard>
    </Modal>
  );
};

const styles = StyleSheet.create({
  card: {
    paddingTop: 24,
    paddingBottom: 30,
    margin: 16,
    alignItems: 'center'
  },
  buttonContainer: {
    marginTop: 24
  },
  btnCheck: {
    backgroundColor: '#00c0ff'
  },
  btnSnooze: {
    backgroundColor: '#E5F8FF'
  },
  btnSnoozeTitle: {
    color: '#2c314c'
  },
  header: {
    textAlign: 'center',
    fontSize: 20,
    fontWeight: 'bold'
  }
});

export default FeelingFeedbackModal;
