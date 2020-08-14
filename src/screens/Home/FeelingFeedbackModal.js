import React from 'react';
import { StyleSheet } from 'react-native';
import Modal from 'react-native-modal';

import { RoundedCard } from '../../components';
import FeelingFeedbackSelect from './FeelingFeedbackSelect';

const FeelingFeedbackModal = ({ show, onFeedback }) => {
  return (
    <Modal isVisible={show}>
      <RoundedCard style={styles.card}>
        <FeelingFeedbackSelect onFeedback={onFeedback} />
      </RoundedCard>
    </Modal>
  );
};

const styles = StyleSheet.create({
  card: {
    padding: 16,
    margin: 16
  }
});

export default FeelingFeedbackModal;
