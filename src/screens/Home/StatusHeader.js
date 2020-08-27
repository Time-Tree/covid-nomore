import React from 'react';
import { StatusBar, View, Image, Text, StyleSheet } from 'react-native';

import { RoundedCard, AppLogo, RoundedButton } from '../../components';
import serviceActive from '../../../images/serviceActive.png';
import serviceInactive from '../../../images/serviceInactive.png';

const StatusHeader = ({
  isActive,
  isLoading,
  isDisabled,
  error,
  onActivatePress
}) => {
  const status = isActive ? 'active' : 'inactive';
  const infoText = isActive
    ? "Open the app once a day\n to check it's status"
    : 'Press this button to allow\n our service to work properly.';

  const statusBarHeight = StatusBar.currentHeight;

  return (
    <RoundedCard style={{ ...styles.card, paddingTop: statusBarHeight + 20 }}>
      <AppLogo />
      <View style={styles.statusWrapper}>
        <Text style={styles.statusText}>
          Service{'\n'}is {status}
        </Text>
        <Image
          style={styles.statusIcon}
          source={isActive ? serviceActive : serviceInactive}
        />
      </View>
      {!isDisabled && <Text style={styles.infoText}>{infoText}</Text>}
      <View style={styles.btnWrapper}>
        {isDisabled ? (
          <Text style={styles.error}>
            Services are disabled, please enable them from settings.
          </Text>
        ) : (
          <RoundedButton
            title={isActive ? 'DEACTIVATE SERVICE' : 'ACTIVATE SERVICE'}
            containerStyle={styles.buttonContainer}
            buttonStyle={isActive ? styles.buttonInactive : styles.button}
            onPress={onActivatePress}
            loading={isLoading}
          />
        )}
      </View>
      {error && <Text style={styles.error}>{error}</Text>}
    </RoundedCard>
  );
};

const styles = StyleSheet.create({
  card: {
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
    paddingLeft: 32,
    paddingBottom: 25
  },
  statusWrapper: {
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  statusText: {
    marginTop: 13,
    fontSize: 34
  },
  statusIcon: {
    width: 109,
    height: 125
  },
  buttonContainer: {
    marginTop: 24,
    marginLeft: -32
  },
  button: {
    backgroundColor: 'rgb(71, 189, 123)'
  },
  buttonInactive: {
    backgroundColor: 'rgb(255, 67, 109)'
  },
  btnWrapper: {
    alignItems: 'center'
  },
  error: {
    color: 'red',
    textAlign: 'center',
    marginTop: 10
  }
});

export default StatusHeader;
