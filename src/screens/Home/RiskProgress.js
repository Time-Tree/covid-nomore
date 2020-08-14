import React from 'react';
import { Text, StyleSheet } from 'react-native';

import { RoundedCard } from '../../components';
import GradientProgress from './GradientProgress';

const RiskProgress = ({ riskPercentage }) => {
  const level = riskPercentage < 50 ? 'Low' : 'High';
  const reminder = riskPercentage < 50 ? 'Everything is OK!' : 'Be careful';

  return (
    <RoundedCard style={styles.card}>
      <Text style={styles.header}>Risk Level - {level}</Text>
      <GradientProgress
        value={riskPercentage}
        containerStyle={styles.progress}
      />
      <Text style={styles.reminder}>{reminder}</Text>
    </RoundedCard>
  );
};

const styles = StyleSheet.create({
  card: {
    padding: 16,
    marginTop: 16,
    marginLeft: 16,
    marginRight: 16
  },
  header: {
    textAlign: 'center',
    fontSize: 20,
    fontWeight: 'bold'
  },
  progress: {
    marginLeft: 'auto',
    marginRight: 'auto',
    marginTop: 16,
    marginBottom: 16
  },
  reminder: {
    textAlign: 'center',
    fontSize: 17
  }
});

export default RiskProgress;
