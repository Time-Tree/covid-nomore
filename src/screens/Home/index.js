import React, { useState, useEffect } from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';
import NearbyAPI from '../../utils/nearbyAPI';
import reduxContainer from '../../redux/reduxContainer';
import StatusHeader from './StatusHeader';
import RiskProgress from './RiskProgress';
import FeelingFeedbackCard from './FeelingFeedbackCard';
import FeelingFeedbackModal from './FeelingFeedbackModal';
import VitalsCheckModal from './VitalsCheckModal';

const Home = props => {
  const [isActive, setIsActive] = useState(
    props.bleStatus || props.nearbyStatus
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  const [feeling, setFeeling] = useState(null);
  const [vitalsChecked, setVitalsChecked] = useState(false);
  const [vitalsModalDismissed, setVitalsModalDismissed] = useState(false);

  const isDisabled = !props.nearbyProcess && !props.bleProcess;
  const showFeelingModal = feeling === null;
  const showVitalsCheckModal =
    showFeelingModal === false &&
    vitalsChecked === false &&
    vitalsModalDismissed === false;

  const onFeedback = feelingFeedback => setFeeling(feelingFeedback);

  const activateHandler = async () => {
    setLoading(true);
    setError(false);
    let status = null;
    if (!isActive) {
      status = await NearbyAPI.startService();
    } else {
      status = await NearbyAPI.stopService();
    }
    if (status === 'SUCCESS') {
      const settings = await NearbyAPI.getSettings();
      console.log('Success status change', settings);
    } else if (status === 'BLE_ERROR') {
      setError('Bluetooth adapter is off or has some problems. Please retry!');
    } else {
      setError('Error with service. Please retry!');
    }
    setLoading(false);
  };

  useEffect(() => {
    setIsActive(props.bleStatus || props.nearbyStatus);
    setLoading(false);
  }, [props.bleStatus, props.nearbyStatus]);

  return (
    <View style={styles.container}>
      <ScrollView>
        <StatusHeader
          isActive={isActive}
          isLoading={loading}
          isDisabled={isDisabled}
          error={error}
          onActivatePress={activateHandler}
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

function mapStateToProps(state) {
  return {
    bleStatus: state.settings.bleStatus,
    nearbyStatus: state.settings.nearbyStatus,
    bleProcess: state.settings.bleProcess,
    nearbyProcess: state.settings.nearbyProcess
  };
}

const dispatchToProps = {};

export default reduxContainer(Home, mapStateToProps, dispatchToProps);
