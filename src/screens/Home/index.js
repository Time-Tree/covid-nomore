import React, { useState } from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';

import StatusHeader from './StatusHeader';
import RiskProgress from './RiskProgress';
import FeelingFeedbackCard from './FeelingFeedbackCard';
import FeelingFeedbackModal from './FeelingFeedbackModal';
import VitalsCheckModal from './VitalsCheckModal';

const Home = () => {
  const [isActive, setIsActive] = useState(false);
  const [feeling, setFeeling] = useState(null);
  const [vitalsChecked, setVitalsChecked] = useState(false);
  const [vitalsModalDismissed, setVitalsModalDismissed] = useState(false);

  const showFeelingModal = feeling === null;
  const showVitalsCheckModal =
    showFeelingModal === false &&
    vitalsChecked === false &&
    vitalsModalDismissed === false;

  const onFeedback = feelingFeedback => setFeeling(feelingFeedback);

  return (
    <View style={styles.container}>
      <ScrollView>
        <StatusHeader
          isActive={isActive}
          onActivatePress={() => setIsActive(true)}
        />
        <RiskProgress riskPercentage={50} />
        <FeelingFeedbackCard
          selectedFeedback={feeling}
          vitalsChecked={vitalsChecked}
          onFeedback={onFeedback}
        />
        <FeelingFeedbackModal show={showFeelingModal} onFeedback={onFeedback} />
        <VitalsCheckModal
          show={showVitalsCheckModal}
          check={() => setVitalsChecked(true)}
          snooze={() => setVitalsModalDismissed(true)}
        />
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#f7f8fc',
    flex: 1
  }
});

export default Home;
