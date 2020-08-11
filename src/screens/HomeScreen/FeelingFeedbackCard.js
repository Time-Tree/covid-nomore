import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import Icon from 'react-native-vector-icons/Feather';

import { RoundedCard, RoundedButton } from '../../components';
import FeelingFeedbackSelect from './FeelingFeedbackSelect';

const FeelingFeedbackCard = ({ selectedFeedback, onFeedback }) => {
  const icon = selectedFeedback ? (
    <Icon name="check" size={22} color="#fff" style={styles.btnIcon} />
  ) : null;

  const buttonColor = selectedFeedback ? '#00de66' : '#00c0ff';

  return (
    <RoundedCard style={styles.card}>
      <FeelingFeedbackSelect
        selectedFeedback={selectedFeedback}
        onFeedback={onFeedback}
      />
      <View style={styles.btnWrapper}>
        <RoundedButton
          title="CHECK VITALS"
          containerStyle={styles.buttonContainer}
          buttonStyle={{ ...styles.button, backgroundColor: buttonColor }}
          onPress={() => {}}
          iconRight={true}
          icon={icon}
        />
      </View>
    </RoundedCard>
  );
};

const styles = StyleSheet.create({
  card: {
    padding: 16,
    margin: 16
  },
  btnWrapper: {
    alignItems: 'center',
    marginBottom: 8
  },
  btnIcon: {
    position: 'absolute',
    right: 16
  }
});

export default FeelingFeedbackCard;
